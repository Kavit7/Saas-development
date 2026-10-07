import { ArrowsClockwise, ShieldCheck, Sparkle } from "@phosphor-icons/react";

/**
 * DashboardHeader Component
 * Executive greeting banner displaying the user's role badge, company name,
 * formatted date, and a manual refresh trigger.
 *
 * @param {string} userFullName - User display name
 * @param {string} role - Normalized role key
 * @param {string} companyName - Organization title
 * @param {boolean} loading - Loading state for refresh button
 * @param {Function} onRefresh - Callback to reload statistics
 */
const DashboardHeader = ({
  userFullName,
  role,
  companyName,
  loading = false,
  onRefresh,
}) => {
  const getRoleDisplayName = (r) => {
    switch (String(r || "").toUpperCase().replace("ROLE_", "")) {
      case "SUPER_ADMIN":
        return "Platform Super Admin";
      case "ADMIN":
        return "Company Admin";
      case "SALES_PERSON":
      case "SALE":
        return "Senior Sales Consultant";
      case "RESERVATION_MANAGER":
      case "RM":
        return "Head of Reservations";
      case "GUIDE":
        return "Senior Safari Guide";
      default:
        return "Executive Member";
    }
  };

  const todayStr = new Intl.DateTimeFormat("en-US", {
    weekday: "long",
    month: "long",
    day: "numeric",
    year: "numeric",
  }).format(new Date());

  return (
    <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 rounded-2xl border border-slate-200/90 bg-white p-5 sm:p-6 shadow-xs font-sans">
      <div className="space-y-1.5">
        <div className="flex flex-wrap items-center gap-2.5">
          <h1 className="text-xl sm:text-2xl font-bold text-slate-900 tracking-tight font-serif-title">
            Welcome back, {userFullName || "Colleague"}
          </h1>
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[11px] font-bold tracking-wide uppercase bg-[#264624]/10 text-[#264624] border border-[#264624]/20 shadow-2xs">
            <ShieldCheck size={13} weight="bold" />
            <span>{getRoleDisplayName(role)}</span>
          </span>
        </div>

        <div className="flex flex-wrap items-center gap-2 text-xs text-slate-500 font-medium">
          <span className="text-[#264624] font-semibold">{companyName || "Safari Global Operations"}</span>
          <span className="text-slate-300">•</span>
          <span>{todayStr}</span>
          <span className="text-slate-300">•</span>
          <span className="inline-flex items-center gap-1 text-emerald-600 font-semibold">
            <Sparkle size={12} weight="fill" />
            <span>System Live</span>
          </span>
        </div>
      </div>

      <div className="flex items-center gap-3">
        <button
          type="button"
          onClick={onRefresh}
          disabled={loading}
          className="inline-flex items-center gap-2 rounded-xl border border-slate-200 bg-slate-50/80 px-3.5 py-2 text-xs font-semibold text-slate-700 hover:border-slate-300 hover:bg-slate-100 hover:text-[#264624] transition active:scale-95 shadow-2xs disabled:opacity-50"
        >
          <ArrowsClockwise
            size={15}
            weight="bold"
            className={`${loading ? "animate-spin text-[#264624]" : ""}`}
          />
          <span>{loading ? "Refreshing..." : "Refresh Data"}</span>
        </button>
      </div>
    </div>
  );
};

export default DashboardHeader;
