package com.saas.backend.serviceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saas.backend.dto.UnpaidAccommodationAlertDto;
import com.saas.backend.models.*;
import com.saas.backend.repositories.*;
import com.saas.backend.service.EmailService;
import com.saas.backend.service.PaymentReminderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentReminderServiceImpl implements PaymentReminderService {

    private final CompanyRepository companyRepository;
    private final AccommodationBookingRepository bookingRepository;
    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    /**
     * Daily scheduled cron job.
     * Default runs at 8:00 AM every morning.
     * Can be configured via app.scheduling.payment-reminder-cron in application.properties.
     */
    @Scheduled(cron = "${app.scheduling.payment-reminder-cron:0 0 8 * * ?}")
    public void scheduledDailyPaymentCheck() {
        log.info("Starting scheduled daily payment deadline reminder scan...");
        try {
            executePaymentRemindersBatch();
        } catch (Exception ex) {
            log.error("Error during scheduled payment reminder batch execution: {}", ex.getMessage(), ex);
        }
    }

    @Override
    @Transactional
    public Map<String, Object> executePaymentRemindersBatch() {
        LocalDate today = LocalDate.now();
        List<Company> companies = companyRepository.findAll();

        int totalSalesEmailsDispatched = 0;
        int totalRmEmailsDispatched = 0;
        int totalAlertsDetected = 0;

        for (Company company : companies) {
            if (company.getId() == null) continue;

            List<AccommodationBooking> bookings = bookingRepository.findAllByCompanyId(company.getId());
            if (bookings.isEmpty()) continue;

            Map<User, List<UnpaidAccommodationAlertDto>> groupedAlerts = new LinkedHashMap<>();

            for (AccommodationBooking booking : bookings) {
                // Ignore already settled or cancelled bookings
                if (booking.getStatus() == BookingStatus.PAID || booking.getStatus() == BookingStatus.CANCELLED) {
                    continue;
                }

                AccommodationRequirement req = booking.getAccommodationRequirement();
                Safari safari = req != null ? req.getSafari() : null;
                if (safari == null || safari.getStatus() == SafariStatus.CANCELLED || safari.getStatus() == SafariStatus.COMPLETED) {
                    continue;
                }

                Invoice invoice = invoiceRepository.findByAccommodationBookingId(booking.getId()).orElse(null);
                UnpaidAccommodationAlertDto alert = evaluateBookingAlert(booking, safari, invoice, today);

                if (alert != null) {
                    totalAlertsDetected++;
                    User salesPerson = safari.getSalesPerson();
                    if (salesPerson != null) {
                        groupedAlerts.computeIfAbsent(salesPerson, k -> new ArrayList<>()).add(alert);
                    }
                }
            }

            // 1. Dispatch individual payment deadline alerts to each affected Sales Consultant
            for (Map.Entry<User, List<UnpaidAccommodationAlertDto>> entry : groupedAlerts.entrySet()) {
                User salesPerson = entry.getKey();
                List<UnpaidAccommodationAlertDto> alertList = entry.getValue();

                if (salesPerson != null && salesPerson.getEmail() != null && !alertList.isEmpty()) {
                    try {
                        emailService.sendSalesPaymentDeadlineAlert(salesPerson, alertList);
                        totalSalesEmailsDispatched++;

                        // Also record in-app notification for the sales consultant
                        boolean hasOverdue = alertList.stream().anyMatch(a -> "OVERDUE".equalsIgnoreCase(a.getUrgency()));
                        Notification notif = Notification.builder()
                                .user(salesPerson)
                                .company(company)
                                .type("PAYMENT_DEADLINE")
                                .title("Payment Deadline: " + alertList.size() + " Accommodation(s) Need Attention")
                                .message(String.format("You have %d accommodation booking(s) with upcoming or overdue payment deadlines. Review immediately to avoid cancellation.", alertList.size()))
                                .priority(hasOverdue ? Priority.URGENT : Priority.HIGH)
                                .sentAt(OffsetDateTime.now())
                                .status("SENT")
                                .relatedEntityType("AccommodationBooking")
                                .relatedEntityId(alertList.get(0).getBookingId())
                                .build();
                        notificationRepository.save(notif);
                    } catch (Exception e) {
                        log.error("Failed to dispatch payment alert email to sales person {}: {}", salesPerson.getEmail(), e.getMessage());
                    }
                }
            }

            // 2. Dispatch grouped daily summary to Reservation Managers of the company
            if (!groupedAlerts.isEmpty()) {
                List<User> rms = userRepository.findReservationManagersByCompanyId(company.getId());
                for (User rm : rms) {
                    if (rm.getEmail() != null && !rm.getEmail().isBlank()) {
                        try {
                            emailService.sendReservationManagerPaymentSummary(rm, groupedAlerts);
                            totalRmEmailsDispatched++;

                            Notification rmNotif = Notification.builder()
                                    .user(rm)
                                    .company(company)
                                    .type("PAYMENT_DEADLINE_SUMMARY")
                                    .title("Daily Deadline Summary: " + totalAlertsDetected + " Expiring Accommodations")
                                    .message(String.format("Consolidated payment digest: %d accommodations require verification across %d sales consultants.", totalAlertsDetected, groupedAlerts.size()))
                                    .priority(Priority.HIGH)
                                    .sentAt(OffsetDateTime.now())
                                    .status("SENT")
                                    .build();
                            notificationRepository.save(rmNotif);
                        } catch (Exception e) {
                            log.error("Failed to dispatch RM summary email to {}: {}", rm.getEmail(), e.getMessage());
                        }
                    }
                }
            }
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("timestamp", OffsetDateTime.now());
        summary.put("totalAlertsDetected", totalAlertsDetected);
        summary.put("salesEmailsDispatched", totalSalesEmailsDispatched);
        summary.put("rmEmailsDispatched", totalRmEmailsDispatched);
        summary.put("status", "SUCCESS");

        log.info("Completed payment reminders batch: alerts={}, salesEmails={}, rmEmails={}",
                totalAlertsDetected, totalSalesEmailsDispatched, totalRmEmailsDispatched);
        return summary;
    }

    private UnpaidAccommodationAlertDto evaluateBookingAlert(
            AccommodationBooking booking,
            Safari safari,
            Invoice invoice,
            LocalDate today) {

        String clientName = safari.getClient() != null
                ? (safari.getClient().getFirstName() + " " + safari.getClient().getLastName()).trim()
                : "Safari Traveler";
        String clientEmail = safari.getClient() != null ? safari.getClient().getEmail() : null;
        String propName = booking.getProperty() != null && booking.getProperty().getName() != null
                ? booking.getProperty().getName()
                : "Partner Lodge";

        // Condition A: Official vendor invoice is attached
        if (invoice != null) {
            // Already paid invoices do not trigger deadline alerts
            if (invoice.getStatus() == InvoiceStatus.PAID || invoice.getStatus() == InvoiceStatus.CANCELLED) {
                return null;
            }

            LocalDate dueDate = invoice.getDueDate();
            if (dueDate == null) {
                dueDate = booking.getCheckIn();
            }

            if (dueDate != null) {
                long days = ChronoUnit.DAYS.between(today, dueDate);

                String urgency = null;
                String statusDesc = null;

                if (days < 0) {
                    urgency = "OVERDUE";
                    statusDesc = "Payment Overdue by " + Math.abs(days) + " Day(s)";
                } else if (days <= 1) {
                    urgency = "DUE_IN_1_DAY";
                    statusDesc = days == 0 ? "Payment Due Today" : "Payment Due Tomorrow";
                } else if (days <= 3) {
                    urgency = "DUE_IN_3_DAYS";
                    statusDesc = "Payment Due in " + days + " Days";
                } else if (booking.getCheckIn() != null && ChronoUnit.DAYS.between(today, booking.getCheckIn()) <= 7) {
                    urgency = "UPCOMING_SAFARI_UNPAID";
                    statusDesc = "Safari Starts Soon (" + ChronoUnit.DAYS.between(today, booking.getCheckIn()) + " days) - Unpaid";
                }

                if (urgency != null) {
                    return UnpaidAccommodationAlertDto.builder()
                            .bookingId(booking.getId())
                            .bookingReference(booking.getReferenceNumber())
                            .clientName(clientName)
                            .clientEmail(clientEmail)
                            .safariReference(safari.getReferenceNumber())
                            .safariStartDate(safari.getStartDate())
                            .propertyName(propName)
                            .checkInDate(booking.getCheckIn())
                            .checkOutDate(booking.getCheckOut())
                            .invoiceNumber(invoice.getInvoiceNumber())
                            .amount(invoice.getAmount())
                            .currency(invoice.getCurrency() != null ? invoice.getCurrency() : "USD")
                            .dueDate(dueDate)
                            .urgency(urgency)
                            .statusDescription(statusDesc)
                            .daysRemaining(days)
                            .hasInvoice(true)
                            .build();
                }
            }
        } else {
            // Condition B: No vendor invoice received yet, but stay is confirmed/provisional and safari begins within 7 days
            if (booking.getCheckIn() != null) {
                long daysToCheckIn = ChronoUnit.DAYS.between(today, booking.getCheckIn());
                if (daysToCheckIn <= 7 && daysToCheckIn >= 0) {
                    return UnpaidAccommodationAlertDto.builder()
                            .bookingId(booking.getId())
                            .bookingReference(booking.getReferenceNumber())
                            .clientName(clientName)
                            .clientEmail(clientEmail)
                            .safariReference(safari.getReferenceNumber())
                            .safariStartDate(safari.getStartDate())
                            .propertyName(propName)
                            .checkInDate(booking.getCheckIn())
                            .checkOutDate(booking.getCheckOut())
                            .invoiceNumber("Awaiting Lodge Bill")
                            .amount(null)
                            .currency("USD")
                            .dueDate(booking.getCheckIn())
                            .urgency("UPCOMING_SAFARI_UNPAID")
                            .statusDescription("Lodge Invoice Not Yet Received (Check-in in " + daysToCheckIn + " days)")
                            .daysRemaining(daysToCheckIn)
                            .hasInvoice(false)
                            .build();
                }
            }
        }

        return null;
    }
}
