import { useEffect, useState } from "react";
import { NavLink, Outlet, useNavigate } from "react-router-dom";

import { resources } from "../../config/resources";
import Header from "../others/Header";
import { useAuth } from "../../hooks/useAuth";
import ChangePasswordModal from "../forms/ChangePasswordModal";
import { WarningCircle, Key, Eye, SignOut } from "@phosphor-icons/react";

const Layout = () => {
  const { hasAccess, user, isImpersonating, stopImpersonation } = useAuth();
  const navigate = useNavigate();
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [passwordModalOpen, setPasswordModalOpen] = useState(false);

  const handleExitImpersonation = () => {
    stopImpersonation();
    navigate("/dashboard");
  };

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
    <div className="h-screen w-full overflow-hidden bg-[#F4F6F3] font-sans">
      {sidebarOpen && (
        <div
          onClick={() => setSidebarOpen(false)}
          className="fixed inset-0 z-40 bg-[#17221B]/35 md:hidden"
          aria-hidden="true"
        />
      )}

      <aside
        className={`fixed inset-y-0 left-0 z-50 flex h-screen w-[270px] shrink-0 transform flex-col overflow-y-auto border-r border-[#28332C]/10 bg-white px-5 py-6 text-[#28332C] transition-transform duration-300 md:w-[260px] md:translate-x-0 lg:w-[300px] ${
          sidebarOpen ? "translate-x-0" : "-translate-x-full"
        }`}
      >
        <div className="mb-3 flex items-center justify-between">
          <div className="flex items-center">
            {isSuperAdmin ? (
              <>
                <span className="mr-2.5 flex h-11 w-11 items-center justify-center rounded-xl bg-[#EAF1EC] text-base font-bold text-[#35634B] font-sans">
                  PA
                </span>
                <div className="grid">
                  <p className="font-semibold text-sm text-[#28332C] font-title">Platform Admin</p>
                  <p className="text-[11px] text-[#68736B] font-sans">SaaS Control</p>
                </div>
              </>
            ) : (
              <>
                <span className="mr-2 flex h-11 w-11 items-center justify-center rounded-xl bg-[#EAF1EC] text-lg font-bold text-[#35634B]">
                  SS
                </span>
                <div className="grid grid-cols">
                  <p className="font-semibold font-title">Safari Sales</p>
                  <p className="text-xs text-[#68736B]">Operations</p>
                </div>
              </>
            )}
          </div>

          <button
            onClick={() => setSidebarOpen(false)}
            className="flex h-9 w-9 items-center justify-center rounded-lg text-[#68736B] transition hover:bg-[#F2F4F0] md:hidden"
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

        <div className="mt-4 h-px w-full bg-[#28332C]/10" />

        <div className="mt-4 px-3 text-[11px] font-semibold text-[#778078] font-sans">
          {isSuperAdmin ? "Platform Management" : "System Navigation"}
        </div>

        <nav className="mt-2 flex flex-1 flex-col space-y-1 font-sans">
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
                    className={`flex items-center gap-3 rounded-lg px-3.5 py-2.5 text-sm font-medium transition ${
                      isActive
                        ? "bg-[#EAF1EC] text-[#315B43] font-semibold"
                        : "text-[#5E6961] hover:bg-[#F2F4F0] hover:text-[#28332C]"
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
        {isImpersonating && (
          <div className="sticky top-0 z-40 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 border-b border-amber-200 bg-amber-50 px-4 py-2.5 text-amber-950 shrink-0">
            <div className="flex items-center gap-2.5">
              <span className="flex h-7 w-7 shrink-0 items-center justify-center rounded-lg bg-amber-100 text-amber-800">
                <Eye size={18} weight="bold" />
              </span>
              <div className="text-xs sm:text-sm">
                <span className="font-semibold">Personalize / Impersonation Mode: </span>
                <span>
                  Umeingia kama <strong>{user?.firstName} {user?.lastName}</strong> ({user?.role_name}) wa kampuni <strong>{user?.companyName}</strong>.
                </span>
              </div>
            </div>
            <button
              type="button"
              onClick={handleExitImpersonation}
              className="inline-flex items-center gap-1.5 rounded-lg bg-amber-900 px-3.5 py-1.5 text-xs font-semibold text-white transition hover:bg-amber-950 cursor-pointer self-start sm:self-auto shrink-0"
            >
              <SignOut size={15} weight="bold" />
              <span>Toka (Rudi Super Admin)</span>
            </button>
          </div>
        )}
        <Header onMenuClick={() => setSidebarOpen((prev) => !prev)} />
        <main className="flex-1 overflow-y-auto bg-[#F4F6F3] p-4 sm:p-6">
          {user?.mustChangePassword && (
            <div className="mb-4 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 rounded-2xl border border-amber-300 bg-amber-50 p-4 text-xs shadow-xs">
              <div className="flex items-center gap-3">
                <span className="flex h-8 w-8 shrink-0 items-center justify-center rounded-xl bg-amber-200 text-amber-900">
                  <WarningCircle size={20} weight="fill" />
                </span>
                <div>
                  <p className="font-bold text-amber-950">
                    Unatumia Nenosiri la Muda (Temporary Password)
                  </p>
                  <p className="text-amber-800/80">
                    Nenosiri hili (jina lako la mwisho) linaisha muda wake ndani ya siku 3. Tafadhali weka nenosiri lako la siri sasa ili kuzuia akaunti kufungwa.
                  </p>
                </div>
              </div>
              <button
                onClick={() => setPasswordModalOpen(true)}
                className="inline-flex items-center gap-1.5 rounded-xl bg-amber-800 px-4 py-2 font-bold text-white shadow-xs transition hover:bg-amber-900 shrink-0"
              >
                <Key size={14} weight="bold" />
                Weka Nenosiri Jipya
              </button>
            </div>
          )}
          <Outlet />
        </main>
        <ChangePasswordModal
          isOpen={passwordModalOpen}
          onClose={() => setPasswordModalOpen(false)}
        />
      </div>
    </div>
  );
};

export default Layout;
