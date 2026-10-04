package com.saas.backend.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saas.backend.response.DashboardStatsResponse;
import com.saas.backend.service.DashboardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * DashboardController
 * Exposes role-aware analytics endpoints to power the executive dashboard.
 * Each user receives data customized to their responsibilities (Super Admin, Company Admin, Sales Person, Reservation Manager, Guide).
 */
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Dashboard", description = "Role-tailored operations and executive analytics")
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Retrieves aggregated KPIs, graphs, and distribution stats for the authenticated user.
     *
     * @param authentication active security context holding user identity and role
     * @return DashboardStatsResponse containing live metrics, chart data points, and status breakdowns
     */
    @GetMapping("/stats")
    @Operation(summary = "Get role-specific dashboard metrics and graphs")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats(Authentication authentication) {
        DashboardStatsResponse stats = dashboardService.getDashboardStats(authentication);
        return ResponseEntity.ok(stats);
    }
}
