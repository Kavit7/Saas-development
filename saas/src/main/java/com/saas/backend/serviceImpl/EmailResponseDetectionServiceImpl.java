package com.saas.backend.serviceImpl;

import java.util.Locale;

import org.springframework.stereotype.Service;

import com.saas.backend.models.EmailResponseType;
import com.saas.backend.service.EmailResponseDetectionService;

@Service
public class EmailResponseDetectionServiceImpl
        implements EmailResponseDetectionService {

    @Override
    public EmailResponseType detectResponse(
            String subject,
            String body) {

        String content =
                ((subject == null ? "" : subject) + " "
                + (body == null ? "" : body))
                .toLowerCase(Locale.ROOT);

        /*
         * Check DECLINED first.
         *
         * Example:
         * "Unfortunately your reservation is not confirmed"
         *
         * If we checked "confirmed" first,
         * we could incorrectly mark it as CONFIRMED.
         */
        if (containsDeclinedKeywords(content)) {
            return EmailResponseType.DECLINED;
        }

        /*
         * Check CONFIRMED
         */
        if (containsConfirmedKeywords(content)) {
            return EmailResponseType.CONFIRMED;
        }

        /*
         * Property is asking for more information
         */
        if (containsMoreInformationKeywords(content)) {
            return EmailResponseType.NEED_MORE_INFORMATION;
        }

        return EmailResponseType.UNKNOWN;
    }

    private boolean containsDeclinedKeywords(
            String content) {

        return content.contains("not available")
                || content.contains("unavailable")
                || content.contains("fully booked")
                || content.contains("no availability")
                || content.contains("unable to accommodate")
                || content.contains("cannot accommodate")
                || content.contains("cannot accept")
                || content.contains("reservation declined")
                || content.contains("request declined")
                || content.contains("unfortunately")
                       && content.contains("available") == false;
    }

    private boolean containsConfirmedKeywords(
            String content) {

        return content.contains("Accommodation confirmed")
                || content.contains("booking confirmed")
                || content.contains("booking confirmed")
                || content.contains("request confirmed")
                || content.contains("reservation is confirmed")
                || content.contains("booking is confirmed")
                || content.contains("we confirm your reservation")
                || content.contains("reservation has been confirmed")
                || content.contains("booking has been confirmed");
    }

    private boolean containsMoreInformationKeywords(
            String content) {

        return content.contains("please provide")
                || content.contains("more information")
                || content.contains("additional information")
                || content.contains("need more information")
                || content.contains("please confirm the following")
                || content.contains("kindly provide");
    }
}