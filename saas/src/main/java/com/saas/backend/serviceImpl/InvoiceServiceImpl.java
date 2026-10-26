package com.saas.backend.serviceImpl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.awt.Color;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.backend.dto.BankDetailsDto;
import com.saas.backend.dto.InvoiceExtractionResult;
import com.saas.backend.dto.InvoiceManualUpdateRequest;
import com.saas.backend.dto.InvoicePaymentDetailsDto;
import com.saas.backend.dto.InvoiceVerificationRequest;
import com.saas.backend.dto.PaymentPlanItemDto;
import com.saas.backend.Exception.ResourceNotFoundException;
import com.saas.backend.models.AccommodationBooking;
import com.saas.backend.models.Invoice;
import com.saas.backend.models.InvoiceStatus;
import com.saas.backend.models.Priority;
import com.saas.backend.models.User;
import com.saas.backend.repositories.AccommodationBookingRepository;
import com.saas.backend.repositories.InvoiceRepository;
import com.saas.backend.repositories.UserRepository;
import com.saas.backend.response.InvoiceResponse;
import com.saas.backend.service.InvoiceDocumentParser;
import com.saas.backend.service.InvoiceService;
import com.saas.backend.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final AccommodationBookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final InvoiceDocumentParser invoiceDocumentParser;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @Value("${app.upload.dir:uploads}")
    private String uploadBaseDir;

    @Override
    @Transactional
    public InvoiceResponse processInboundInvoiceAttachment(
            AccommodationBooking booking,
            byte[] fileBytes,
            String fileName,
            String contentType) {

        log.info("Processing inbound invoice attachment '{}' for booking {}", fileName, booking.getReferenceNumber());

        // 1. Save attachment to persistent storage
        String savedFilePath = storeInvoiceFile(booking.getId(), fileName, fileBytes);

        // 2. Fetch existing or create invoice attached to this specific booking
        Invoice invoice = invoiceRepository.findByAccommodationBookingId(booking.getId())
                .orElseGet(() -> Invoice.builder()
                        .accommodationBooking(booking)
                        .invoiceNumber("BILL-" + booking.getReferenceNumber())
                        .status(InvoiceStatus.PENDING)
                        .issuedAt(OffsetDateTime.now())
                        .verified(false)
                        .build());

        invoice.setFileName(fileName);
        invoice.setFilePath(savedFilePath);
        invoice.setFileType(contentType);

        // 3. Delegate to pluggable Document Parser (e.g. Gemini AI)
        InvoiceExtractionResult extraction = invoiceDocumentParser.parseInvoice(fileBytes, fileName, contentType);

        if (extraction != null && extraction.isSuccess()) {
            log.info("AI successfully extracted invoice data for file {}", fileName);
            if (extraction.getInvoiceNumber() != null && !extraction.getInvoiceNumber().isBlank()) {
                invoice.setInvoiceNumber(extraction.getInvoiceNumber());
            }
            if (extraction.getAmount() != null) {
                invoice.setAmount(extraction.getAmount());
            }
            if (extraction.getCurrency() != null && !extraction.getCurrency().isBlank()) {
                invoice.setCurrency(extraction.getCurrency());
            }
            if (extraction.getDueDate() != null) {
                invoice.setDueDate(extraction.getDueDate());
            }

            InvoicePaymentDetailsDto paymentDetails = InvoicePaymentDetailsDto.builder()
                    .bankDetails(extraction.getBankDetails())
                    .paymentPlan(extraction.getPaymentPlan())
                    .confidenceScore(extraction.getConfidenceScore())
                    .detectionSource(invoiceDocumentParser.getParserName())
                    .detectedAt(OffsetDateTime.now())
                    .extractionNotes(extraction.getExtractionNotes())
                    .rawSummary(extraction.getRawResponse())
                    .build();

            invoice.setPaymentDetails(writeJsonSafe(paymentDetails));
            invoice.setAiExtracted(true);
            invoice.setNeedsManualReview(false);
            invoice.setExtractionError(null);

        } else {
            String errorMsg = extraction != null ? extraction.getErrorMessage() : "Document parser returned no result";
            log.warn("Automatic invoice detection failed for booking {}: {}. Triggering manual entry fallback.",
                    booking.getReferenceNumber(), errorMsg);

            invoice.setAiExtracted(false);
            invoice.setNeedsManualReview(true);
            invoice.setExtractionError(errorMsg);

            // Populate an empty payment details structure so it can be completed manually
            InvoicePaymentDetailsDto emptyDetails = InvoicePaymentDetailsDto.builder()
                    .bankDetails(new BankDetailsDto())
                    .paymentPlan(Collections.emptyList())
                    .confidenceScore(0.0)
                    .detectionSource("FAILED_FALLBACK")
                    .detectedAt(OffsetDateTime.now())
                    .extractionNotes(errorMsg)
                    .build();
            invoice.setPaymentDetails(writeJsonSafe(emptyDetails));

            // Notify assigned reservation officer or management
            notifyManualReviewRequired(booking, fileName, errorMsg);
        }

        invoice.setVerified(false);
        Invoice saved = invoiceRepository.save(invoice);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public InvoiceResponse uploadAndDetectInvoice(UUID bookingId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No file provided for upload.");
        }

        AccommodationBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));

        try {
            byte[] bytes = file.getBytes();
            String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "invoice.pdf";
            String contentType = file.getContentType() != null ? file.getContentType() : "application/pdf";

            return processInboundInvoiceAttachment(booking, bytes, originalFilename, contentType);

        } catch (IOException e) {
            log.error("Failed to read uploaded file: {}", e.getMessage(), e);
            throw new RuntimeException("Could not read uploaded invoice file: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceByBookingId(UUID bookingId) {
        Invoice invoice = invoiceRepository.findByAccommodationBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("No invoice recorded for booking: " + bookingId));
        return mapToResponse(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceById(UUID id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with ID: " + id));
        return mapToResponse(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getAllInvoices(
            InvoiceStatus status,
            UUID bookingId,
            Boolean needsReview,
            Boolean verified) {

        List<Invoice> list = invoiceRepository.findAllByOrderByIssuedAtDesc();

        return list.stream()
                .filter(inv -> status == null || inv.getStatus() == status)
                .filter(inv -> bookingId == null || (inv.getAccommodationBooking() != null && bookingId.equals(inv.getAccommodationBooking().getId())))
                .filter(inv -> needsReview == null || Boolean.valueOf(needsReview).equals(inv.getNeedsManualReview()))
                .filter(inv -> verified == null || Boolean.valueOf(verified).equals(inv.getVerified()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public InvoiceResponse updateInvoiceManually(UUID id, InvoiceManualUpdateRequest request) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with ID: " + id));

        if (request.getInvoiceNumber() != null && !request.getInvoiceNumber().isBlank()) {
            invoice.setInvoiceNumber(request.getInvoiceNumber().trim());
        }
        if (request.getAmount() != null) {
            invoice.setAmount(request.getAmount());
        }
        if (request.getCurrency() != null && !request.getCurrency().isBlank()) {
            invoice.setCurrency(request.getCurrency().trim());
        }
        if (request.getDueDate() != null) {
            invoice.setDueDate(request.getDueDate());
        }
        if (request.getStatus() != null) {
            invoice.setStatus(request.getStatus());
        }

        // Parse or create payment details structure
        InvoicePaymentDetailsDto existing = parsePaymentDetailsSafe(invoice.getPaymentDetails());
        if (existing == null) {
            existing = new InvoicePaymentDetailsDto();
        }

        if (request.getBankDetails() != null) {
            existing.setBankDetails(request.getBankDetails());
        }
        if (request.getPaymentPlan() != null) {
            existing.setPaymentPlan(request.getPaymentPlan());
        }
        existing.setDetectionSource("MANUAL_INPUT");
        existing.setExtractionNotes(request.getNotes() != null ? request.getNotes() : "Manually updated by operator");

        invoice.setPaymentDetails(writeJsonSafe(existing));
        invoice.setNeedsManualReview(false);
        invoice.setExtractionError(null);

        Invoice saved = invoiceRepository.save(invoice);
        log.info("Manually updated invoice {}", invoice.getInvoiceNumber());
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public InvoiceResponse createOrUpdateInvoiceForBooking(UUID bookingId, InvoiceManualUpdateRequest request) {
        AccommodationBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Accommodation Booking not found with ID: " + bookingId));

        Invoice invoice = invoiceRepository.findByAccommodationBookingId(bookingId).orElse(null);
        if (invoice == null) {
            String invNum = (request.getInvoiceNumber() != null && !request.getInvoiceNumber().isBlank())
                    ? request.getInvoiceNumber().trim()
                    : "INV-" + booking.getReferenceNumber().replace("ACC-BOOK-", "");

            invoice = Invoice.builder()
                    .accommodationBooking(booking)
                    .invoiceNumber(invNum)
                    .amount(request.getAmount())
                    .currency(request.getCurrency() != null && !request.getCurrency().isBlank() ? request.getCurrency().trim() : "USD")
                    .dueDate(request.getDueDate() != null ? request.getDueDate() : (booking.getCheckIn() != null ? booking.getCheckIn() : LocalDate.now().plusDays(7)))
                    .status(request.getStatus() != null ? request.getStatus() : InvoiceStatus.PENDING)
                    .issuedAt(OffsetDateTime.now())
                    .aiExtracted(false)
                    .needsManualReview(false)
                    .verified(false)
                    .build();
        } else {
            if (request.getInvoiceNumber() != null && !request.getInvoiceNumber().isBlank()) {
                invoice.setInvoiceNumber(request.getInvoiceNumber().trim());
            }
            if (request.getAmount() != null) {
                invoice.setAmount(request.getAmount());
            }
            if (request.getCurrency() != null && !request.getCurrency().isBlank()) {
                invoice.setCurrency(request.getCurrency().trim());
            }
            if (request.getDueDate() != null) {
                invoice.setDueDate(request.getDueDate());
            }
            if (request.getStatus() != null) {
                invoice.setStatus(request.getStatus());
            }
        }

        InvoicePaymentDetailsDto existing = parsePaymentDetailsSafe(invoice.getPaymentDetails());
        if (existing == null) {
            existing = new InvoicePaymentDetailsDto();
        }

        if (request.getBankDetails() != null) {
            existing.setBankDetails(request.getBankDetails());
        }
        if (request.getPaymentPlan() != null) {
            existing.setPaymentPlan(request.getPaymentPlan());
        }
        existing.setDetectionSource("MANUAL_INPUT");
        existing.setExtractionNotes(request.getNotes() != null ? request.getNotes() : "Manually entered by operator");

        invoice.setPaymentDetails(writeJsonSafe(existing));
        invoice.setNeedsManualReview(false);
        invoice.setExtractionError(null);

        Invoice saved = invoiceRepository.save(invoice);
        log.info("Manually created/updated invoice {} for booking {}", saved.getInvoiceNumber(), booking.getReferenceNumber());
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public InvoiceResponse verifyInvoice(UUID id, InvoiceVerificationRequest request) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with ID: " + id));

        User currentUser = getCurrentUser();
        invoice.setVerified(Boolean.TRUE.equals(request.getVerified()));
        invoice.setVerifiedBy(currentUser);
        invoice.setVerifiedAt(OffsetDateTime.now());
        invoice.setVerificationNotes(request.getVerificationNotes());

        if (Boolean.TRUE.equals(request.getVerified())) {
            invoice.setNeedsManualReview(false);
        }

        Invoice saved = invoiceRepository.save(invoice);
        log.info("Invoice {} verified status set to {} by {}",
                invoice.getInvoiceNumber(), invoice.getVerified(),
                currentUser != null ? currentUser.getEmail() : "system");

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public byte[] getInvoiceFileBytes(UUID id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + id));

        if (invoice.getFilePath() != null) {
            try {
                Path path = Paths.get(invoice.getFilePath());
                if (Files.exists(path) && Files.size(path) > 0) {
                    return Files.readAllBytes(path);
                }
            } catch (Exception ignored) {}
        }

        // Dynamically generate official PDF invoice on the fly
        byte[] pdfBytes = generateInvoicePdf(invoice);
        try {
            String invNum = (invoice.getInvoiceNumber() != null && !invoice.getInvoiceNumber().isBlank())
                    ? invoice.getInvoiceNumber().replaceAll("[^a-zA-Z0-9.-]", "_")
                    : ("INV-" + invoice.getId().toString().substring(0, 8));
            UUID bookingId = (invoice.getAccommodationBooking() != null) ? invoice.getAccommodationBooking().getId() : invoice.getId();
            Path dir = Paths.get(uploadBaseDir, "invoices", bookingId.toString());
            Files.createDirectories(dir);
            Path filePath = dir.resolve(invNum + ".pdf");
            Files.write(filePath, pdfBytes);
            invoice.setFilePath(filePath.toAbsolutePath().toString());
            if (invoice.getFileName() == null || invoice.getFileName().isBlank()) {
                invoice.setFileName(invNum + ".pdf");
            }
            if (invoice.getFileType() == null || invoice.getFileType().isBlank()) {
                invoice.setFileType("application/pdf");
            }
            invoiceRepository.save(invoice);
        } catch (Exception ex) {
            log.warn("Could not cache generated invoice PDF to disk: {}", ex.getMessage());
        }
        return pdfBytes;
    }

    private byte[] generateInvoicePdf(Invoice invoice) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 36, 36, 40, 40);
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(38, 70, 36));
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY);
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new Color(38, 70, 36));
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.BLACK);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);

            // 1. Header Bar
            Paragraph header = new Paragraph("ACCOMMODATION INVOICE / VOUCHER", titleFont);
            header.setAlignment(Element.ALIGN_CENTER);
            doc.add(header);

            String invNo = invoice.getInvoiceNumber() != null ? invoice.getInvoiceNumber() : ("INV-" + invoice.getId().toString().substring(0, 8));
            Paragraph sub = new Paragraph("Official Payment Document • Reference: " + invNo, subtitleFont);
            sub.setAlignment(Element.ALIGN_CENTER);
            sub.setSpacingAfter(15);
            doc.add(sub);

            // 2. Invoice & Property Overview Table
            PdfPTable metaTable = new PdfPTable(2);
            metaTable.setWidthPercentage(100);
            metaTable.setSpacingAfter(15);

            AccommodationBooking booking = invoice.getAccommodationBooking();
            String propName = (booking != null && booking.getProperty() != null) ? booking.getProperty().getName() : "Partner Lodge / Camp";
            String bookingRef = (booking != null && booking.getReferenceNumber() != null) ? booking.getReferenceNumber() : "N/A";
            String checkIn = (booking != null && booking.getCheckIn() != null) ? booking.getCheckIn().toString() : "N/A";
            String checkOut = (booking != null && booking.getCheckOut() != null) ? booking.getCheckOut().toString() : "N/A";

            String clientName = "Safari Client";
            String safariRef = "N/A";
            if (booking != null && booking.getAccommodationRequirement() != null && booking.getAccommodationRequirement().getSafari() != null) {
                safariRef = booking.getAccommodationRequirement().getSafari().getReferenceNumber();
                if (booking.getAccommodationRequirement().getSafari().getClient() != null) {
                    clientName = booking.getAccommodationRequirement().getSafari().getClient().getFirstName() + " " +
                                 booking.getAccommodationRequirement().getSafari().getClient().getLastName();
                }
            }

            PdfPCell leftCell = new PdfPCell();
            leftCell.setBorder(PdfPCell.BOX);
            leftCell.setBorderColor(new Color(220, 225, 230));
            leftCell.setPadding(8);
            leftCell.addElement(new Paragraph("BILL TO / RECIPIENT", boldFont));
            leftCell.addElement(new Paragraph("Client: " + clientName, normalFont));
            leftCell.addElement(new Paragraph("Safari Ref: " + safariRef, normalFont));
            leftCell.addElement(new Paragraph("Booking Ref: " + bookingRef, normalFont));
            metaTable.addCell(leftCell);

            PdfPCell rightCell = new PdfPCell();
            rightCell.setBorder(PdfPCell.BOX);
            rightCell.setBorderColor(new Color(220, 225, 230));
            rightCell.setPadding(8);
            rightCell.addElement(new Paragraph("LODGE & ISSUANCE DETAILS", boldFont));
            rightCell.addElement(new Paragraph("Property: " + propName, normalFont));
            rightCell.addElement(new Paragraph("Stay: " + checkIn + " to " + checkOut, normalFont));
            rightCell.addElement(new Paragraph("Status: " + (invoice.getStatus() != null ? invoice.getStatus().name() : "PENDING"), boldFont));
            rightCell.addElement(new Paragraph("Due Date: " + (invoice.getDueDate() != null ? invoice.getDueDate().toString() : "Upon Receipt"), normalFont));
            metaTable.addCell(rightCell);

            doc.add(metaTable);

            // 3. Line Items Table
            PdfPTable itemsTable = new PdfPTable(4);
            itemsTable.setWidthPercentage(100);
            itemsTable.setWidths(new float[]{4f, 1.5f, 2f, 2.5f});
            itemsTable.setSpacingAfter(15);

            Color headerBg = new Color(38, 70, 36);
            addTableHeader(itemsTable, "Description", headerFont, headerBg);
            addTableHeader(itemsTable, "Quantity", headerFont, headerBg);
            addTableHeader(itemsTable, "Rate", headerFont, headerBg);
            addTableHeader(itemsTable, "Amount", headerFont, headerBg);

            int rooms = (booking != null && booking.getAccommodationRequirement() != null && booking.getAccommodationRequirement().getNumberOfRooms() != null)
                    ? booking.getAccommodationRequirement().getNumberOfRooms() : 1;
            String cur = (invoice.getCurrency() != null && !invoice.getCurrency().isBlank()) ? invoice.getCurrency() : "USD";
            String amtStr = (invoice.getAmount() != null) ? invoice.getAmount().toString() : "0.00";

            PdfPCell c1 = new PdfPCell(new Phrase("Accommodation & Full Board Stay (" + propName + ")", normalFont));
            c1.setPadding(6);
            itemsTable.addCell(c1);

            PdfPCell c2 = new PdfPCell(new Phrase(rooms + " Room(s)", normalFont));
            c2.setPadding(6);
            itemsTable.addCell(c2);

            PdfPCell c3 = new PdfPCell(new Phrase(cur + " " + amtStr, normalFont));
            c3.setPadding(6);
            itemsTable.addCell(c3);

            PdfPCell c4 = new PdfPCell(new Phrase(cur + " " + amtStr, boldFont));
            c4.setPadding(6);
            itemsTable.addCell(c4);

            doc.add(itemsTable);

            // 4. Total Summary Table
            PdfPTable totalTable = new PdfPTable(2);
            totalTable.setWidthPercentage(45);
            totalTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalTable.setSpacingAfter(20);

            PdfPCell tLabel = new PdfPCell(new Phrase("TOTAL AMOUNT:", boldFont));
            tLabel.setPadding(6);
            tLabel.setBorder(PdfPCell.NO_BORDER);
            totalTable.addCell(tLabel);

            PdfPCell tVal = new PdfPCell(new Phrase(cur + " " + amtStr, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(38, 70, 36))));
            tVal.setPadding(6);
            tVal.setBorder(PdfPCell.NO_BORDER);
            tVal.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalTable.addCell(tVal);

            doc.add(totalTable);

            // 5. Banking / Settlement Coordinates (if present)
            InvoicePaymentDetailsDto details = parsePaymentDetailsSafe(invoice.getPaymentDetails());
            if (details != null && details.getBankDetails() != null) {
                BankDetailsDto b = details.getBankDetails();
                PdfPTable bankTable = new PdfPTable(1);
                bankTable.setWidthPercentage(100);
                PdfPCell bCell = new PdfPCell();
                bCell.setBackgroundColor(new Color(248, 250, 252));
                bCell.setBorderColor(new Color(226, 232, 240));
                bCell.setPadding(8);
                bCell.addElement(new Paragraph("BANK SETTLEMENT INSTRUCTIONS", sectionFont));
                if (b.getBankName() != null) bCell.addElement(new Paragraph("Bank: " + b.getBankName(), normalFont));
                if (b.getAccountName() != null) bCell.addElement(new Paragraph("Account Name: " + b.getAccountName(), normalFont));
                if (b.getAccountNumber() != null) bCell.addElement(new Paragraph("Account Number / IBAN: " + b.getAccountNumber(), boldFont));
                if (b.getSwiftCode() != null) bCell.addElement(new Paragraph("SWIFT / BIC: " + b.getSwiftCode(), normalFont));
                if (b.getBranch() != null) bCell.addElement(new Paragraph("Branch: " + b.getBranch(), normalFont));
                bankTable.addCell(bCell);
                doc.add(bankTable);
            }

            // 6. Footer Note
            Paragraph footer = new Paragraph("Thank you for your business. Generated by Safari Sales OS Platform.", subtitleFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(15);
            doc.add(footer);

            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate PDF for invoice {}: {}", invoice.getInvoiceNumber(), e.getMessage(), e);
            throw new RuntimeException("Could not generate invoice PDF: " + e.getMessage(), e);
        }
    }

    private void addTableHeader(PdfPTable table, String text, Font font, Color bg) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(cell);
    }

    @Override
    @Transactional(readOnly = true)
    public String getInvoiceFileName(UUID id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + id));
        return invoice.getFileName() != null ? invoice.getFileName() : "invoice.pdf";
    }

    @Override
    @Transactional(readOnly = true)
    public String getInvoiceFileType(UUID id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + id));
        return invoice.getFileType() != null ? invoice.getFileType() : "application/pdf";
    }

    private String storeInvoiceFile(UUID bookingId, String originalFileName, byte[] data) {
        try {
            String sanitized = originalFileName != null
                    ? originalFileName.replaceAll("[^a-zA-Z0-9._-]", "_")
                    : "invoice.pdf";

            Path targetDir = Paths.get(uploadBaseDir, "invoices", bookingId.toString());
            Files.createDirectories(targetDir);

            Path targetFile = targetDir.resolve(System.currentTimeMillis() + "_" + sanitized);
            Files.write(targetFile, data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            return targetFile.toAbsolutePath().toString();

        } catch (IOException e) {
            log.error("Failed to store invoice file: {}", e.getMessage(), e);
            throw new RuntimeException("Could not persist invoice file: " + e.getMessage(), e);
        }
    }

    private InvoiceResponse mapToResponse(Invoice inv) {
        AccommodationBooking booking = inv.getAccommodationBooking();
        String bookingRef = booking != null ? booking.getReferenceNumber() : null;
        UUID bookingId = booking != null ? booking.getId() : null;
        String safariRef = (booking != null && booking.getAccommodationRequirement() != null
                && booking.getAccommodationRequirement().getSafari() != null)
                ? booking.getAccommodationRequirement().getSafari().getReferenceNumber()
                : null;
        String propName = (booking != null && booking.getProperty() != null)
                ? booking.getProperty().getName()
                : null;
        String clientName = (booking != null && booking.getAccommodationRequirement() != null
                && booking.getAccommodationRequirement().getSafari() != null
                && booking.getAccommodationRequirement().getSafari().getClient() != null)
                ? (booking.getAccommodationRequirement().getSafari().getClient().getFirstName() + " "
                   + booking.getAccommodationRequirement().getSafari().getClient().getLastName())
                : null;

        InvoicePaymentDetailsDto parsed = parsePaymentDetailsSafe(inv.getPaymentDetails());
        BankDetailsDto bankDetails = parsed != null ? parsed.getBankDetails() : null;
        List<PaymentPlanItemDto> paymentPlan = parsed != null ? parsed.getPaymentPlan() : Collections.emptyList();
        Double confidenceScore = parsed != null ? parsed.getConfidenceScore() : null;
        String extractionNotes = parsed != null ? parsed.getExtractionNotes() : null;

        UUID verifiedById = inv.getVerifiedBy() != null ? inv.getVerifiedBy().getId() : null;
        String verifiedByName = inv.getVerifiedBy() != null
                ? (inv.getVerifiedBy().getFirstName() + " " + inv.getVerifiedBy().getLastName())
                : null;

        return InvoiceResponse.builder()
                .id(inv.getId())
                .bookingId(bookingId)
                .bookingReference(bookingRef)
                .safariReference(safariRef)
                .propertyName(propName)
                .clientName(clientName)
                .invoiceNumber(inv.getInvoiceNumber())
                .amount(inv.getAmount())
                .currency(inv.getCurrency())
                .dueDate(inv.getDueDate())
                .status(inv.getStatus())
                .fileName(inv.getFileName())
                .filePath(inv.getFilePath())
                .fileType(inv.getFileType())
                .issuedAt(inv.getIssuedAt())
                .paymentDetailsJson(inv.getPaymentDetails())
                .bankDetails(bankDetails)
                .paymentPlan(paymentPlan)
                .confidenceScore(confidenceScore)
                .extractionNotes(extractionNotes)
                .verified(inv.getVerified())
                .verifiedById(verifiedById)
                .verifiedByName(verifiedByName)
                .verifiedAt(inv.getVerifiedAt())
                .verificationNotes(inv.getVerificationNotes())
                .aiExtracted(inv.getAiExtracted())
                .needsManualReview(inv.getNeedsManualReview())
                .extractionError(inv.getExtractionError())
                .build();
    }

    private void notifyManualReviewRequired(AccommodationBooking booking, String fileName, String reason) {
        try {
            User officer = booking.getReservationManager();
            if (officer != null) {
                notificationService.createNotification(
                        officer,
                        "INVOICE_MANUAL_REVIEW",
                        "Supplier Invoice Review Required: " + booking.getReferenceNumber(),
                        String.format("Lodge invoice file '%s' was received for booking %s, but automatic banking detection could not complete (%s). Please review and complete banking details manually.",
                                fileName, booking.getReferenceNumber(), reason),
                        Priority.HIGH,
                        "/bookings",officer.getId()
                );
            }
        } catch (Exception ex) {
            log.warn("Could not dispatch notification for invoice manual review: {}", ex.getMessage());
        }
    }

    private User getCurrentUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getName() != null) {
                return userRepository.findByEmail(auth.getName()).orElse(null);
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String writeJsonSafe(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("Failed to serialize object to JSON: {}", e.getMessage());
            return "{}";
        }
    }

    private InvoicePaymentDetailsDto parsePaymentDetailsSafe(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readValue(json, InvoicePaymentDetailsDto.class);
        } catch (Exception e) {
            log.warn("Could not deserialize payment_details JSONB: {}", e.getMessage());
            return null;
        }
    }
}
