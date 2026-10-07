package com.saas.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailAttachmentDto {
    private String fileName;
    private String contentType;
    private byte[] data;
    private long size;
}
