package com.saas.backend.service;

import com.saas.backend.models.EmailResponseType;

public interface EmailResponseDetectionService {

    EmailResponseType detectResponse(
            String subject,
            String body
    );

    com.saas.backend.dto.EmailAnalysisResult analyzeEmailResponse(
            String subject,
            String body
    );
}
