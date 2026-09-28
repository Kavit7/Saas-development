
package com.saas.backend.serviceImpl;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.saas.backend.dto.IncomingMailMessage;
import com.saas.backend.service.MailboxClient;

import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.search.FlagTerm;
import jakarta.mail.*;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ImapMailboxClient implements MailboxClient {

    @Value("${mail.imap.host}")
    private String host;

    @Value("${mail.imap.port}")
    private int port;

    @Value("${mail.imap.username}")
    private String username;

    @Value("${mail.imap.password}")
    private String password;

    @Override
    public List<IncomingMailMessage> fetchUnreadEmails() {

        List<IncomingMailMessage> emails = new ArrayList<>();

        Store store = null;
        Folder inbox = null;

        try {

            // 1. IMAP configuration
            Properties properties = new Properties();

            properties.put(
                    "mail.store.protocol",
                    "imaps"
            );

            properties.put(
                    "mail.imaps.host",
                    host
            );

            properties.put(
                    "mail.imaps.port",
                    String.valueOf(port)
            );

            properties.put(
                    "mail.imaps.ssl.enable",
                    "true"
            );

            // 2. Create mail session
            Session session =
                    Session.getInstance(properties);

            // 3. Connect to mailbox
            store = session.getStore("imaps");

            store.connect(
                    host,
                    username,
                    password
            );

            // 4. Open INBOX
            inbox = store.getFolder("INBOX");

            inbox.open(Folder.READ_WRITE);

            // 5. Find unread emails
            FlagTerm unread =
                    new FlagTerm(
                            new Flags(Flags.Flag.SEEN),
                            false
                    );

            Message[] messages =
                    inbox.search(unread);

            log.info(
                    "Found {} unread emails",
                    messages.length
            );

            // 6. Read emails
            for (Message message : messages) {

                try {

                    IncomingMailMessage mail =
                            convertMessage(message);

                    if (mail != null) {

                        emails.add(mail);

                    }

                } catch (Exception e) {

                    log.error(
                            "Failed to process email",
                            e
                    );
                }
            }

        } catch (Exception e) {

            log.error(
                    "Failed to connect to mailbox",
                    e
            );

        } finally {

            // 7. Close folder
            if (inbox != null && inbox.isOpen()) {

                try {
                    inbox.close(false);
                } catch (Exception e) {
                    log.error(
                            "Failed to close inbox",
                            e
                    );
                }
            }

            // 8. Close connection
            if (store != null && store.isConnected()) {

                try {
                    store.close();
                } catch (Exception e) {
                    log.error(
                            "Failed to close mail store",
                            e
                    );
                }
            }
        }

        return emails;
    }

    private IncomingMailMessage convertMessage(
            Message message) throws Exception {

        String messageId =
                getMessageId(message);

        String from =
                message.getFrom() != null
                        && message.getFrom().length > 0
                        ? message.getFrom()[0]
                                .toString()
                        : null;

        String to =
                message.getRecipients(
                        Message.RecipientType.TO
                ) != null
                        && message.getRecipients(
                                Message.RecipientType.TO
                        ).length > 0
                        ? message.getRecipients(
                                Message.RecipientType.TO
                        )[0].toString()
                        : null;

        String subject =
                message.getSubject();

        String body =
                extractText(message);

        OffsetDateTime receivedAt =
                message.getReceivedDate() != null
                        ? message.getReceivedDate()
                                .toInstant()
                                .atOffset(
                                        ZoneOffset.UTC
                                )
                        : OffsetDateTime.now();

        return IncomingMailMessage.builder()
                .messageId(messageId)
                .from(from)
                .to(to)
                .subject(subject)
                .body(body)
                .receivedAt(receivedAt)
                .build();
    }

    private String getMessageId(
            Message message) {

        try {

            String[] headers =
                    message.getHeader("Message-ID");

            if (headers != null
                    && headers.length > 0) {

                return headers[0];
            }

        } catch (Exception e) {

            log.error(
                    "Could not read Message-ID",
                    e
            );
        }

        return null;
    }

    private String extractText(
            Message message) throws Exception {

        Object content =
                message.getContent();

        if (content instanceof String) {

            return (String) content;
        }

        if (content instanceof Multipart multipart) {

            StringBuilder text =
                    new StringBuilder();

            for (int i = 0;
                 i < multipart.getCount();
                 i++) {

                BodyPart part =
                        multipart.getBodyPart(i);

                String disposition =
                        part.getDisposition();

                if (disposition != null
                        && disposition.equalsIgnoreCase(
                                Part.ATTACHMENT
                        )) {

                    continue;
                }

                Object partContent =
                        part.getContent();

                if (partContent instanceof String) {

                    text.append(
                            partContent
                    );
                }

                else if (partContent
                        instanceof Multipart) {

                    text.append(
                            extractMultipartText(
                                    (jakarta.mail.Multipart)
                                            partContent
                            )
                    );
                }
            }

            return text.toString();
        }

        return "";
    }

    private String extractMultipartText(
            Multipart multipart)
            throws Exception {

        StringBuilder text =
                new StringBuilder();

        for (int i = 0;
             i < multipart.getCount();
             i++) {

            BodyPart part =
                    multipart.getBodyPart(i);

            Object content =
                    part.getContent();

            if (content instanceof String) {

                text.append(content);
            }

            else if (content
                    instanceof jakarta.mail.Multipart) {

                text.append(
                        extractMultipartText(
                                (jakarta.mail.Multipart)
                                        content
                        )
                );
            }
        }

        return text.toString();
    }
}
