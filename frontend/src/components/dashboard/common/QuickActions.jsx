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
    <div className="rounded-xl border border-[#28332C]/10 bg-white p-5 font-sans">
      <div className="mb-4">
        <h3 className="text-base font-semibold tracking-tight text-[#28332C]">
          {title}
        </h3>
        <p className="text-xs text-slate-500 mt-0.5">Frequently used operational workflows</p>
      </div>

      <div className="grid grid-cols-1 gap-2 sm:grid-cols-2 lg:grid-cols-3">
        {actions.map((act, idx) => (
          <Link
            key={idx}
            to={act.to}
            className="group flex items-center justify-between rounded-lg border border-[#28332C]/10 px-3 py-3 transition-colors hover:border-[#35634B]/30 hover:bg-[#F8FAF8] focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#35634B]"
          >
            <div className="flex items-center gap-3">
              <div
                className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-[#EAF1EC] text-[#35634B]"
              >
                {act.icon}
              </div>
              <div>
                <h4 className="text-sm font-medium text-[#28332C] group-hover:text-[#35634B] transition-colors">
                  {act.label}
                </h4>
                <p className="mt-0.5 line-clamp-1 text-xs text-slate-500">
                  {act.description}
                </p>
              </div>
            </div>

            <ArrowRight
              size={14}
              weight="bold"
              className="ml-2 shrink-0 text-slate-400 transition-colors group-hover:text-[#35634B]"
            />
          </Link>
        ))}
      </div>
    </div>
  );
};

export default QuickActions;
