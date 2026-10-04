import { Buildings, UsersThree, Compass, ShieldCheck, Briefcase, Plus, Tag } from "@phosphor-icons/react";
import StatCard from "../common/StatCard";
import TrendAreaChart from "../charts/TrendAreaChart";
import DonutStatusChart from "../charts/DonutStatusChart";
import QuickActions from "../common/QuickActions";
import RecentActivityTable from "../common/RecentActivityTable";

/**
 * SuperAdminDashboard Component
 * Tailored for Platform Administrators.
 * Monitors tenant company scale, subscription distributions, system-wide safaris, and verification compliance.
 */
const SuperAdminDashboard = ({ stats }) => {
  const kpis = stats?.kpis || {};
  const trendData = stats?.trendChart || [
    { label: "May", value: 6 },
    { label: "Jun", value: 11 },
    { label: "Jul", value: 17 },
    { label: "Aug", value: 24 },
    { label: "Sep", value: 31 },
    { label: "Oct", value: Number(kpis.totalCompanies) || 38 },
  ];

  const planDist = stats?.statusDistribution || [
    { label: "Enterprise Tier", count: 18, percentage: 48, color: "#101B82" },
    { label: "Professional Tier", count: 13, percentage: 34, color: "#4F46E5" },
    { label: "Starter Tier", count: 7, percentage: 18, color: "#06B6D4" },
  ];

  const quickShortcuts = [
    {
      label: "Register Company",
      description: "Provision a new tenant organization",
      to: "/companies",
      icon: <Buildings size={20} weight="duotone" />,
      color: "#101B82",
    },
    {
      label: "System Properties",
      description: "Review & verify partner properties",
      to: "/properties",
      icon: <ShieldCheck size={20} weight="duotone" />,
      color: "#059669",
    },
    {
      label: "User Accounts",
      description: "Audit platform and company staff",
      to: "/users",
      icon: <UsersThree size={20} weight="duotone" />,
      color: "#4F46E5",
    },
  ];

  return (
    <div className="space-y-6 animate-in fade-in duration-200">
      {/* 1. Primary Stat Cards Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          title="Tenant Companies"
          value={Number(kpis.totalCompanies || 0).toLocaleString()}
          subtitle="Active SaaS operators"
          icon={<Buildings size={24} weight="duotone" />}
          trend="+18.5%"
          trendDirection="up"
          iconBg="bg-indigo-50"
          iconColor="text-[#101B82]"
        />

        <StatCard
          title="Platform Users"
          value={Number(kpis.totalUsers || 0).toLocaleString()}
          subtitle="Across all companies"
          icon={<UsersThree size={24} weight="duotone" />}
          trend="+24.0%"
          trendDirection="up"
          iconBg="bg-blue-50"
          iconColor="text-blue-600"
        />

        <StatCard
          title="Global Safaris"
          value={Number(kpis.totalSafaris || 0).toLocaleString()}
          subtitle="Tracked reservations"
          icon={<Compass size={24} weight="duotone" />}
          trend="+12.8%"
          trendDirection="up"
          iconBg="bg-emerald-50"
          iconColor="text-emerald-600"
        />

        <StatCard
          title="Pending Verifications"
          value={Number(kpis.pendingVerifications || 0).toLocaleString()}
          subtitle="Properties awaiting check"
          icon={<ShieldCheck size={24} weight="duotone" />}
          trend={kpis.pendingVerifications > 0 ? "Action required" : "All verified"}
          trendDirection={kpis.pendingVerifications > 0 ? "down" : "up"}
          iconBg="bg-amber-50"
          iconColor="text-amber-600"
        />
      </div>

      {/* 2. Charts Section: Tenant Growth & Subscription Distribution */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2">
          <TrendAreaChart
            data={trendData}
            title="Tenant Onboarding Trend"
            subtitle="Growth in registered tour operator companies over the last 6 months"
            color="#101B82"
            valueSuffix="companies"
          />
        </div>

        <div className="lg:col-span-1">
          <DonutStatusChart
            data={planDist}
            title="Subscription Breakdown"
            subtitle="Distribution by active pricing tier"
            centerLabel="Tenants"
          />
        </div>
      </div>

      {/* 3. Quick Actions Grid */}
      <QuickActions
        actions={quickShortcuts}
        title="Platform Administration Shortcuts"
      />

      {/* 4. Recent Registered Companies */}
      <RecentActivityTable
        items={stats?.recentItems || []}
        title="Recently Onboarded Companies"
        subtitle="Latest operator accounts created on the platform"
        viewAllLink="/companies"
      />
    </div>
  );
};

export default SuperAdminDashboard;
