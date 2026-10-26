import { useEffect, useRef, useState, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import { getAllData, apiRequest } from "../../api/api";

const Header = ({ onMenuClick }) => {
  const { user, token, logout, isImpersonating, stopImpersonation } = useAuth();
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

  const formatRelativeTime = (isoString) => {
    if (!isoString) return "";
    try {
      const diff = Math.floor((new Date() - new Date(isoString)) / 1000);
      if (diff < 60) return "Just now";
      if (diff < 3600) return `${Math.floor(diff / 60)}m ago`;
      if (diff < 86400) return `${Math.floor(diff / 3600)}h ago`;
      if (diff < 172800) return "Yesterday";
      return new Date(isoString).toLocaleDateString(undefined, { month: "short", day: "numeric" });
    } catch {
      return "";
    }
  };

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

  const handleNotificationClick = async (notification) => {
    const isRead = Boolean(notification.readAt) || notification.status === "READ";
    if (!isRead) {
      await handleRead(notification.id);
    }
    setOpenMenu(null);

    const type = (notification.relatedEntityType || "").toLowerCase();
    if (type.includes("booking") || type.includes("invoice")) {
      navigate("/accommodation-bookings");
    } else if (type.includes("requirement") || type.includes("safari")) {
      navigate("/safaris");
    } else if (type.includes("ticket") || type.includes("support")) {
      navigate("/support-tickets");
    }
  };

  const handleDeleteNotification = async (e, id) => {
    e.stopPropagation();
    setNotifications((prev) => prev.filter((n) => n.id !== id));
    try {
      await apiRequest(`/api/v1/notifications/${id}`, { method: "DELETE" }, token);
    } catch {
      // Ignore error
    }
  };

  const handleClearAll = async () => {
    setNotifications([]);
    try {
      await apiRequest("/api/v1/notifications/clear-all", { method: "DELETE" }, token);
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
    <header className="sticky top-0 z-30 flex h-[72px] w-full items-center justify-between border-b border-[#28332C]/10 bg-white px-3 sm:px-6 md:px-8">
      <div className="flex min-w-0 max-w-[60%] items-center gap-2 sm:gap-3">
        <button
          onClick={onMenuClick}
          className="flex h-10 w-10 items-center justify-center rounded-lg border border-[#28332C]/10 text-[#35634B] transition hover:bg-[#EAF1EC] md:hidden"
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
          <p className="truncate text-sm font-semibold text-[#315B43] sm:text-lg">
            Safari Sales
          </p>

          <p className="hidden text-[11px] text-[#28332C]/60 sm:block">
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
            className="relative flex h-10 w-10 shrink-0 items-center justify-center rounded-lg border border-[#28332C]/10 text-[#35634B] transition hover:bg-[#EAF1EC]"
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
            <div className="fixed left-3 right-3 top-[80px] z-50 overflow-hidden rounded-xl border border-[#28332C]/10 bg-white shadow-lg sm:absolute sm:left-auto sm:right-0 sm:top-auto sm:mt-3 sm:w-96">
              <div className="flex items-center justify-between gap-3 border-b border-[#28332C]/10 px-4 py-3 bg-[#F7F8F6]">
                <div className="flex items-center gap-2">
                  <p className="text-sm font-semibold text-[#28332C]">Notifications</p>
                  {unreadCount > 0 && (
                    <span className="rounded-full bg-red-100 px-2 py-0.5 text-[10px] font-bold text-red-700">
                      {unreadCount} unread
                    </span>
                  )}
                </div>

                <div className="flex items-center gap-3">
                  {unreadCount > 0 && (
                    <button
                      onClick={handleReadAll}
                      className="shrink-0 text-xs font-semibold text-[#35634B] hover:underline"
                    >
                      Mark all read
                    </button>
                  )}
                  {notifications.length > 0 && (
                    <button
                      onClick={handleClearAll}
                      className="shrink-0 text-xs text-[#28332C]/50 hover:text-red-600 transition"
                      title="Clear all notifications"
                    >
                      Clear all
                    </button>
                  )}
                </div>
              </div>

              <div className="max-h-[65vh] overflow-y-auto sm:max-h-96 divide-y divide-[#28332C]/5">
                {notifications.length === 0 ? (
                  <div className="px-4 py-8 text-center text-[#28332C]/60">
                    <svg xmlns="http://www.w3.org/2000/svg" className="mx-auto h-8 w-8 text-[#28332C]/30 mb-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.5" d="M15 17h5l-1.4-1.4A2 2 0 0118 14.2V11a6 6 0 10-12 0v3.2a2 2 0 01-.6 1.4L4 17h5m6 0a3 3 0 11-6 0" />
                    </svg>
                    <p className="text-sm font-medium">No notifications yet</p>
                    <p className="text-xs text-[#28332C]/40 mt-0.5">You're all caught up!</p>
                  </div>
                ) : (
                  notifications.map((notification) => {
                    const isRead = Boolean(notification.readAt) || notification.status === "READ";
                    const isUrgent = notification.priority === "URGENT";
                    const isHigh = notification.priority === "HIGH";

                    return (
                      <div
                        key={notification.id}
                        onClick={() => handleNotificationClick(notification)}
                        className={`group relative flex w-full cursor-pointer items-start gap-3 px-4 py-3 text-left transition hover:bg-[#35634B]/5 ${
                          isRead ? "bg-white" : "bg-[#35634B]/[0.04]"
                        }`}
                      >
                        <span
                          className={`mt-1.5 h-2 w-2 shrink-0 rounded-full ${
                            isRead
                              ? "bg-transparent"
                              : isUrgent
                              ? "bg-red-500 animate-pulse"
                              : "bg-[#35634B]"
                          }`}
                        />

                        <div className="min-w-0 flex-1">
                          <div className="flex items-center justify-between gap-2">
                            <p className={`truncate text-xs font-semibold ${isRead ? "text-[#28332C]/80" : "text-[#28332C] font-bold"}`}>
                              {notification.title}
                            </p>
                            <span className="shrink-0 text-[10px] text-[#28332C]/40">
                              {formatRelativeTime(notification.createdAt || notification.sentAt)}
                            </span>
                          </div>

                          {notification.message && (
                            <p className="mt-0.5 line-clamp-2 text-xs text-[#28332C]/65">
                              {notification.message}
                            </p>
                          )}

                          <div className="mt-1.5 flex items-center gap-2">
                            {isUrgent && (
                              <span className="rounded bg-red-100 px-1.5 py-0.5 text-[9px] font-bold text-red-700 uppercase">
                                Urgent
                              </span>
                            )}
                            {isHigh && (
                              <span className="rounded bg-amber-100 px-1.5 py-0.5 text-[9px] font-bold text-amber-800 uppercase">
                                High
                              </span>
                            )}
                            {notification.relatedEntityType && (
                              <span className="rounded bg-[#28332C]/5 px-1.5 py-0.5 text-[9px] font-medium text-[#28332C]/60">
                                {notification.relatedEntityType.replace(/([A-Z])/g, " $1").trim()}
                              </span>
                            )}
                          </div>
                        </div>

                        <button
                          onClick={(e) => handleDeleteNotification(e, notification.id)}
                          className="opacity-0 group-hover:opacity-100 -mr-1 p-1 text-[#28332C]/40 hover:text-red-600 transition rounded"
                          title="Dismiss notification"
                          aria-label="Dismiss notification"
                        >
                          <svg xmlns="http://www.w3.org/2000/svg" className="h-3.5 w-3.5" viewBox="0 0 20 20" fill="currentColor">
                            <path fillRule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clipRule="evenodd" />
                          </svg>
                        </button>
                      </div>
                    );
                  })
                )}
              </div>
            </div>
          )}
        </div>

        <div className="relative">
          <button
            onClick={() => toggle("profile")}
            className="flex items-center gap-2 rounded-full border border-[#28332C]/10 py-1 pl-1 pr-1 transition hover:bg-[#35634B]/5 sm:gap-3 sm:pr-4"
          >
            <span className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-[#35634B] text-sm font-semibold text-white">
              {displayName.charAt(0).toUpperCase()}
            </span>

            <span className="hidden max-w-[120px] truncate text-sm font-semibold text-[#28332C] sm:block">
              {displayName}
            </span>
          </button>

          {openMenu === "profile" && (
            <div className="absolute right-0 z-50 mt-3 w-[calc(100vw-24px)] max-w-64 overflow-hidden rounded-xl border border-[#28332C]/10 bg-white shadow-lg sm:w-64">
              <div className="flex items-center gap-3 border-b border-[#28332C]/10 px-4 py-4">
                <span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-full bg-[#35634B] text-lg font-semibold text-white">
                  {displayName.charAt(0).toUpperCase()}
                </span>

                <div className="min-w-0">
                  <p className="truncate text-sm font-semibold text-[#28332C]">
                    {displayName}
                  </p>

                  {user?.email && (
                    <p className="truncate text-xs text-[#28332C]/60">
                      {user.email}
                    </p>
                  )}

                  {user?.role && (
                    <span className="mt-1 inline-block max-w-full truncate rounded-full bg-[#EAF1EC] px-2 py-0.5 text-[11px] font-semibold capitalize text-[#315B43]">
                      {String(user.role).replace(/_/g, " ").toLowerCase()}
                    </span>
                  )}
                </div>
              </div>

              <div className="border-b border-[#28332C]/10 p-1">
                <button
                  onClick={() => {
                    setOpenMenu(null);
                    navigate("/profile");
                  }}
                  className="flex w-full items-center gap-3 rounded-lg px-3 py-2 text-xs font-medium text-[#28332C] transition hover:bg-[#F2F4F0]"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 text-[#35634B]" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
                    <path strokeLinecap="round" strokeLinejoin="round" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                  </svg>
                  My Profile & Security
                </button>

                <button
                  onClick={() => {
                    setOpenMenu(null);
                    navigate("/support-tickets");
                  }}
                  className="flex w-full items-center gap-3 rounded-lg px-3 py-2 text-xs font-medium text-[#28332C] transition hover:bg-[#F2F4F0]"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 text-[#35634B]" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
                    <path strokeLinecap="round" strokeLinejoin="round" d="M18.364 5.636l-3.536 3.536m0 5.656l3.536 3.536M9.172 9.172L5.636 5.636m3.536 9.192l-3.536 3.536M21 12a9 9 0 11-18 0 9 9 0 0118 0zm-5 0a4 4 0 11-8 0 4 4 0 018 0z" />
                  </svg>
                  Support Tickets
                </button>

                {isImpersonating && (
                  <button
                    onClick={() => {
                      setOpenMenu(null);
                      stopImpersonation();
                      navigate("/dashboard");
                    }}
                    className="flex w-full items-center gap-3 rounded-lg bg-amber-50 px-3 py-2 text-xs font-bold text-amber-900 transition hover:bg-amber-100 mt-1"
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 text-amber-800" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
                      <path strokeLinecap="round" strokeLinejoin="round" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                    </svg>
                    Toka (Rudi Super Admin)
                  </button>
                )}
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
