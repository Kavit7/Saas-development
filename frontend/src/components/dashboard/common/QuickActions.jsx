import { Link } from "react-router-dom";
import { ArrowRight } from "@phosphor-icons/react";

/**
 * QuickActions Component
 * Provides direct one-click workflow shortcuts tailored to the active role.
 *
 * @param {Array<{label: string, description: string, to: string, icon: React.ReactNode, color: string}>} actions - List of shortcuts
 * @param {string} title - Section heading
 */
const QuickActions = ({
  actions = [],
  title = "Quick Operations",
}) => {
  if (!actions || actions.length === 0) return null;

  return (
    <div className="rounded-2xl border border-slate-200/90 bg-white p-5 sm:p-6 shadow-xs font-sans">
      <div className="mb-4">
        <h3 className="text-base sm:text-lg font-bold text-slate-900 font-serif-title tracking-tight">
          {title}
        </h3>
        <p className="text-xs text-slate-500 mt-0.5">Frequently used operational workflows</p>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
        {actions.map((act, idx) => (
          <Link
            key={idx}
            to={act.to}
            className="flex items-center justify-between p-3.5 rounded-xl border border-slate-200/80 bg-slate-50/50 hover:bg-white hover:border-[#101B82]/30 hover:shadow-xs transition-all group"
          >
            <div className="flex items-center gap-3">
              <div
                className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-white border border-slate-200/90 shadow-2xs group-hover:scale-105 transition-transform"
                style={{ color: act.color || "#101B82" }}
              >
                {act.icon}
              </div>
              <div>
                <h4 className="text-xs font-bold text-slate-900 group-hover:text-[#101B82] transition-colors">
                  {act.label}
                </h4>
                <p className="text-[11px] text-slate-500 mt-0.5 line-clamp-1">
                  {act.description}
                </p>
              </div>
            </div>

            <ArrowRight
              size={14}
              weight="bold"
              className="text-slate-400 group-hover:text-[#101B82] group-hover:translate-x-0.5 transition-all shrink-0 ml-2"
            />
          </Link>
        ))}
      </div>
    </div>
  );
};

export default QuickActions;
