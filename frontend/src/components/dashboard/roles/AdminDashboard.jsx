import {
  Compass,
  UsersThree,
  CalendarCheck,
  UserGear,
  Buildings,
  ShieldCheck,
  Lifebuoy,
} from "@phosphor-icons/react";
import StatCard from "../common/StatCard";
import TrendAreaChart from "../charts/TrendAreaChart";
import DonutStatusChart from "../charts/DonutStatusChart";
import QuickActions from "../common/QuickActions";
import RecentActivityTable from "../common/RecentActivityTable";

/**
 * AdminDashboard Component
 * Tailored for Company Administrators and Operations Directors.
 * Provides a command center for company safari expeditions, client pipeline, lodge bookings, staff, and audit trails.
 */
const AdminDashboard = ({ stats }) => {
  const kpis = stats?.kpis || {};
  const trendData = stats?.trendChart || [
    { label: "May", value: 14 },
    { label: "Jun", value: 22 },
    { label: "Jul", value: 29 },
    { label: "Aug", value: 38 },
    { label: "Sep", value: 45 },
    { label: "Oct", value: Number(kpis.totalSafaris) || 52 },
  ];

  const statusDist = stats?.statusDistribution || [
    { label: "Confirmed", count: 28, percentage: 54, color: "#059669" },
    { label: "Draft", count: 14, percentage: 27, color: "#D97706" },
    { label: "Completed", count: 7, percentage: 13, color: "#2563EB" },
    { label: "Cancelled", count: 3, percentage: 6, color: "#E11D48" },
  ];

  const quickShortcuts = [
    {
      label: "New Safari Trip",
      description: "Build an itinerary for a client",
      to: "/safaris",
      icon: <Compass size={20} weight="duotone" />,
      color: "#264624",
    },
    {
      label: "Register Client",
      description: "Onboard a new guest profile",
      to: "/clients",
      icon: <UsersThree size={20} weight="duotone" />,
      color: "#059669",
    },
    {
      label: "Company Staff",
      description: "Manage sales, RMs & guides",
      to: "/users",
      icon: <UserGear size={20} weight="duotone" />,
      color: "#7A5229",
    },
    {
      label: "Lodge Bookings",
      description: "Track room reservations",
      to: "/accommodation-bookings",
      icon: <CalendarCheck size={20} weight="duotone" />,
      color: "#D97706",
    },
    {
      label: "Partner Lodges",
      description: "Review properties & price tiers",
      to: "/properties",
      icon: <Buildings size={20} weight="duotone" />,
      color: "#0284C7",
    },
    {
      label: "Support Tickets",
      description: "Review operational issues",
      to: "/support-tickets",
      icon: <Lifebuoy size={20} weight="duotone" />,
      color: "#DC2626",
    },
    {
      label: "Audit Logs",
      description: "Inspect data change history",
      to: "/audit-logs",
      icon: <ShieldCheck size={20} weight="duotone" />,
      color: "#475569",
    },
  ];

  return (
    <div className="space-y-6 animate-in fade-in duration-200">
      {/* 1. Executive KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          title="Total Safaris"
          value={Number(kpis.totalSafaris || 0).toLocaleString()}
          subtitle={`${kpis.confirmedSafaris || 0} confirmed expeditions`}
          icon={<Compass size={24} weight="duotone" />}
          trend={`${kpis.totalSafaris || 0} active`}
          trendDirection="up"
          iconBg="bg-[#264624]/10"
          iconColor="text-[#264624]"
        />

        <StatCard
          title="Lodge Bookings"
          value={Number(kpis.totalBookings || 0).toLocaleString()}
          subtitle="Confirmed & provisional rooms"
          icon={<CalendarCheck size={24} weight="duotone" />}
          trend="Operational"
          trendDirection="up"
          iconBg="bg-emerald-50"
          iconColor="text-emerald-700"
        />

        <StatCard
          title="Total Clients"
          value={Number(kpis.totalClients || 0).toLocaleString()}
          subtitle="Guest database profiles"
          icon={<UsersThree size={24} weight="duotone" />}
          trend="Registered"
          trendDirection="up"
          iconBg="bg-blue-50"
          iconColor="text-blue-600"
        />

        <StatCard
          title="Operations Staff"
          value={Number(kpis.totalStaff || 0).toLocaleString()}
          subtitle="Active sales, RMs & guides"
          icon={<UserGear size={24} weight="duotone" />}
          trend="Company Team"
          trendDirection="up"
          iconBg="bg-amber-50"
          iconColor="text-amber-600"
        />
      </div>

      {/* 2. Charts: Booking Trend & Status Breakdown */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2">
          <TrendAreaChart
            data={trendData}
            title="Safari Bookings Trend"
            subtitle="Monthly expedition reservations over the last 6 months"
            color="#264624"
            valueSuffix="safaris"
          />
        </div>

        <div className="lg:col-span-1">
          <DonutStatusChart
            data={statusDist}
            title="Safari Booking Status"
            subtitle="Current status distribution across active files"
            centerLabel="Safaris"
          />
        </div>
      </div>

      {/* 3. Action Shortcuts */}
      <QuickActions
        actions={quickShortcuts}
        title="Executive Operations Shortcuts"
      />

      {/* 4. Recent Safaris */}
      <RecentActivityTable
        items={stats?.recentItems || []}
        title="Recent Safaris & Expeditions"
        subtitle="Latest safari itineraries in your company"
        viewAllLink="/safaris"
      />
    </div>
  );
};

export default AdminDashboard;
