package com.saas.backend.dto;
import java.time.OffsetDateTime;
import lombok.*;
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IncomingMailMessage {

    private String messageId;

    private String from;

    private String to;

    private String subject;

    private String body;

    private OffsetDateTime receivedAt;

    @Builder.Default
    private java.util.List<EmailAttachmentDto> attachments = new java.util.ArrayList<>();
}
