import { Link } from "react-router-dom";
import { ArrowRight, Clock } from "@phosphor-icons/react";

const getStatusBadge = (status) => {
  const s = String(status || "").toUpperCase();
  if (s.includes("CONFIRM") || s.includes("ACTIVE") || s.includes("VERIF") || s.includes("ENTERPRISE")) {
    return "bg-emerald-50 text-emerald-700 border-emerald-200/90";
  }
  if (s.includes("DRAFT") || s.includes("PENDING") || s.includes("PRO")) {
    return "bg-amber-50 text-amber-700 border-amber-200/90";
  }
  if (s.includes("CANCEL") || s.includes("INACTIVE") || s.includes("REJECT")) {
    return "bg-rose-50 text-rose-700 border-rose-200/90";
  }
  return "bg-indigo-50 text-[#101B82] border-indigo-200/90";
};

/**
 * RecentActivityTable Component
 * Shows recent operations (safaris, tenants, requirements, or clients) with status badges and links.
 *
 * @param {Array<{title: string, subtitle: string, date: string, status: string, passengers?: number}>} items - Records list
 * @param {string} title - Table section heading
 * @param {string} subtitle - Explanatory caption
 * @param {string} viewAllLink - Optional route link to see all records (e.g. "/safaris")
 */
const RecentActivityTable = ({
  items = [],
  title = "Recent Activity",
  subtitle = "Latest operational updates",
  viewAllLink,
}) => {
  return (
    <div className="rounded-2xl border border-slate-200/90 bg-white p-5 sm:p-6 shadow-xs font-sans">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 mb-4">
        <div>
          <h3 className="text-base sm:text-lg font-bold text-slate-900 font-serif-title tracking-tight">
            {title}
          </h3>
          <p className="text-xs text-slate-500 mt-0.5">{subtitle}</p>
        </div>

        {viewAllLink && (
          <Link
            to={viewAllLink}
            className="inline-flex items-center gap-1.5 text-xs font-semibold text-[#101B82] hover:text-[#0c145e] hover:underline"
          >
            <span>View All Records</span>
            <ArrowRight size={13} weight="bold" />
          </Link>
        )}
      </div>

      {!items || items.length === 0 ? (
        <div className="p-8 text-center text-slate-400 text-xs">
          No recent activity recorded yet
        </div>
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead>
              <tr className="border-b border-slate-100 text-slate-400 font-bold uppercase tracking-wider text-[10px]">
                <th className="pb-3 font-semibold">Details / Reference</th>
                <th className="pb-3 font-semibold">Entity / Client</th>
                <th className="pb-3 font-semibold">Date</th>
                <th className="pb-3 font-semibold text-right">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100/80">
              {items.map((row, idx) => (
                <tr key={idx} className="hover:bg-slate-50/50 transition">
                  <td className="py-3 font-bold text-slate-900 font-serif-title">
                    {row.title}
                  </td>
                  <td className="py-3 text-slate-600 font-medium truncate max-w-[180px]">
                    {row.subtitle}
                  </td>
                  <td className="py-3 text-slate-400 font-mono text-[11px]">
                    <div className="flex items-center gap-1">
                      <Clock size={12} className="text-slate-400" />
                      <span>{row.date}</span>
                    </div>
                  </td>
                  <td className="py-3 text-right">
                    <span
                      className={`inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold uppercase tracking-wider border shadow-2xs ${getStatusBadge(
                        row.status
                      )}`}
                    >
                      {row.status}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};

export default RecentActivityTable;
