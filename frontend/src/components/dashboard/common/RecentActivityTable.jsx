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
  return "bg-[#EAF1EC] text-[#315B43] border-[#35634B]/15";
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
    <div className="overflow-hidden rounded-xl border border-[#28332C]/10 bg-white font-sans">
      <div className="flex flex-col justify-between gap-2 border-b border-[#28332C]/10 px-5 py-4 sm:flex-row sm:items-center">
        <div>
          <h3 className="text-base font-semibold tracking-tight text-[#28332C]">
            {title}
          </h3>
          <p className="text-xs text-slate-500 mt-0.5">{subtitle}</p>
        </div>

        {viewAllLink && (
          <Link
            to={viewAllLink}
            className="inline-flex items-center gap-1.5 text-xs font-medium text-[#35634B] hover:underline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#35634B]"
          >
            <span>View All Records</span>
            <ArrowRight size={13} weight="bold" />
          </Link>
        )}
      </div>

      {!items || items.length === 0 ? (
        <div className="p-8 text-center text-sm text-slate-500">
          No recent activity recorded yet
        </div>
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full min-w-[640px] text-left text-sm">
            <thead className="bg-[#F7F8F6]">
              <tr className="border-b border-[#28332C]/10 text-[11px] font-medium text-slate-500">
                <th className="px-5 py-3 font-medium">Details / Reference</th>
                <th className="px-5 py-3 font-medium">Entity / Client</th>
                <th className="px-5 py-3 font-medium">Date</th>
                <th className="px-5 py-3 text-right font-medium">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[#28332C]/[0.06]">
              {items.map((row, idx) => (
                <tr key={idx} className="transition-colors hover:bg-[#F8FAF8]">
                  <td className="px-5 py-3.5 font-medium text-[#28332C]">
                    {row.title}
                  </td>
                  <td className="max-w-[180px] truncate px-5 py-3.5 text-slate-600">
                    {row.subtitle}
                  </td>
                  <td className="px-5 py-3.5 text-xs text-slate-500">
                    <div className="flex items-center gap-1">
                      <Clock size={13} className="text-slate-400" />
                      <span>{row.date}</span>
                    </div>
                  </td>
                  <td className="px-5 py-3.5 text-right">
                    <span
                      className={`inline-flex items-center rounded-md border px-2 py-1 text-[11px] font-medium ${getStatusBadge(
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
