import { Compass, UsersThree, Target, UserCheck, CurrencyDollar, Plus } from "@phosphor-icons/react";
import StatCard from "../common/StatCard";
import TrendAreaChart from "../charts/TrendAreaChart";
import DonutStatusChart from "../charts/DonutStatusChart";
import BarMetricChart from "../charts/BarMetricChart";
import QuickActions from "../common/QuickActions";
import RecentActivityTable from "../common/RecentActivityTable";

/**
 * SalesDashboard Component
 * Tailored for Sales Consultants and Travel Designers.
 * Focuses on personal sales pipeline, individual conversion rates, client portfolios, and monthly target quotas.
 */
const SalesDashboard = ({ stats }) => {
  const kpis = stats?.kpis || {};
  const trendData = stats?.trendChart || [
    { label: "May", value: 5 },
    { label: "Jun", value: 9 },
    { label: "Jul", value: 14 },
    { label: "Aug", value: 18 },
    { label: "Sep", value: 23 },
    { label: "Oct", value: Number(kpis.mySafaris) || 28 },
  ];

  const statusDist = stats?.statusDistribution || [
    { label: "Confirmed Deals", count: 18, percentage: 64, color: "#059669" },
    { label: "Active Quotes (Draft)", count: 7, percentage: 25, color: "#D97706" },
    { label: "Completed Safaris", count: 3, percentage: 11, color: "#2563EB" },
  ];

  const targetBars = [
    { label: "Monthly Safari Target", value: Number(kpis.confirmedBookings || 14), target: 20, color: "#101B82" },
    { label: "Client Onboarding Quota", value: Number(kpis.myClients || 18), target: 25, color: "#059669" },
    { label: "Passengers Booked", value: Number(kpis.totalPassengersBooked || 34), target: 45, color: "#4F46E5" },
  ];

  const quickShortcuts = [
    {
      label: "Create Safari Quote",
      description: "Draft a new safari for an inquiry",
      to: "/safaris",
      icon: <Compass size={20} weight="duotone" />,
      color: "#101B82",
    },
    {
      label: "Add New Client",
      description: "Record a new prospect or traveler",
      to: "/clients",
      icon: <UsersThree size={20} weight="duotone" />,
      color: "#059669",
    },
    {
      label: "Browse Lodges",
      description: "Inspect partner accommodation tiers",
      to: "/properties",
      icon: <Target size={20} weight="duotone" />,
      color: "#D97706",
    },
  ];

  return (
    <div className="space-y-6 animate-in fade-in duration-200">
      {/* 1. Sales KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          title="My Safaris"
          value={Number(kpis.mySafaris || 0).toLocaleString()}
          subtitle={`${kpis.confirmedBookings || 0} confirmed files`}
          icon={<Compass size={24} weight="duotone" />}
          trend="+18.2%"
          trendDirection="up"
          iconBg="bg-indigo-50"
          iconColor="text-[#101B82]"
        />

        <StatCard
          title="My Direct Clients"
          value={Number(kpis.myClients || 0).toLocaleString()}
          subtitle="Assigned guest profiles"
          icon={<UsersThree size={24} weight="duotone" />}
          trend="+10.5%"
          trendDirection="up"
          iconBg="bg-blue-50"
          iconColor="text-blue-600"
        />

        <StatCard
          title="Conversion Rate"
          value={`${kpis.conversionRate || 78.5}%`}
          subtitle="Inquiry to confirmed ratio"
          icon={<UserCheck size={24} weight="duotone" />}
          trend="+4.8%"
          trendDirection="up"
          iconBg="bg-emerald-50"
          iconColor="text-emerald-600"
        />

        <StatCard
          title="Pipeline Value"
          value={kpis.salesPipelineValue ? `$${Number(kpis.salesPipelineValue).toLocaleString()}` : "$58,400"}
          subtitle="Active closed & pending deals"
          icon={<CurrencyDollar size={24} weight="duotone" />}
          trend="85% of Target"
          trendDirection="up"
          iconBg="bg-amber-50"
          iconColor="text-amber-600"
        />
      </div>

      {/* 2. Charts: Personal Sales Trend & Target Tracking */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2">
          <TrendAreaChart
            data={trendData}
            title="My Booking Performance"
            subtitle="Monthly closed safari files over the last 6 months"
            color="#101B82"
            valueSuffix="safaris"
          />
        </div>

        <div className="lg:col-span-1">
          <DonutStatusChart
            data={statusDist}
            title="Quote & Deal Status"
            subtitle="Current status of your assigned files"
            centerLabel="Deals"
          />
        </div>
      </div>

      {/* 3. Monthly Benchmark Progress Bars */}
      <BarMetricChart
        items={targetBars}
        title="Monthly Quota & Targets"
        subtitle="Tracking your individual sales deliverables for this month"
      />

      {/* 4. Action Shortcuts */}
      <QuickActions
        actions={quickShortcuts}
        title="Sales Operations Shortcuts"
      />

      {/* 5. Recent Quotes & Bookings */}
      <RecentActivityTable
        items={stats?.recentItems || []}
        title="My Recent Safari Quotes"
        subtitle="Latest expeditions and itineraries managed by you"
        viewAllLink="/safaris"
      />
    </div>
  );
};

export default SalesDashboard;
