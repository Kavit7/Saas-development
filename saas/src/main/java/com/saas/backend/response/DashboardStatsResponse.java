package com.saas.backend.response;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DashboardStatsResponse
 * Data transfer object encapsulating role-tailored dashboard analytics,
 * key performance indicators (KPIs), trend charts, and status distributions.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {

    /**
     * The role of the authenticated user (e.g. SUPER_ADMIN, ADMIN, SALES_PERSON, RESERVATION_MANAGER, GUIDE).
     */
    private String role;

    /**
     * User's full display name.
     */
    private String userFullName;

    /**
     * Associated company or organization name.
     */
    private String companyName;

    /**
     * Summary Key Performance Indicators tailored to the role.
     * Examples: totalSafaris, activeBookings, totalClients, totalRevenue, pendingRequirements, verifiedProperties.
     */
    private Map<String, Object> kpis;

    /**
     * Monthly or periodic trend data points for visual graph plotting.
     */
    private List<ChartPoint> trendChart;

    /**
     * Status breakdown data for donut/pie charts.
     */
    private List<StatusDistributionItem> statusDistribution;

    /**
     * Secondary category or tier breakdown data.
     */
    private List<StatusDistributionItem> secondaryDistribution;

    /**
     * List of recent operational items / activities relevant to the role.
     */
    private List<Map<String, Object>> recentItems;

    /**
     * Inner DTO representing a single data point on a trend graph.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChartPoint {
        private String label;        // e.g. "Jan", "Feb", "Mar"
        private Double value;        // primary numeric value
        private Double secondaryValue; // optional secondary metric (e.g., target, revenue)
        private Long count;          // count of records
    }

    /**
     * Inner DTO representing a slice of a status distribution chart.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusDistributionItem {
        private String label;        // e.g. "Confirmed", "Draft", "Cancelled"
        private Long count;          // number of items in this status
        private Double percentage;   // calculated percentage (0 - 100)
        private String color;        // hex or css color token
    }
}
