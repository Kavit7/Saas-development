package com.saas.backend.service;

import com.saas.backend.dto.PlatformAdminRequest;
import com.saas.backend.response.PlatformAdminResponse;

public interface PlatformAdminService {

    PlatformAdminResponse createPlatformAdmin(PlatformAdminRequest request);
} 