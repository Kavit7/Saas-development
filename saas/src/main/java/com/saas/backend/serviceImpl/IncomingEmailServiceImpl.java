package com.saas.backend.serviceImpl;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saas.backend.dto.IncomingMailMessage;
import com.saas.backend.models.AccommodationBooking;
import com.saas.backend.models.EmailResponseType;
import com.saas.backend.models.IncomingEmail;
import com.saas.backend.repositories.AccommodationBookingRepository;
import com.saas.backend.repositories.IncomingEmailRepository;
import com.saas.backend.service.AccommodationBookingService;
import com.saas.backend.service.EmailResponseDetectionService;
import com.saas.backend.service.IncomingEmailService;
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
    }

    @Transactional
    protected void processEmail(
            IncomingMailMessage email) {

        // 1. Prevent duplicate processing

        if (incomingEmailRepository
                .existsByMessageId(email.getMessageId())) {

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
                        .booking(booking)
                        .processed(false)
                        .build();

        incomingEmailRepository.save(
                incomingEmail
        );

        // 5. Detect response

        EmailResponseType responseType =
                responseDetectionService
                        .detectResponse(
                                email.getSubject(),
                                email.getBody()
                        );

        // 6. Process response

        switch (responseType) {

            case CONFIRMED:

                bookingService
                        .processEmailConfirmation(
                                booking,
                                email
                        );

                break;

            case DECLINED:

                bookingService
                        .processEmailDecline(
                                booking,
                                email
                        );

                break;

            // case NEED_MORE_INFORMATION:

            //     bookingService
            //             .processInformationRequest(
            //                     booking,
            //                     email
            //             );

            //     break;

        //     case UNKNOWN:

        //         bookingService
        //                 .markForManualReview(
        //                         booking,
        //                         email
        //                 );

        //         break;
         }

        // 7. Mark email as processed

        incomingEmail.setProcessed(true);

        incomingEmailRepository.save(
                incomingEmail
        );
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