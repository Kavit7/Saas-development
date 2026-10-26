import { ArrowUpRight, ArrowDownRight } from "@phosphor-icons/react";

/**
 * StatCard Component
 * High-impact executive KPI card featuring an icon, formatted value,
 * title, trend indicator badge, and luxury serif typography.
 *
 * @param {string} title - Label describing the KPI
 * @param {string|number} value - The primary stat number/string
 * @param {string} subtitle - Explanatory subtext (e.g. "vs last month")
 * @param {React.ReactNode} icon - Phosphor Icon component
 * @param {string} trend - Optional trend text (e.g. "+14.2%", "-3.1%")
 * @param {"up"|"down"|"neutral"} trendDirection - Trend direction for styling
 * @param {string} iconBg - Tailwind bg class for the icon container
 * @param {string} iconColor - Tailwind text class for the icon
 */
const StatCard = ({
  title,
  value,
  subtitle,
  icon,
  trend,
  trendDirection = "up",
}) => {
  const isPositive = trendDirection === "up";

  return (
    <div className="rounded-xl border border-[#28332C]/10 bg-white p-5 transition-shadow hover:shadow-sm">
      <div className="flex items-start justify-between gap-3">
        <div className="space-y-1">
          <span className="text-xs font-medium text-slate-500">
            {title}
          </span>
          <div className="text-2xl font-semibold tracking-tight text-[#28332C]">
            {value}
          </div>
        </div>

        <div
          className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-[#EAF1EC] text-[#35634B]"
        >
          {icon}
        </div>
      </div>

      {(subtitle || trend) && (
        <div className="mt-3 flex min-h-5 items-center gap-2 text-xs">
          {trend && (
            <span
              className={`inline-flex items-center gap-0.5 rounded-md px-1.5 py-0.5 font-medium ${
                isPositive
                  ? "bg-emerald-50 text-emerald-700"
                  : "bg-rose-50 text-rose-700"
              }`}
            >
              {isPositive ? <ArrowUpRight size={12} /> : <ArrowDownRight size={12} />}
              <span>{trend}</span>
            </span>
          )}
          {subtitle && <span className="truncate text-slate-500">{subtitle}</span>}
        </div>
      )}
    </div>
  );
};

export default StatCard;
