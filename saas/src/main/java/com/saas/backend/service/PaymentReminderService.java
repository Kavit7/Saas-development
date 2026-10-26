package com.saas.backend.service;

import java.util.Map;

public interface PaymentReminderService {

    /**
     * Executes the daily batch to scan unpaid accommodation bookings and invoices across all companies,
     * dispatching tailored payment deadline reminders to Sales Consultants and grouped summaries to Reservation Managers.
     *
     * @return summary map containing counts of alerts and emails dispatched
     */
    Map<String, Object> executePaymentRemindersBatch();
}
