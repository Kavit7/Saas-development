import { useEffect, useState } from "react";
import { NavLink, Outlet } from "react-router-dom";

import { resources } from "../../config/resources";
import Header from "../others/Header";
import { useAuth } from "../../hooks/useAuth";

const Layout = () => {
  const { hasAccess, user } = useAuth();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const role = String(user?.role_name || user?.role || "")
    .toUpperCase()
    .replace(/^ROLE_/, "")
    .trim();
  const isSuperAdmin = role === "SUPER_ADMIN" || role === "PLATFORM_ADMIN";

  useEffect(() => {
    const handleKey = (event) => {
      if (event.key === "Escape") setSidebarOpen(false);
    };
    document.addEventListener("keydown", handleKey);
    return () => document.removeEventListener("keydown", handleKey);
  }, []);

  return (
    <div className="h-screen w-full overflow-hidden bg-white font-serif">
      {sidebarOpen && (
        <div
          onClick={() => setSidebarOpen(false)}
          className="fixed inset-0 z-40 bg-black/50 md:hidden"
          aria-hidden="true"
        />
      )}

      <aside
        className={`fixed inset-y-0 left-0 z-50 flex h-screen w-[270px] shrink-0 transform flex-col overflow-y-auto border-r border-[#211917]/10 bg-[#101B82] px-5 py-6 text-white transition-transform duration-300 md:w-[260px] md:translate-x-0 lg:w-[300px] ${
          sidebarOpen ? "translate-x-0" : "-translate-x-full"
        }`}
      >
        <div className="mb-3 flex items-center justify-between">
          <div className="flex items-center font-serif">
            {isSuperAdmin ? (
              <>
                <span className="mr-2.5 flex h-11 w-11 items-center justify-center rounded-2xl bg-gradient-to-tr from-amber-400 via-amber-200 to-white text-base font-black text-[#101B82] shadow-sm font-sans">
                  PA
                </span>
                <div className="grid">
                  <p className="font-bold tracking-wide text-sm text-white">Platform Admin</p>
                  <p className="text-[11px] uppercase tracking-wider text-indigo-200/80 font-mono">SaaS Control</p>
                </div>
              </>
            ) : (
              <>
                <span className="mr-2 rounded-[15px] bg-white p-1 text-5xl text-black">
                  SS
                </span>
                <div className="grid grid-cols">
                  <p className="font-bold tracking-[1.5px]">Safari Sales</p>
                  <p className="text-sm text-white/70">Operations</p>
                </div>
              </>
            )}
          </div>

          <button
            onClick={() => setSidebarOpen(false)}
            className="flex h-9 w-9 items-center justify-center rounded-full text-white transition hover:bg-white/15 md:hidden"
            aria-label="Close menu"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              className="h-5 w-5"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
              strokeWidth="2"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                d="M6 18L18 6M6 6l12 12"
              />
            </svg>
          </button>
        </div>

        <div className="mt-4 h-px w-full bg-white/15" />

        <div className="mt-4 text-[10px] font-bold uppercase tracking-wider text-indigo-200/60 font-sans px-3">
          {isSuperAdmin ? "Platform Management" : "System Navigation"}
        </div>

        <nav className="mt-2 flex flex-1 flex-col space-y-1.5 font-sans">
          {Object.entries(resources)
            .filter(([, filter]) => hasAccess(filter.roles))
            .map(([key, config]) => (
              <NavLink
                key={key}
                to={`/${key}`}
                onClick={() => setSidebarOpen(false)}
              >
                {({ isActive }) => (
                  <div
                    className={`flex items-center gap-3 rounded-xl px-4 py-2.5 text-xs font-semibold transition ${
                      isActive
                        ? "bg-white text-[#101B82] shadow-sm font-bold"
                        : "text-white/80 hover:bg-white/10 hover:text-white"
                    }`}
                  >
                    <span className="flex items-center justify-center">
                      {config.icon && (
                        <config.icon size={18} aria-hidden="true" />
                      )}
                    </span>
                    <span>{config.label ?? config.title ?? key}</span>
                  </div>
                )}
              </NavLink>
            ))}
        </nav>
      </aside>

      <div className="flex h-full flex-col md:pl-[260px] lg:pl-[300px]">
        <Header onMenuClick={() => setSidebarOpen((prev) => !prev)} />
        <main className="flex-1 overflow-y-auto bg-[#F8F8FC] p-4 sm:p-6">
          <Outlet />
        </main>
      </div>
    </div>
  );
};

export default Layout;
