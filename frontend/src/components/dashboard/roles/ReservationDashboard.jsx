import { Bed, Buildings, ShieldCheck, Tag, CalendarBlank, CheckSquareOffset } from "@phosphor-icons/react";
import StatCard from "../common/StatCard";
import TrendAreaChart from "../charts/TrendAreaChart";
import DonutStatusChart from "../charts/DonutStatusChart";
import BarMetricChart from "../charts/BarMetricChart";
import QuickActions from "../common/QuickActions";
import RecentActivityTable from "../common/RecentActivityTable";

/**
 * ReservationDashboard Component
 * Tailored for Reservation Managers and Lodging Coordinators.
 * Monitors accommodation requirements across safari itineraries, room night allocations, and property verification status.
 */
const ReservationDashboard = ({ stats }) => {
  const kpis = stats?.kpis || {};
  const trendData = stats?.trendChart || [
    { label: "May", value: 38 },
    { label: "Jun", value: 55 },
    { label: "Jul", value: 72 },
    { label: "Aug", value: 95 },
    { label: "Sep", value: 120 },
    { label: "Oct", value: 148 },
  ];

  const statusDist = stats?.statusDistribution || [
    { label: "Verified Lodges", count: 18, percentage: 72, color: "#059669" },
    { label: "Pending Verification", count: 7, percentage: 28, color: "#D97706" },
  ];

  const categoryBars = [
    { label: "Luxury Tented Camps", value: 42, target: 50, color: "#101B82" },
    { label: "National Park Lodges", value: 68, target: 80, color: "#059669" },
    { label: "Boutique Safari Hotels", value: 25, target: 35, color: "#4F46E5" },
    { label: "Specialty Campsites", value: 13, target: 20, color: "#D97706" },
  ];

  const quickShortcuts = [
    {
      label: "Properties Inventory",
      description: "Manage lodges, camps & verifications",
      to: "/properties",
      icon: <Buildings size={20} weight="duotone" />,
      color: "#101B82",
    },
    {
      label: "Safari Itineraries",
      description: "Assign room tiers to safari days",
      to: "/safaris",
      icon: <CalendarBlank size={20} weight="duotone" />,
      color: "#059669",
    },
    {
      label: "Room Types & Pricing",
      description: "Manage double, twin & family suites",
      to: "/room-types",
      icon: <Bed size={20} weight="duotone" />,
      color: "#4F46E5",
    },
  ];

  return (
    <div className="space-y-6 animate-in fade-in duration-200">
      {/* 1. Reservation & Lodging KPIs */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          title="Lodging Requirements"
          value={Number(kpis.totalRequirements || 0).toLocaleString()}
          subtitle="Itinerary day room requests"
          icon={<Bed size={24} weight="duotone" />}
          trend="+14.0%"
          trendDirection="up"
          iconBg="bg-indigo-50"
          iconColor="text-[#101B82]"
        />

        <StatCard
          title="Partner Properties"
          value={Number(kpis.totalPartnerProperties || 0).toLocaleString()}
          subtitle={`${kpis.verifiedProperties || 0} active & verified`}
          icon={<Buildings size={24} weight="duotone" />}
          trend="+8.2%"
          trendDirection="up"
          iconBg="bg-blue-50"
          iconColor="text-blue-600"
        />

        <StatCard
          title="Verification Queue"
          value={Number(kpis.pendingVerificationQueue || 0).toLocaleString()}
          subtitle="Awaiting compliance sign-off"
          icon={<ShieldCheck size={24} weight="duotone" />}
          trend={kpis.pendingVerificationQueue > 0 ? "Review pending" : "All cleared"}
          trendDirection={kpis.pendingVerificationQueue > 0 ? "down" : "up"}
          iconBg="bg-amber-50"
          iconColor="text-amber-600"
        />

        <StatCard
          title="Allocation Accuracy"
          value={`${kpis.allocationAccuracy || 98.5}%`}
          subtitle="Confirmed room nights ratio"
          icon={<CheckSquareOffset size={24} weight="duotone" />}
          trend="High Standard"
          trendDirection="up"
          iconBg="bg-emerald-50"
          iconColor="text-emerald-600"
        />
      </div>

      {/* 2. Charts: Room Nights Volume & Property Verification */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2">
          <TrendAreaChart
            data={trendData}
            title="Lodge Room Nights Booked"
            subtitle="Monthly allocated room nights across partner properties"
            color="#101B82"
            valueSuffix="nights"
          />
        </div>

        <div className="lg:col-span-1">
          <DonutStatusChart
            data={statusDist}
            title="Property Compliance"
            subtitle="Verification status across partner accommodations"
            centerLabel="Lodges"
          />
        </div>
      </div>

      {/* 3. Category Allocation Bars */}
      <BarMetricChart
        items={categoryBars}
        title="Lodging Category Demand"
        subtitle="Distribution of room requirements across accommodation categories"
      />

      {/* 4. Action Shortcuts */}
      <QuickActions
        actions={quickShortcuts}
        title="Reservation Desk Shortcuts"
      />

      {/* 5. Recent Allocations */}
      <RecentActivityTable
        items={stats?.recentItems || []}
        title="Recent Accommodation Requirements"
        subtitle="Latest lodging requirements configured for active safaris"
        viewAllLink="/safaris"
      />
    </div>
  );
};

export default ReservationDashboard;
