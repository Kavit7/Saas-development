import { useEffect, useState } from "react";
import { NavLink, Outlet } from "react-router-dom";
import { resources } from "../../config/resources";
import { useAuth } from "../../context/AuthProvider";
import Header from "../others/Header";

const Layout = () => {
  const { hasAccess } = useAuth();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  // close sidebar with the Escape key
  useEffect(() => {
    const handleKey = (e) => {
      if (e.key === "Escape") setSidebarOpen(false);
    };
    document.addEventListener("keydown", handleKey);
    return () => document.removeEventListener("keydown", handleKey);
  }, []);

  return (
    <div className="h-screen w-full overflow-hidden bg-white font-serif">
      {/* mobile overlay */}
      {sidebarOpen && (
        <div
          onClick={() => setSidebarOpen(false)}
          className="fixed inset-0 z-40 bg-black/50 md:hidden"
          aria-hidden="true"
        />
      )}

      {/* sidebar */}
      <aside
        className={`fixed inset-y-0 left-0 z-50 flex h-screen w-[270px] shrink-0 transform flex-col overflow-y-auto border-r border-[#211917]/10 bg-[#101B82] px-5 py-6 text-white transition-transform duration-300 md:w-[260px] md:translate-x-0 lg:w-[300px] ${
          sidebarOpen ? "translate-x-0" : "-translate-x-full"
        }`}
      >
        <div className="mb-3 flex items-center justify-between">
          <div className="flex font-serif items-center">
            <span className="text-5xl text-black bg-white p-1 rounded-[15px] mr-2">
              SS
            </span>
            <div className="grid grid-cols">
              <p className="tracking-[1.5px] font-bold">Safari Sales</p>
              <p className="text-sm text-white/70">Operations</p>
            </div>
          </div>

          {/* close button (below md only) */}
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

        <div className="mt-6 h-px w-full bg-white/15" />

        <nav className="space-y-2 mt-6 flex-1">
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
                    className={`flex items-center gap-3 px-4 py-2.5 text-sm font-semibold transition rounded-[10px] ${
                      isActive
                        ? "bg-white text-[#101B82] shadow-sm"
                        : "text-white/90 hover:bg-white/15 hover:text-white"
                    }`}
                  >
                    {config.icon && (
                      <config.icon
                        size={20}
                        weight={isActive ? "fill" : "regular"}
                        aria-hidden="true"
                        className="shrink-0"
                      />
                    )}
                    {config.title}
                  </div>
                )}
              </NavLink>
            ))}
        </nav>
      </aside>

      {/* main area */}
      <div className="flex h-screen flex-col bg-white md:ml-[260px] lg:ml-[300px]">
        <Header onMenuClick={() => setSidebarOpen(true)} />
        <main className="flex-1 overflow-y-auto overflow-x-hidden p-3 text-[#211917] sm:p-5 md:p-6 lg:p-8">
          <div className="mx-auto w-full max-w-full">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  );
};

export default Layout;
