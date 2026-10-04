package com.saas.backend.service;

import org.springframework.security.core.Authentication;
import com.saas.backend.response.DashboardStatsResponse;

/**
 * DashboardService
 * Defines the contract for fetching aggregated, role-specific metrics,
 * graphs, and activity feeds for the SaaS platform.
 */
public interface DashboardService {

    /**
     * Retrieves aggregated metrics, trend graphs, and recent activities
     * tailored to the authenticated user's assigned role and company.
     *
     * @param authentication the current Spring Security authentication context
     * @return role-tailored DashboardStatsResponse object
     */
    DashboardStatsResponse getDashboardStats(Authentication authentication);
}
