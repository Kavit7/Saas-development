import { 
  Compass, 
  MapTrifold, 
  UsersThree, 
  ShieldCheck, 
  Star, 
  CalendarCheck,
  CheckCircle,
  Binoculars
} from "@phosphor-icons/react";
import StatCard from "../common/StatCard";
import TrendAreaChart from "../charts/TrendAreaChart";
import DonutStatusChart from "../charts/DonutStatusChart";
import BarMetricChart from "../charts/BarMetricChart";
import QuickActions from "../common/QuickActions";
import RecentActivityTable from "../common/RecentActivityTable";

/**
 * GuideDashboard Component
 * Tailored specifically for Safari Guides and Tour Leaders in the field.
 * Highlights upcoming expeditions, daily tourist manifests, field days logged, and vehicle safety readiness.
 *
 * @param {Object} props
 * @param {Object} props.stats - Aggregated analytics payload returned by the dashboard API
 */
const GuideDashboard = ({ stats }) => {
  const kpis = stats?.kpis || {};

  // Historical 6-month field expedition activity
  const trendData = stats?.trendChart || [
    { label: "May", value: 12 },
    { label: "Jun", value: 15 },
    { label: "Jul", value: 20 },
    { label: "Aug", value: 18 },
    { label: "Sep", value: 22 },
    { label: "Oct", value: Number(kpis.daysInFieldThisMonth) || 25 },
  ];

  // Dispatch breakdown
  const statusDist = stats?.statusDistribution || [
    { label: "Upcoming Trips", count: Number(kpis.upcomingExpeditions || 4), percentage: 60, color: "#059669" },
    { label: "Completed Safaris", count: 14, percentage: 40, color: "#2563EB" },
  ];

  // Field Readiness & Compliance Benchmarks
  const readinessMetrics = [
    { label: "Vehicle Inspection & Safety Check", value: 100, target: 100, color: "#059669" },
    { label: "Daily Itinerary Briefing Complete", value: 92, target: 100, color: "#101B82" },
    { label: "Guest Manifest & Park Permits Verified", value: 96, target: 100, color: "#D97706" },
  ];

  // Quick shortcuts for active guides
  const guideShortcuts = [
    {
      label: "My Assigned Safaris",
      description: "View upcoming expedition routes and schedules",
      to: "/safaris",
      icon: <Compass size={20} weight="duotone" />,
      color: "#101B82",
    },
    {
      label: "Expedition Itinerary Days",
      description: "Review daily park circuits and camp check-ins",
      to: "/safaris",
      icon: <MapTrifold size={20} weight="duotone" />,
      color: "#059669",
    },
    {
      label: "Guest Manifest",
      description: "Access traveler details and dietary requirements",
      to: "/clients",
      icon: <UsersThree size={20} weight="duotone" />,
      color: "#D97706",
    },
  ];

  // Columns for the upcoming expedition table
  const columns = [
    {
      header: "Expedition / Reference",
      accessor: "title",
      render: (item) => (
        <div className="flex items-center gap-2.5">
          <div className="w-8 h-8 rounded-lg bg-indigo-50 text-[#101B82] flex items-center justify-center font-bold text-xs">
            <Compass size={16} weight="duotone" />
          </div>
          <div>
            <div className="font-semibold text-slate-800 text-sm font-serif-title">{item.title}</div>
            <div className="text-xs text-slate-500">{item.subtitle || "Circuit Tour"}</div>
          </div>
        </div>
      ),
    },
    {
      header: "Departure Date",
      accessor: "date",
      render: (item) => (
        <div className="text-xs font-medium text-slate-600">
          {item.date || "Upcoming"}
        </div>
      ),
    },
    {
      header: "Tourists Guided",
      accessor: "passengers",
      render: (item) => (
        <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-blue-50 text-blue-700">
          <UsersThree size={13} weight="bold" />
          {item.passengers || 2} Pax
        </span>
      ),
    },
    {
      header: "Status",
      accessor: "status",
      render: (item) => {
        const isConfirmed = item.status === "CONFIRMED";
        return (
          <span
            className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-semibold uppercase tracking-wider ${
              isConfirmed ? "bg-emerald-100 text-emerald-800" : "bg-amber-100 text-amber-800"
            }`}
          >
            {item.status || "CONFIRMED"}
          </span>
        );
      },
    },
  ];

  return (
    <div className="space-y-6 animate-in fade-in duration-200">
      {/* 1. Guide Operational KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          title="Assigned Expeditions"
          value={Number(kpis.upcomingExpeditions || 0).toLocaleString()}
          subtitle="Active upcoming circuits"
          icon={<Compass size={24} weight="duotone" />}
          trend="Upcoming"
          trendDirection="up"
          iconBg="bg-indigo-50"
          iconColor="text-[#101B82]"
        />

        <StatCard
          title="Tourists Guided"
          value={Number(kpis.totalTouristsGuided || 0).toLocaleString()}
          subtitle="Current passenger manifest"
          icon={<UsersThree size={24} weight="duotone" />}
          trend="+14.2%"
          trendDirection="up"
          iconBg="bg-blue-50"
          iconColor="text-blue-600"
        />

        <StatCard
          title="Field Days (This Month)"
          value={`${kpis.daysInFieldThisMonth || 18} Days`}
          subtitle="Out on bush expeditions"
          icon={<CalendarCheck size={24} weight="duotone" />}
          trend="+3 days"
          trendDirection="up"
          iconBg="bg-amber-50"
          iconColor="text-amber-600"
        />

        <StatCard
          title="Guide Rating & Safety"
          value={kpis.guideRating ? `${kpis.guideRating} / 5.0` : "4.95 / 5.0"}
          subtitle={kpis.safetyRecord || "100% Incident Free"}
          icon={<ShieldCheck size={24} weight="duotone" />}
          trend="Top Tier"
          trendDirection="up"
          iconBg="bg-emerald-50"
          iconColor="text-emerald-600"
        />
      </div>

      {/* 2. Graphical Analytics: Field Days Trend & Dispatch Status */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2">
          <TrendAreaChart
            title="Field Expedition Days Trend"
            subtitle="Monthly days spent guiding groups through national parks"
            data={trendData}
            dataKey="value"
            color="#101B82"
            valuePrefix=""
          />
        </div>
        <div className="lg:col-span-1">
          <DonutStatusChart
            title="Expedition Dispatch"
            subtitle="Upcoming vs completed tours"
            data={statusDist}
            totalLabel="Total Trips"
          />
        </div>
      </div>

      {/* 3. Field Readiness & Safety Checklist */}
      <BarMetricChart
        title="Expedition Readiness & Safety Compliance"
        subtitle="Standard operating procedure completion rate before vehicle departure"
        metrics={readinessMetrics}
      />

      {/* 4. Action Shortcuts & Upcoming Expedition Table */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-1">
          <QuickActions
            title="Field Guide Actions"
            subtitle="Daily operational tools"
            actions={guideShortcuts}
          />
        </div>
        <div className="lg:col-span-2">
          <RecentActivityTable
            title="Upcoming Safari Expeditions"
            subtitle="Next scheduled group tours, destinations, and client groups"
            data={stats?.recentItems || []}
            columns={columns}
            emptyMessage="No upcoming expeditions scheduled at the moment."
          />
        </div>
      </div>
    </div>
  );
};

export default GuideDashboard;
