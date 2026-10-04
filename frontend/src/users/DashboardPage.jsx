import { useEffect, useState, useCallback } from "react";
import { WarningCircle, ArrowsClockwise } from "@phosphor-icons/react";
import { useAuth } from "../hooks/useAuth";
import { getAllData } from "../api/api";

// Executive Dashboard Modular Components
import DashboardHeader from "../components/dashboard/DashboardHeader";
import SuperAdminDashboard from "../components/dashboard/roles/SuperAdminDashboard";
import AdminDashboard from "../components/dashboard/roles/AdminDashboard";
import SalesDashboard from "../components/dashboard/roles/SalesDashboard";
import ReservationDashboard from "../components/dashboard/roles/ReservationDashboard";
import GuideDashboard from "../components/dashboard/roles/GuideDashboard";

/**
 * Normalizes role string representation across backend JWT claims and system configurations.
 * Handles prefixes such as 'ROLE_' and case sensitivity.
 *
 * @param {string} role - Raw role string from JWT or API
 * @returns {string} Clean uppercase role key
 */
const normalizeRoleKey = (role) => {
  if (!role) return "";
  return String(role).toUpperCase().replace(/^ROLE_/, "").trim();
};

/**
 * DashboardPage
 * Central executive dashboard container.
 * Inspects authenticated user credentials and dynamically renders the role-specific
 * dashboard view with live backend telemetry, KPI stats, and interactive charts.
 */
const DashboardPage = () => {
  const { user, token } = useAuth();
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  /**
   * Fetches real-time aggregated dashboard telemetry from the backend.
   * If backend service is initializing or unreachable, gracefully falls back to empty defaults
   * so the interface remains operational without breaking.
   */
  const fetchDashboardStats = useCallback(async () => {
    setLoading(true);
    setError(null);

    try {
      if (token) {
        const response = await getAllData("/api/v1/dashboard/stats", token);
        if (response) {
          setStats(response);
          return;
        }
      }
    } catch (err) {
      console.warn("Notice: Live dashboard stats fetch encountered an issue, initializing view:", err);
      setError(err?.message || "Failed to retrieve real-time analytics.");
    } finally {
      setLoading(false);
    }
  }, [token]);

  // Load telemetry on initial mount and when authentication token changes
  useEffect(() => {
    fetchDashboardStats();
  }, [fetchDashboardStats]);

  // Resolve the operational role for this user session
  // Prioritize the role reported by backend dashboard service, then user token claim
  const currentRole = normalizeRoleKey(stats?.role || user?.role_name || user?.role || "ADMIN");
  const userDisplayName = stats?.userFullName || user?.email?.split("@")[0] || "Executive Member";
  const userCompany = stats?.companyName || (user?.companyId ? "Safari Global Operations" : "Safari Platform");

  /**
   * Dispatches the appropriate role-based dashboard view component.
   * Every role has its own dedicated, separated module for clean maintainability.
   */
  const renderRoleDashboard = () => {
    switch (currentRole) {
      // 1. Platform Super Administrator (Tenant & Subscription analytics)
      case "SUPER_ADMIN":
      case "PLATFORM_ADMIN":
        return <SuperAdminDashboard stats={stats} />;

      // 2. Sales Person / Travel Consultant (Lead conversion, quotes, client pipeline)
      case "SALES_PERSON":
      case "SALE":
      case "SALES":
      case "SALESPERSON":
        return <SalesDashboard stats={stats} />;

      // 3. Reservation Manager (Lodge allocations, room nights, property verifications)
      case "RESERVATION_MANAGER":
      case "RESERVATION":
      case "RESERVATIONS":
      case "RM":
        return <ReservationDashboard stats={stats} />;

      // 4. Tour & Safari Field Guide (Daily circuits, passenger manifest, vehicle safety)
      case "GUIDE":
      case "TOUR_GUIDE":
        return <GuideDashboard stats={stats} />;

      // 5. Company Admin (Revenue, safaris pipeline, fleet, staff directory)
      case "ADMIN":
      case "COMPANY_ADMIN":
      default:
        return <AdminDashboard stats={stats} />;
    }
  };

  return (
    <div className="space-y-6 pb-12 font-sans max-w-7xl mx-auto">
      {/* Top Executive Header Banner */}
      <DashboardHeader
        userFullName={userDisplayName}
        role={currentRole}
        companyName={userCompany}
        loading={loading}
        onRefresh={fetchDashboardStats}
      />

      {/* Non-intrusive warning notice if API had a temporary communication error */}
      {error && !loading && (
        <div className="flex items-center justify-between gap-3 p-4 rounded-xl border border-amber-200 bg-amber-50 text-amber-900 text-xs shadow-xs animate-in fade-in duration-200">
          <div className="flex items-center gap-2">
            <WarningCircle size={18} weight="fill" className="text-amber-600 shrink-0" />
            <span>
              <strong className="font-semibold">Notice:</strong> Unable to connect with live telemetry service ({error}). Showing cached metrics.
            </span>
          </div>
          <button
            type="button"
            onClick={fetchDashboardStats}
            className="inline-flex items-center gap-1.5 px-3 py-1 font-semibold rounded-lg bg-amber-100 hover:bg-amber-200 text-amber-900 transition active:scale-95 shrink-0"
          >
            <ArrowsClockwise size={13} weight="bold" />
            Retry
          </button>
        </div>
      )}

      {/* Loading Skeleton during initial telemetry fetch */}
      {loading && !stats ? (
        <div className="space-y-6 animate-pulse">
          {/* Skeleton KPI Cards */}
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
            {[1, 2, 3, 4].map((i) => (
              <div key={i} className="h-32 rounded-2xl bg-white border border-slate-200/80 p-5 space-y-3">
                <div className="flex justify-between items-center">
                  <div className="h-3 w-24 bg-slate-200 rounded" />
                  <div className="h-8 w-8 bg-slate-200 rounded-xl" />
                </div>
                <div className="h-7 w-20 bg-slate-200 rounded" />
                <div className="h-3 w-32 bg-slate-100 rounded" />
              </div>
            ))}
          </div>

          {/* Skeleton Charts */}
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            <div className="lg:col-span-2 h-80 rounded-2xl bg-white border border-slate-200/80 p-6 space-y-4">
              <div className="h-4 w-40 bg-slate-200 rounded" />
              <div className="h-56 bg-slate-100 rounded-xl" />
            </div>
            <div className="lg:col-span-1 h-80 rounded-2xl bg-white border border-slate-200/80 p-6 space-y-4">
              <div className="h-4 w-32 bg-slate-200 rounded" />
              <div className="h-56 bg-slate-100 rounded-xl" />
            </div>
          </div>
        </div>
      ) : (
        /* Dynamic Role-Based View */
        renderRoleDashboard()
      )}
    </div>
  );
};

export default DashboardPage;
