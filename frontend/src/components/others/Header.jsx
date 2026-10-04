import { useEffect, useRef, useState, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import { getAllData, apiRequest } from "../../api/api";

const Header = ({ onMenuClick }) => {
  const { user, token, logout } = useAuth();
  const navigate = useNavigate();
  const [openMenu, setOpenMenu] = useState(null);
  const [notifications, setNotifications] = useState([]);
  const wrapperRef = useRef(null);

  const displayName = user?.name || user?.email || "User";
  const unreadCount = notifications.filter((n) => !n.readAt && n.status !== "READ").length;

  const fetchNotifications = useCallback(async () => {
    if (!token) return;
    try {
      const res = await getAllData("/api/v1/notifications", token);
      if (Array.isArray(res)) {
        setNotifications(res);
      }
    } catch {
      // Fallback silently if offline
    }
  }, [token]);

  useEffect(() => {
    fetchNotifications();
    const interval = setInterval(fetchNotifications, 20000);
    return () => clearInterval(interval);
  }, [fetchNotifications]);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (wrapperRef.current && !wrapperRef.current.contains(event.target)) {
        setOpenMenu(null);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  const toggle = (menu) =>
    setOpenMenu((prev) => (prev === menu ? null : menu));

  const handleRead = async (id) => {
    setNotifications((prev) =>
      prev.map((n) => (n.id === id ? { ...n, readAt: new Date().toISOString(), status: "READ" } : n))
    );
    try {
      await apiRequest(`/api/v1/notifications/${id}/read`, { method: "PATCH" }, token);
    } catch {
      // Ignore error
    }
  };

  const handleReadAll = async () => {
    setNotifications((prev) =>
      prev.map((n) => ({ ...n, readAt: new Date().toISOString(), status: "READ" }))
    );
    try {
      await apiRequest("/api/v1/notifications/mark-all-read", { method: "PATCH" }, token);
    } catch {
      // Ignore error
    }
  };

  const handleLogout = async () => {
    try {
      await logout();
    } finally {
      navigate("/login");
    }
  };

  return (
    <header className="sticky top-0 z-30 flex h-[72px] w-full items-center justify-between border-b border-[#211917]/10 bg-white px-3 sm:px-6 md:px-8">
      <div className="flex min-w-0 max-w-[60%] items-center gap-2 sm:gap-3">
        <button
          onClick={onMenuClick}
          className="flex h-10 w-10 items-center justify-center rounded-[10px] border border-[#211917]/10 text-[#101B82] transition hover:bg-[#101B82]/10 md:hidden"
          aria-label="Open menu"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            className="h-6 w-6"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
            strokeWidth="2"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              d="M4 6h16M4 12h16M4 18h16"
            />
          </svg>
        </button>

        <div className="min-w-0">
          <p className="truncate text-sm font-bold text-[#101B82] sm:text-lg">
            Safari Sales
          </p>

          <p className="hidden text-[11px] text-[#211917]/60 sm:block">
            Operations dashboard
          </p>
        </div>
      </div>

      <div
        ref={wrapperRef}
        className="flex shrink-0 items-center gap-1.5 sm:gap-3"
      >
        <div className="relative">
          <button
            onClick={() => toggle("notifications")}
            className="relative flex h-10 w-10 shrink-0 items-center justify-center rounded-full border border-[#211917]/10 text-[#101B82] transition hover:bg-[#101B82]/10"
            aria-label="Notifications"
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
                d="M15 17h5l-1.4-1.4A2 2 0 0118 14.2V11a6 6 0 10-12 0v3.2a2 2 0 01-.6 1.4L4 17h5m6 0a3 3 0 11-6 0"
              />
            </svg>

            {unreadCount > 0 && (
              <span className="absolute -right-1 -top-1 flex h-5 min-w-[20px] items-center justify-center rounded-full bg-red-500 px-1 text-[11px] font-bold text-white">
                {unreadCount > 9 ? "9+" : unreadCount}
              </span>
            )}
          </button>

          {openMenu === "notifications" && (
            <div className="fixed left-3 right-3 top-[80px] z-50 overflow-hidden rounded-[12px] border border-[#211917]/10 bg-white shadow-lg sm:absolute sm:left-auto sm:right-0 sm:top-auto sm:mt-3 sm:w-80">
              <div className="flex items-center justify-between gap-3 border-b border-[#211917]/10 px-4 py-3">
                <p className="text-sm font-bold text-[#211917]">Notifications</p>

                {unreadCount > 0 && (
                  <button
                    onClick={handleReadAll}
                    className="shrink-0 text-xs font-semibold text-[#101B82] hover:underline"
                  >
                    Mark all as read
                  </button>
                )}
              </div>

              <div className="max-h-[60vh] overflow-y-auto sm:max-h-80">
                {notifications.length === 0 ? (
                  <p className="px-4 py-6 text-center text-sm text-[#211917]/60">
                    No notifications yet
                  </p>
                ) : (
                  notifications.map((notification) => (
                    <button
                      key={notification.id}
                      onClick={() => handleRead(notification.id)}
                      className={`flex w-full items-start gap-3 border-b border-[#211917]/5 px-4 py-3 text-left transition hover:bg-[#101B82]/5 ${
                        notification.read ? "" : "bg-[#101B82]/5"
                      }`}
                    >
                      <span
                        className={`mt-1.5 h-2 w-2 shrink-0 rounded-full ${
                          notification.read ? "bg-transparent" : "bg-[#101B82]"
                        }`}
                      />

                      <div className="min-w-0">
                        <p className="truncate text-sm font-semibold text-[#211917]">
                          {notification.title}
                        </p>

                        {notification.message && (
                          <p className="break-words text-xs text-[#211917]/60">
                            {notification.message}
                          </p>
                        )}
                      </div>
                    </button>
                  ))
                )}
              </div>
            </div>
          )}
        </div>

        <div className="relative">
          <button
            onClick={() => toggle("profile")}
            className="flex items-center gap-2 rounded-full border border-[#211917]/10 py-1 pl-1 pr-1 transition hover:bg-[#101B82]/10 sm:gap-3 sm:pr-4"
          >
            <span className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-[#101B82] text-sm font-bold text-white">
              {displayName.charAt(0).toUpperCase()}
            </span>

            <span className="hidden max-w-[120px] truncate text-sm font-semibold text-[#211917] sm:block">
              {displayName}
            </span>
          </button>

          {openMenu === "profile" && (
            <div className="absolute right-0 z-50 mt-3 w-[calc(100vw-24px)] max-w-64 overflow-hidden rounded-[12px] border border-[#211917]/10 bg-white shadow-lg sm:w-64">
              <div className="flex items-center gap-3 border-b border-[#211917]/10 px-4 py-4">
                <span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-full bg-[#101B82] text-lg font-bold text-white">
                  {displayName.charAt(0).toUpperCase()}
                </span>

                <div className="min-w-0">
                  <p className="truncate text-sm font-bold text-[#211917]">
                    {displayName}
                  </p>

                  {user?.email && (
                    <p className="truncate text-xs text-[#211917]/60">
                      {user.email}
                    </p>
                  )}

                  {user?.role && (
                    <span className="mt-1 inline-block max-w-full truncate rounded-full bg-[#101B82]/10 px-2 py-0.5 text-[11px] font-semibold capitalize text-[#101B82]">
                      {String(user.role).replace(/_/g, " ").toLowerCase()}
                    </span>
                  )}
                </div>
              </div>

              <button
                onClick={handleLogout}
                className="flex w-full items-center gap-3 px-4 py-3 text-sm font-semibold text-red-600 transition hover:bg-red-50"
              >
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  className="h-5 w-5 shrink-0"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                  strokeWidth="2"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"
                  />
                </svg>
                Logout
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};

export default Header;
