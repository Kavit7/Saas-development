package com.saas.backend.serviceImpl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saas.backend.dto.EmailAnalysisResult;
import com.saas.backend.dto.EmailAttachmentDto;
import com.saas.backend.dto.IncomingMailMessage;
import com.saas.backend.models.AccommodationBooking;
import com.saas.backend.models.IncomingEmail;
import com.saas.backend.models.Safari;
import com.saas.backend.models.User;
import com.saas.backend.repositories.AccommodationBookingRepository;
import com.saas.backend.repositories.IncomingEmailRepository;
import com.saas.backend.service.AccommodationBookingService;
import com.saas.backend.service.EmailResponseDetectionService;
import com.saas.backend.service.IncomingEmailService;
import com.saas.backend.service.InvoiceService;
import com.saas.backend.service.MailboxClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class IncomingEmailServiceImpl
        implements IncomingEmailService {


    private final MailboxClient mailboxClient;

    private final IncomingEmailRepository
            incomingEmailRepository;

    private final AccommodationBookingRepository
            bookingRepository;

    private final EmailResponseDetectionService
            responseDetectionService;

    private final AccommodationBookingService
            bookingService;

    private final InvoiceService
            invoiceService;

    private final com.saas.backend.service.EmailService
            emailService;

    private final com.saas.backend.repositories.UserRepository
            userRepository;

    @org.springframework.beans.factory.annotation.Value("${app.upload.dir:uploads}")
    private String uploadBaseDir = "uploads";

    @Override
    public void processNewEmails() {

        List<IncomingMailMessage> emails =
                mailboxClient.fetchUnreadEmails();

        for (IncomingMailMessage email : emails) {

            try {

                processEmail(email);

            } catch (Exception e) {

                // One bad email must not stop the others
                log.error(
                        "Failed to process incoming email. messageId={}, subject={}",
                        email.getMessageId(),
                        email.getSubject(),
                        e
                );
            }
        }

        reprocessPendingEmails();
    }

    private void reprocessPendingEmails() {
        List<IncomingEmail> pending = incomingEmailRepository.findByProcessedFalse();
        for (IncomingEmail pendingEmail : pending) {
            try {
                AccommodationBooking booking = pendingEmail.getBooking();
                if (booking == null) {
                    String ref = extractBookingReference(pendingEmail.getSubject(), pendingEmail.getBody());
                    if (ref != null) {
                        booking = bookingRepository.findByReferenceNumber(ref);
                        if (booking != null) {
                            pendingEmail.setBooking(booking);
                            incomingEmailRepository.save(pendingEmail);
                        }
                    }
                }
                if (booking != null) {
                    EmailAnalysisResult analysis = responseDetectionService.analyzeEmailResponse(
                            pendingEmail.getSubject(), pendingEmail.getBody()
                    );
                    if (analysis != null) {
                        IncomingMailMessage mailMsg = IncomingMailMessage.builder()
                                .messageId(pendingEmail.getMessageId())
                                .from(pendingEmail.getFromEmail())
                                .to(pendingEmail.getToEmail())
                                .subject(pendingEmail.getSubject())
                                .body(pendingEmail.getBody())
                                .receivedAt(pendingEmail.getReceivedAt())
                                .build();

                        switch (analysis.getResponseType()) {
                            case CONFIRMED:
                                bookingService.processEmailConfirmation(booking, mailMsg, analysis.getConfirmationNumber());
                                break;
                            case DECLINED:
                                bookingService.processEmailDecline(booking, mailMsg);
                                break;
                            case NEED_MORE_INFORMATION:
                            case UNKNOWN:
                            default:
                                bookingService.markForManualReview(booking, mailMsg, analysis.getNotes());
                                break;
                        }
                        dispatchLodgeReplyAlerts(booking, mailMsg, analysis);
                        pendingEmail.setProcessed(true);
                        incomingEmailRepository.save(pendingEmail);
                        log.info("Reprocessed previously pending email {} (booking {}) as {}",
                                pendingEmail.getMessageId(), booking.getReferenceNumber(), analysis.getResponseType());
                    }
                }
            } catch (Exception ex) {
                log.warn("Failed to reprocess pending email {}: {}", pendingEmail.getMessageId(), ex.getMessage());
            }
        }
    }

    @Transactional
    protected void processEmail(
            IncomingMailMessage email) {

        // 1. Prevent duplicate processing
        Optional<IncomingEmail> existingOpt =
                incomingEmailRepository.findByMessageId(email.getMessageId());

        if (existingOpt.isPresent() && existingOpt.get().isProcessed()) {
            log.debug("Skipping already processed incoming email: {}", email.getMessageId());
            return;
        }

        // 2. Extract booking reference
        String reference =
                extractBookingReference(
                        email.getSubject(),
                        email.getBody()
                );

        /*
         * Email may not belong to our booking system.
         */
        if (reference == null) {
            saveUnmatchedEmail(email);
            return;
        }

        // 3. Find booking
        AccommodationBooking booking =
                bookingRepository
                        .findByReferenceNumber(reference);

        if (booking == null) {
            saveUnmatchedEmail(email);
            return;
        }

        // 4. Save incoming email
        IncomingEmail incomingEmail = existingOpt.orElseGet(() ->
                IncomingEmail.builder()
                        .messageId(email.getMessageId())
                        .fromEmail(email.getFrom())
                        .toEmail(email.getTo())
                        .subject(email.getSubject())
                        .body(email.getBody())
                        .receivedAt(email.getReceivedAt())
                        .booking(booking)
                        .processed(false)
                        .build()
        );
        incomingEmail.setBooking(booking);
        if (email.getAttachments() != null && !email.getAttachments().isEmpty()) {
            incomingEmail.setHasAttachment(true);
            EmailAttachmentDto first = email.getAttachments().get(0);
            incomingEmail.setAttachmentFileName(first.getFileName());
            incomingEmail.setAttachmentFileType(first.getContentType());
            try {
                String safeMsgId = (email.getMessageId() != null)
                        ? email.getMessageId().replaceAll("[^a-zA-Z0-9.-]", "_")
                        : "msg_" + System.currentTimeMillis();
                Path attDir = Paths.get(uploadBaseDir, "email-attachments", safeMsgId);
                Files.createDirectories(attDir);
                String safeName = (first.getFileName() != null && !first.getFileName().isBlank())
                        ? first.getFileName().replaceAll("[^a-zA-Z0-9.-]", "_")
                        : "attachment.pdf";
                Path attFile = attDir.resolve(safeName);
                if (first.getData() != null && first.getData().length > 0) {
                    Files.write(attFile, first.getData());
                    incomingEmail.setAttachmentFilePath(attFile.toAbsolutePath().toString());
                    log.info("Persisted email attachment to {}", attFile.toAbsolutePath());
                }
            } catch (Exception ex) {
                log.warn("Failed to persist email attachment to disk: {}", ex.getMessage());
            }
        }
        incomingEmailRepository.save(incomingEmail);

        // 5. Intelligent AI / Heuristic Analysis
        EmailAnalysisResult analysis =
                responseDetectionService
                        .analyzeEmailResponse(
                                email.getSubject(),
                                email.getBody()
                        );

        // 6. Process response based on AI / heuristic decision
        switch (analysis.getResponseType()) {

            case CONFIRMED:
                bookingService
                        .processEmailConfirmation(
                                booking,
                                email,
                                analysis.getConfirmationNumber()
                        );
                break;

            case DECLINED:
                bookingService
                        .processEmailDecline(
                                booking,
                                email
                        );
                break;

            case NEED_MORE_INFORMATION:
            case UNKNOWN:
            default:
                bookingService
                        .markForManualReview(
                                booking,
                                email,
                                analysis.getNotes()
                        );
                break;
        }

        // 6b. Auto-detect and process lodge invoice attachments (PDF / Excel)
        processInvoiceAttachments(booking, email);

        // 6c. Dispatch alert email to Sales Person and Reservation Manager
        dispatchLodgeReplyAlerts(booking, email, analysis);

        // 7. Mark email as processed
        incomingEmail.setProcessed(true);

        incomingEmailRepository.save(
                incomingEmail
        );
    }

    private void dispatchLodgeReplyAlerts(AccommodationBooking booking, IncomingMailMessage email, EmailAnalysisResult analysis) {
        if (booking == null) return;
        try {
            // 1. Notify Sales Consultant
            Safari safari = (booking.getAccommodationRequirement() != null)
                    ? booking.getAccommodationRequirement().getSafari()
                    : null;
            User salesPerson = (safari != null) ? safari.getSalesPerson() : null;
            if (salesPerson != null && salesPerson.getEmail() != null) {
                emailService.sendLodgeReplyAlert(salesPerson, booking, email, analysis);
            }

            // 2. Notify Reservation Manager
            User rm = booking.getReservationManager();
            if (rm != null && rm.getEmail() != null) {
                if (salesPerson == null || !rm.getId().equals(salesPerson.getId())) {
                    emailService.sendLodgeReplyAlert(rm, booking, email, analysis);
                }
            } else if (safari != null && safari.getClient() != null && safari.getClient().getCompany() != null) {
                List<User> companyRms = userRepository.findReservationManagersByCompanyId(safari.getClient().getCompany().getId());
                for (User companyRm : companyRms) {
                    if (companyRm.getEmail() != null && (salesPerson == null || !companyRm.getId().equals(salesPerson.getId()))) {
                        emailService.sendLodgeReplyAlert(companyRm, booking, email, analysis);
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Failed to dispatch lodge reply alerts for booking {}: {}", booking.getReferenceNumber(), ex.getMessage());
        }
    }

    private void processInvoiceAttachments(AccommodationBooking booking, IncomingMailMessage email) {
        if (email.getAttachments() == null || email.getAttachments().isEmpty()) {
            return;
        }

        for (EmailAttachmentDto att : email.getAttachments()) {
            String name = att.getFileName() != null ? att.getFileName().toLowerCase() : "";
            String ct = att.getContentType() != null ? att.getContentType().toLowerCase() : "";
            boolean isInvoiceDoc = name.endsWith(".pdf") || name.endsWith(".xlsx") || name.endsWith(".xls")
                    || ct.contains("pdf") || ct.contains("spreadsheet") || ct.contains("excel")
                    || name.contains("invoice") || name.contains("bill") || name.contains("proforma");

            if (isInvoiceDoc && att.getData() != null && att.getData().length > 0) {
                try {
                    log.info("Found invoice attachment '{}' ({} bytes, ct: {}) in email {} for booking {}",
                            att.getFileName(), att.getSize(), att.getContentType(), email.getMessageId(), booking.getReferenceNumber());
                    invoiceService.processInboundInvoiceAttachment(
                            booking,
                            att.getData(),
                            att.getFileName(),
                            att.getContentType()
                    );
                } catch (Exception ex) {
                    log.error("Error processing inbound invoice attachment '{}': {}", att.getFileName(), ex.getMessage(), ex);
                }
            }
        }
    }

    private String extractBookingReference(
            String subject,
            String body) {

        String content =
                (subject == null ? "" : subject)
                + " "
                + (body == null ? "" : body);

        Pattern pattern =
                Pattern.compile(
                        "ACC-BOOK-\\d{4}-[A-Z0-9]+"
                );

        Matcher matcher =
                pattern.matcher(content);

        if (matcher.find()) {

            return matcher.group();
        }

        return null;
    }

    private void saveUnmatchedEmail(
            IncomingMailMessage email) {

        IncomingEmail incomingEmail =
                IncomingEmail.builder()
                        .messageId(
                                email.getMessageId()
                        )
                        .fromEmail(
                                email.getFrom()
                        )
                        .toEmail(
                                email.getTo()
                        )
                        .subject(
                                email.getSubject()
                        )
                        .body(
                                email.getBody()
                        )
                        .receivedAt(
                                email.getReceivedAt()
                        )
                        .processed(false)
                        .build();

        incomingEmailRepository.save(
                incomingEmail
        );
    }
}