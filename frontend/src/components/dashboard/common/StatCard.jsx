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
  iconBg = "bg-indigo-50",
  iconColor = "text-[#101B82]",
}) => {
  const isPositive = trendDirection === "up";

  return (
    <div className="relative overflow-hidden rounded-2xl border border-slate-200/90 bg-white p-5 sm:p-6 shadow-xs transition-all hover:shadow-md hover:border-slate-300 group">
      {/* Decorative top accent line */}
      <div className="absolute top-0 inset-x-0 h-1 bg-gradient-to-r from-transparent via-slate-100 to-transparent group-hover:via-indigo-500 transition-all duration-300" />

      <div className="flex items-start justify-between gap-3">
        <div className="space-y-1">
          <span className="text-xs font-semibold uppercase tracking-wider text-slate-500">
            {title}
          </span>
          <div className="text-2xl sm:text-3xl font-extrabold text-slate-900 font-serif-title tracking-tight">
            {value}
          </div>
        </div>

        {/* Icon Badge */}
        <div
          className={`flex h-12 w-12 shrink-0 items-center justify-center rounded-2xl border border-slate-100 ${iconBg} ${iconColor} transition-transform group-hover:scale-105 shadow-2xs`}
        >
          {icon}
        </div>
      </div>

      {/* Subtitle & Trend footer */}
      {(subtitle || trend) && (
        <div className="mt-4 flex items-center gap-2 pt-3 border-t border-slate-100/80 text-xs">
          {trend && (
            <span
              className={`inline-flex items-center gap-0.5 rounded-full px-2 py-0.5 font-bold ${
                isPositive
                  ? "bg-emerald-50 text-emerald-700 border border-emerald-200/80"
                  : "bg-rose-50 text-rose-700 border border-rose-200/80"
              }`}
            >
              {isPositive ? <ArrowUpRight size={12} weight="bold" /> : <ArrowDownRight size={12} weight="bold" />}
              <span>{trend}</span>
            </span>
          )}
          {subtitle && <span className="text-slate-500 truncate">{subtitle}</span>}
        </div>
      )}
    </div>
  );
};

export default StatCard;
