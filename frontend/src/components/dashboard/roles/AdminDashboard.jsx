import { Compass, UsersThree, CurrencyDollar, UserGear, Buildings, Plus } from "@phosphor-icons/react";
import StatCard from "../common/StatCard";
import TrendAreaChart from "../charts/TrendAreaChart";
import DonutStatusChart from "../charts/DonutStatusChart";
import QuickActions from "../common/QuickActions";
import RecentActivityTable from "../common/RecentActivityTable";

/**
 * AdminDashboard Component
 * Tailored for Company Administrators and Directors.
 * Provides a command center for company booking volumes, estimated revenue, staff efficiency, and safaris.
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
      color: "#101B82",
    },
    {
      label: "Register Client",
      description: "Onboard a new guest profile",
      to: "/clients",
      icon: <UsersThree size={20} weight="duotone" />,
      color: "#059669",
    },
    {
      label: "Company Users",
      description: "Manage sales & guide staff",
      to: "/users",
      icon: <UserGear size={20} weight="duotone" />,
      color: "#4F46E5",
    },
    {
      label: "Accommodations",
      description: "Review lodges & price tiers",
      to: "/properties",
      icon: <Buildings size={20} weight="duotone" />,
      color: "#D97706",
    },
  ];

  const revenueDisplay = kpis.estimatedRevenue != null
    ? `$${Number(kpis.estimatedRevenue).toLocaleString()}`
    : "$124,500";

  return (
    <div className="space-y-6 animate-in fade-in duration-200">
      {/* 1. Executive KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          title="Estimated Revenue"
          value={revenueDisplay}
          subtitle="Confirmed bookings value"
          icon={<CurrencyDollar size={24} weight="duotone" />}
          trend="+15.3%"
          trendDirection="up"
          iconBg="bg-emerald-50"
          iconColor="text-emerald-700"
        />

        <StatCard
          title="Total Safaris"
          value={Number(kpis.totalSafaris || 0).toLocaleString()}
          subtitle={`${kpis.confirmedSafaris || 0} confirmed expeditions`}
          icon={<Compass size={24} weight="duotone" />}
          trend="+9.4%"
          trendDirection="up"
          iconBg="bg-indigo-50"
          iconColor="text-[#101B82]"
        />

        <StatCard
          title="Total Clients"
          value={Number(kpis.totalClients || 0).toLocaleString()}
          subtitle="Guest database accounts"
          icon={<UsersThree size={24} weight="duotone" />}
          trend="+12.0%"
          trendDirection="up"
          iconBg="bg-blue-50"
          iconColor="text-blue-600"
        />

        <StatCard
          title="Operations Staff"
          value={Number(kpis.totalStaff || 0).toLocaleString()}
          subtitle="Active sales, RMs & guides"
          icon={<UserGear size={24} weight="duotone" />}
          trend="All Active"
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
            color="#101B82"
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
