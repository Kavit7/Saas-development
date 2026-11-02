import { useState, useEffect } from "react";
import { useAuth } from "../hooks/useAuth";
import { apiRequest } from "../api/api";
import {
  User,
  ShieldCheck,
  Key,
  CheckCircle,
  Warning,
  ArrowsClockwise,
  PencilSimple,
  Buildings,
  Briefcase,
  Bell,
  Sliders,
  Trash,
  X,
  LockKey,
} from "@phosphor-icons/react";

const ProfilePage = () => {
  const { user, token, refreshProfile } = useAuth();
  
  // Tabs: 'profile', 'security', 'team', 'notifications', 'preferences'
  const [activeTab, setActiveTab] = useState("profile");

  // Password Modal State
  const [passwordModalOpen, setPasswordModalOpen] = useState(false);
  const [passwordForm, setPasswordForm] = useState({
    currentPassword: "",
    newPassword: "",
    confirmPassword: "",
  });
  const [passwordLoading, setPasswordLoading] = useState(false);

  // Edit Profile Modal State
  const [editModalOpen, setEditModalOpen] = useState(false);
  const [editForm, setEditForm] = useState({
    firstName: "",
    lastName: "",
    phone: "",
    gender: "MALE",
  });
  const [savingProfile, setSavingProfile] = useState(false);

  // General State
  const [refreshing, setRefreshing] = useState(false);
  const [message, setMessage] = useState({ text: "", type: "" });
  const [deleteModalOpen, setDeleteModalOpen] = useState(false);

  // Sync edit form with current user data when user changes or modal opens
  useEffect(() => {
    if (user) {
      setEditForm({
        firstName: user.firstName || "",
        lastName: user.lastName || "",
        phone: user.phone || "",
        gender: user.gender || "MALE",
      });
    }
  }, [user]);

  const role = String(user?.role_name || user?.role || "")
    .toUpperCase()
    .replace(/^ROLE_/, "")
    .replace(/_/g, " ")
    .trim();

  const isTemporary = Boolean(user?.mustChangePassword);

  const handleRefresh = async () => {
    setRefreshing(true);
    try {
      await refreshProfile();
      setMessage({ text: "Profile refreshed successfully.", type: "success" });
    } catch {
      setMessage({ text: "Failed to reload profile.", type: "error" });
    } finally {
      setRefreshing(false);
    }
  };

  const handleOpenEditModal = () => {
    setEditForm({
      firstName: user?.firstName || "",
      lastName: user?.lastName || "",
      phone: user?.phone || "",
      gender: user?.gender || "MALE",
    });
    setEditModalOpen(true);
  };

  const handleSaveProfile = async (e) => {
    e.preventDefault();
    setSavingProfile(true);
    setMessage({ text: "", type: "" });

    try {
      await apiRequest(
        "/api/v1/auth/profile",
        {
          method: "PUT",
          body: JSON.stringify({
            firstName: editForm.firstName.trim(),
            lastName: editForm.lastName.trim(),
            phone: editForm.phone.trim(),
            gender: editForm.gender,
          }),
        },
        token
      );

      await refreshProfile();
      setMessage({ text: "Profile updated successfully!", type: "success" });
      setEditModalOpen(false);
    } catch (err) {
      setMessage({
        text: err.message || "Failed to update profile. Please try again.",
        type: "error",
      });
    } finally {
      setSavingProfile(false);
    }
  };

  const handlePasswordChange = async (e) => {
    e.preventDefault();
    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      setMessage({ text: "New passwords do not match.", type: "error" });
      return;
    }
    if (passwordForm.newPassword.length < 4) {
      setMessage({
        text: "New password must be at least 4 characters long.",
        type: "error",
      });
      return;
    }

    setPasswordLoading(true);
    setMessage({ text: "", type: "" });
    try {
      await apiRequest(
        "/api/v1/auth/change-password",
        {
          method: "POST",
          body: JSON.stringify({
            newPassword: passwordForm.newPassword,
          }),
        },
        token
      );

      setMessage({
        text: "Password changed successfully! Your permanent password is now active.",
        type: "success",
      });
      setPasswordForm({ currentPassword: "", newPassword: "", confirmPassword: "" });
      setPasswordModalOpen(false);
      await refreshProfile();
    } catch (err) {
      setMessage({ text: err.message || "Failed to change password.", type: "error" });
    } finally {
      setPasswordLoading(false);
    }
  };

  const fullName = user?.firstName
    ? `${user.firstName} ${user.lastName || ""}`.trim()
    : user?.email || "User Profile";

  const initials = (
    user?.firstName?.charAt(0) ||
    user?.email?.charAt(0) ||
    "U"
  ).toUpperCase();

  return (
    <div className="mx-auto max-w-6xl space-y-6 p-4 sm:p-6 lg:p-8">
      {/* Top Header / Account Settings Title */}
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between border-b border-[#28332C]/10 pb-5">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-[#211917] font-title sm:text-3xl">
            Account Settings
          </h1>
          <p className="mt-1 text-xs sm:text-sm text-[#211917]/70">
            Manage your personal profile details, account security, and organizational preferences.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <button
            onClick={handleRefresh}
            disabled={refreshing}
            className="inline-flex items-center gap-2 rounded-xl border border-[#211917]/15 bg-white px-3.5 py-2 text-xs font-semibold text-[#264624] shadow-xs transition hover:bg-[#FAF8F5] active:scale-95 disabled:opacity-50"
            title="Refresh profile data"
          >
            <ArrowsClockwise className={refreshing ? "animate-spin" : ""} size={16} />
            <span>{refreshing ? "Refreshing..." : "Refresh"}</span>
          </button>
        </div>
      </div>

      {/* Alert / Notification Banner */}
      {message.text && (
        <div
          className={`relative flex items-center justify-between rounded-2xl border p-4 text-sm font-medium transition-all ${
            message.type === "success"
              ? "border-emerald-200 bg-emerald-50 text-emerald-800"
              : "border-rose-200 bg-rose-50 text-rose-800"
          }`}
        >
          <div className="flex items-center gap-2.5">
            {message.type === "success" ? (
              <CheckCircle size={20} className="shrink-0 text-emerald-600" />
            ) : (
              <Warning size={20} className="shrink-0 text-rose-600" />
            )}
            <span>{message.text}</span>
          </div>
          <button
            onClick={() => setMessage({ text: "", type: "" })}
            className="text-xs font-bold opacity-60 hover:opacity-100"
          >
            ✕
          </button>
        </div>
      )}

      {/* Main Layout: Left Vertical Navigation Tabs + Right Content Area */}
      <div className="flex flex-col gap-6 md:flex-row md:items-start">
        {/* Left Tab Navigation (matching profile.jpeg sidebar) */}
        <div className="w-full md:w-56 lg:w-64 shrink-0">
          <div className="flex md:flex-col gap-1.5 overflow-x-auto pb-2 md:pb-0">
            <button
              onClick={() => setActiveTab("profile")}
              className={`flex items-center gap-3 rounded-2xl px-4 py-3 text-xs sm:text-sm font-semibold transition text-left whitespace-nowrap ${
                activeTab === "profile"
                  ? "bg-[#264624]/10 text-[#264624] shadow-xs"
                  : "text-[#211917]/70 hover:bg-[#FAF8F5] hover:text-[#211917]"
              }`}
            >
              <User size={18} className={activeTab === "profile" ? "text-[#264624]" : "text-[#211917]/50"} />
              <span>My Profile</span>
            </button>

            <button
              onClick={() => setActiveTab("security")}
              className={`flex items-center justify-between rounded-2xl px-4 py-3 text-xs sm:text-sm font-semibold transition text-left whitespace-nowrap ${
                activeTab === "security"
                  ? "bg-[#264624]/10 text-[#264624] shadow-xs"
                  : "text-[#211917]/70 hover:bg-[#FAF8F5] hover:text-[#211917]"
              }`}
            >
              <div className="flex items-center gap-3">
                <LockKey size={18} className={activeTab === "security" ? "text-[#264624]" : "text-[#211917]/50"} />
                <span>Security</span>
              </div>
              {isTemporary && (
                <span className="h-2 w-2 rounded-full bg-amber-500" title="Password change required" />
              )}
            </button>

            <button
              onClick={() => setActiveTab("team")}
              className={`flex items-center gap-3 rounded-2xl px-4 py-3 text-xs sm:text-sm font-semibold transition text-left whitespace-nowrap ${
                activeTab === "team"
                  ? "bg-[#264624]/10 text-[#264624] shadow-xs"
                  : "text-[#211917]/70 hover:bg-[#FAF8F5] hover:text-[#211917]"
              }`}
            >
              <Buildings size={18} className={activeTab === "team" ? "text-[#264624]" : "text-[#211917]/50"} />
              <span>Teams & Company</span>
            </button>

            <button
              onClick={() => setActiveTab("notifications")}
              className={`flex items-center gap-3 rounded-2xl px-4 py-3 text-xs sm:text-sm font-semibold transition text-left whitespace-nowrap ${
                activeTab === "notifications"
                  ? "bg-[#264624]/10 text-[#264624] shadow-xs"
                  : "text-[#211917]/70 hover:bg-[#FAF8F5] hover:text-[#211917]"
              }`}
            >
              <Bell size={18} className={activeTab === "notifications" ? "text-[#264624]" : "text-[#211917]/50"} />
              <span>Notifications</span>
            </button>

            <button
              onClick={() => setActiveTab("preferences")}
              className={`flex items-center gap-3 rounded-2xl px-4 py-3 text-xs sm:text-sm font-semibold transition text-left whitespace-nowrap ${
                activeTab === "preferences"
                  ? "bg-[#264624]/10 text-[#264624] shadow-xs"
                  : "text-[#211917]/70 hover:bg-[#FAF8F5] hover:text-[#211917]"
              }`}
            >
              <Sliders size={18} className={activeTab === "preferences" ? "text-[#264624]" : "text-[#211917]/50"} />
              <span>Preferences</span>
            </button>

            <div className="pt-2 mt-2 border-t border-[#211917]/10 hidden md:block" />

            <button
              onClick={() => setDeleteModalOpen(true)}
              className="flex items-center gap-3 rounded-2xl px-4 py-3 text-xs sm:text-sm font-semibold text-rose-600 transition hover:bg-rose-50 text-left whitespace-nowrap"
            >
              <Trash size={18} className="text-rose-500" />
              <span>Delete Account</span>
            </button>
          </div>
        </div>

        {/* Right Content Area */}
        <div className="flex-1 space-y-6 min-w-0">
          {/* TAB 1: MY PROFILE */}
          {activeTab === "profile" && (
            <div className="space-y-6">
              {/* Card 1: My Profile (Top summary card matching profile.jpeg) */}
              <div className="rounded-3xl border border-[#211917]/10 bg-white p-6 sm:p-7 shadow-xs transition">
                <div className="flex items-center justify-between pb-5 border-b border-[#211917]/5">
                  <h2 className="text-base font-bold text-[#211917]">My Profile</h2>
                  <button
                    onClick={handleOpenEditModal}
                    className="inline-flex items-center gap-1.5 rounded-xl border border-[#211917]/15 bg-white px-3.5 py-1.5 text-xs font-semibold text-[#211917] shadow-2xs transition hover:bg-[#FAF8F5] active:scale-95"
                  >
                    <span>Edit</span>
                    <PencilSimple size={14} className="text-[#264624]" />
                  </button>
                </div>

                <div className="mt-6 flex flex-col sm:flex-row items-start sm:items-center gap-5">
                  {/* Circular Avatar */}
                  <div className="relative">
                    <div className="flex h-20 w-20 sm:h-22 sm:w-22 shrink-0 items-center justify-center rounded-full border-4 border-[#FAF8F5] bg-gradient-to-tr from-[#264624] to-[#3b6638] text-2xl sm:text-3xl font-black text-white shadow-md">
                      {initials}
                    </div>
                    <span
                      className={`absolute bottom-0 right-1 h-5 w-5 rounded-full border-2 border-white ${
                        user?.status === "INACTIVE" ? "bg-rose-500" : "bg-emerald-500"
                      }`}
                      title={user?.status || "Active"}
                    />
                  </div>

                  {/* Profile info block */}
                  <div className="space-y-1 min-w-0 flex-1">
                    <h3 className="text-xl font-bold text-[#211917] sm:text-2xl truncate">
                      {fullName}
                    </h3>
                    <p className="text-xs sm:text-sm font-medium text-[#211917]/70">
                      {role || "Staff Member"}
                    </p>
                    <p className="text-xs text-[#211917]/50 font-sans">
                      {user?.companyName || "Global Platform"}
                    </p>
                  </div>

                  {/* Badges */}
                  <div className="flex flex-wrap sm:flex-col items-start sm:items-end gap-2">
                    <span className="inline-flex items-center gap-1.5 rounded-full bg-[#264624]/10 px-3 py-1 text-xs font-semibold text-[#264624]">
                      <ShieldCheck size={14} />
                      {role || "STAFF"}
                    </span>
                    <span
                      className={`inline-flex items-center gap-1.5 rounded-full px-3 py-1 text-xs font-semibold ${
                        user?.status === "INACTIVE"
                          ? "bg-rose-100 text-rose-800"
                          : "bg-emerald-100 text-emerald-800"
                      }`}
                    >
                      <CheckCircle size={14} />
                      {user?.status || "ACTIVE"}
                    </span>
                  </div>
                </div>
              </div>

              {/* Card 2: Personal Information (matching profile.jpeg) */}
              <div className="rounded-3xl border border-[#211917]/10 bg-white p-6 sm:p-7 shadow-xs transition">
                <div className="flex items-center justify-between pb-5 border-b border-[#211917]/5">
                  <h2 className="text-base font-bold text-[#211917]">Personal Information</h2>
                  <button
                    onClick={handleOpenEditModal}
                    className="inline-flex items-center gap-1.5 rounded-xl border border-[#211917]/15 bg-white px-3.5 py-1.5 text-xs font-semibold text-[#211917] shadow-2xs transition hover:bg-[#FAF8F5] active:scale-95"
                  >
                    <span>Edit</span>
                    <PencilSimple size={14} className="text-[#264624]" />
                  </button>
                </div>

                <div className="mt-6 grid grid-cols-1 gap-y-6 gap-x-8 sm:grid-cols-2">
                  <div>
                    <p className="text-xs font-medium text-[#211917]/50">First Name</p>
                    <p className="mt-1 text-sm font-semibold text-[#211917]">
                      {user?.firstName || "—"}
                    </p>
                  </div>

                  <div>
                    <p className="text-xs font-medium text-[#211917]/50">Last Name</p>
                    <p className="mt-1 text-sm font-semibold text-[#211917]">
                      {user?.lastName || "—"}
                    </p>
                  </div>

                  <div>
                    <p className="text-xs font-medium text-[#211917]/50">Email address</p>
                    <p className="mt-1 text-sm font-semibold text-[#211917] font-mono break-all">
                      {user?.email || "—"}
                    </p>
                  </div>

                  <div>
                    <p className="text-xs font-medium text-[#211917]/50">Phone</p>
                    <p className="mt-1 text-sm font-semibold text-[#211917]">
                      {user?.phone || "No phone registered"}
                    </p>
                  </div>

                  <div>
                    <p className="text-xs font-medium text-[#211917]/50">Gender</p>
                    <p className="mt-1 text-sm font-semibold text-[#211917]">
                      {user?.gender || "Not specified"}
                    </p>
                  </div>

                  <div>
                    <p className="text-xs font-medium text-[#211917]/50">Bio / Designation</p>
                    <p className="mt-1 text-sm font-semibold text-[#211917]">
                      {role || "Staff Member"}
                    </p>
                  </div>
                </div>
              </div>

              {/* Card 3: Address / Organization & System Details */}
              <div className="rounded-3xl border border-[#211917]/10 bg-white p-6 sm:p-7 shadow-xs transition">
                <div className="flex items-center justify-between pb-5 border-b border-[#211917]/5">
                  <h2 className="text-base font-bold text-[#211917]">Company & System Details</h2>
                  <span className="inline-flex items-center gap-1.5 rounded-full bg-[#FAF8F5] border border-[#211917]/10 px-3 py-1 text-xs font-medium text-[#211917]/70">
                    <ShieldCheck size={14} className="text-[#264624]" />
                    <span>Verified</span>
                  </span>
                </div>

                <div className="mt-6 grid grid-cols-1 gap-y-6 gap-x-8 sm:grid-cols-2">
                  <div>
                    <p className="text-xs font-medium text-[#211917]/50">Company / Organization</p>
                    <p className="mt-1 text-sm font-semibold text-[#211917]">
                      {user?.companyName || "Global Platform"}
                    </p>
                  </div>

                  <div>
                    <p className="text-xs font-medium text-[#211917]/50">Assigned Role</p>
                    <p className="mt-1 text-sm font-semibold text-[#211917]">
                      {role || "STAFF"}
                    </p>
                  </div>

                  <div>
                    <p className="text-xs font-medium text-[#211917]/50">Account ID</p>
                    <p className="mt-1 text-xs font-mono text-[#211917]/80 truncate">
                      {user?.id || "—"}
                    </p>
                  </div>

                  <div>
                    <p className="text-xs font-medium text-[#211917]/50">Account Status</p>
                    <p className="mt-1 text-sm font-semibold text-emerald-700">
                      {user?.status || "ACTIVE"}
                    </p>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* TAB 2: SECURITY */}
          {activeTab === "security" && (
            <div className="space-y-6">
              <div className="rounded-3xl border border-[#211917]/10 bg-white p-6 sm:p-7 shadow-xs">
                <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 pb-5 border-b border-[#211917]/5">
                  <div>
                    <h2 className="text-base font-bold text-[#211917]">Security & Credentials</h2>
                    <p className="text-xs text-[#211917]/60 mt-0.5">
                      Manage your password, temporary credential statuses, and session protection.
                    </p>
                  </div>
                  <button
                    onClick={() => setPasswordModalOpen(true)}
                    className="inline-flex items-center gap-1.5 rounded-xl bg-[#264624] px-4 py-2 text-xs font-bold text-white shadow-xs transition hover:bg-[#1b331a] active:scale-95"
                  >
                    <Key size={14} />
                    <span>{isTemporary ? "Set Permanent Password" : "Change Password"}</span>
                  </button>
                </div>

                <div className="mt-6 space-y-4">
                  {isTemporary ? (
                    <div className="rounded-2xl border border-amber-300 bg-amber-50 p-4 text-amber-900">
                      <div className="flex items-start gap-3">
                        <Warning className="shrink-0 text-amber-600 mt-0.5" size={20} />
                        <div>
                          <h4 className="font-bold text-sm">Temporary Default Password</h4>
                          <p className="mt-1 text-xs text-amber-800 leading-relaxed">
                            You are currently using your temporary default password. For security, this password will expire in 3 days. Please change your password to make it permanent.
                          </p>
                          {user?.passwordExpire && (
                            <p className="mt-2 text-[11px] font-mono text-amber-900">
                              Expires: {new Date(user.passwordExpire).toLocaleString()}
                            </p>
                          )}
                        </div>
                      </div>
                    </div>
                  ) : (
                    <div className="rounded-2xl border border-emerald-200 bg-emerald-50 p-4 text-emerald-900">
                      <div className="flex items-start gap-3">
                        <CheckCircle className="shrink-0 text-emerald-600 mt-0.5" size={20} />
                        <div>
                          <h4 className="font-bold text-sm">Permanent Password Active</h4>
                          <p className="mt-1 text-xs text-emerald-800 leading-relaxed">
                            Your chosen personal password is secure and permanent. It does not expire.
                          </p>
                        </div>
                      </div>
                    </div>
                  )}

                  <div className="rounded-2xl border border-[#211917]/10 bg-[#FAF8F5] p-5 text-xs text-[#211917]/70 space-y-2">
                    <p className="font-semibold text-[#211917] text-sm">Security Recommendations:</p>
                    <ul className="list-disc pl-4 space-y-1.5">
                      <li>Use a unique password of at least 8 characters with a mix of numbers and symbols.</li>
                      <li>Never share your credentials with colleagues or third parties.</li>
                      <li>Each team member must log in with their own registered account.</li>
                      <li>Admins can reset passwords for staff if you get locked out.</li>
                    </ul>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* TAB 3: TEAMS & COMPANY */}
          {activeTab === "team" && (
            <div className="space-y-6">
              <div className="rounded-3xl border border-[#211917]/10 bg-white p-6 sm:p-7 shadow-xs">
                <div className="pb-5 border-b border-[#211917]/5">
                  <h2 className="text-base font-bold text-[#211917]">Organization & Team Structure</h2>
                  <p className="text-xs text-[#211917]/60 mt-0.5">
                    Information regarding your assigned company workspace and system access.
                  </p>
                </div>

                <div className="mt-6 space-y-4">
                  <div className="flex items-start gap-3.5 rounded-2xl border border-[#211917]/5 bg-[#FAF8F5] p-4">
                    <Buildings className="mt-0.5 text-[#264624]" size={22} />
                    <div className="min-w-0">
                      <p className="text-xs font-medium text-[#211917]/60">Workspace / Company</p>
                      <p className="font-bold text-sm text-[#211917]">{user?.companyName || "Global Platform"}</p>
                      <p className="mt-1 text-xs text-[#211917]/50 font-mono">Company ID: {user?.companyId || "Primary Tenant"}</p>
                    </div>
                  </div>

                  <div className="flex items-start gap-3.5 rounded-2xl border border-[#211917]/5 bg-[#FAF8F5] p-4">
                    <Briefcase className="mt-0.5 text-[#264624]" size={22} />
                    <div className="min-w-0">
                      <p className="text-xs font-medium text-[#211917]/60">Assigned System Role</p>
                      <p className="font-bold text-sm text-[#211917]">{role || "STAFF"}</p>
                      <p className="mt-1 text-xs text-[#211917]/50">
                        Permissions and resource access are controlled by system administrators.
                      </p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* TAB 4: NOTIFICATIONS */}
          {activeTab === "notifications" && (
            <div className="rounded-3xl border border-[#211917]/10 bg-white p-6 sm:p-7 shadow-xs">
              <div className="pb-5 border-b border-[#211917]/5">
                <h2 className="text-base font-bold text-[#211917]">Notification Preferences</h2>
                <p className="text-xs text-[#211917]/60 mt-0.5">
                  Configure alerts, safari booking updates, and system email notifications.
                </p>
              </div>

              <div className="mt-6 space-y-4">
                <div className="flex items-center justify-between p-4 rounded-2xl bg-[#FAF8F5] border border-[#211917]/5">
                  <div>
                    <h4 className="text-sm font-bold text-[#211917]">Booking & Itinerary Updates</h4>
                    <p className="text-xs text-[#211917]/60">Receive notifications when safari bookings or itineraries change.</p>
                  </div>
                  <input
                    type="checkbox"
                    defaultChecked
                    className="h-4 w-4 rounded accent-[#264624]"
                  />
                </div>

                <div className="flex items-center justify-between p-4 rounded-2xl bg-[#FAF8F5] border border-[#211917]/5">
                  <div>
                    <h4 className="text-sm font-bold text-[#211917]">Security & Password Alerts</h4>
                    <p className="text-xs text-[#211917]/60">Receive email alerts on new logins or credential modifications.</p>
                  </div>
                  <input
                    type="checkbox"
                    defaultChecked
                    className="h-4 w-4 rounded accent-[#264624]"
                  />
                </div>
              </div>
            </div>
          )}

          {/* TAB 5: PREFERENCES */}
          {activeTab === "preferences" && (
            <div className="rounded-3xl border border-[#211917]/10 bg-white p-6 sm:p-7 shadow-xs">
              <div className="pb-5 border-b border-[#211917]/5">
                <h2 className="text-base font-bold text-[#211917]">System Preferences</h2>
                <p className="text-xs text-[#211917]/60 mt-0.5">
                  Customize your workspace experience and regional settings.
                </p>
              </div>

              <div className="mt-6 grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div className="p-4 rounded-2xl bg-[#FAF8F5] border border-[#211917]/5">
                  <p className="text-xs font-medium text-[#211917]/60">System Language</p>
                  <p className="font-semibold text-sm text-[#211917] mt-1">English (US)</p>
                </div>
                <div className="p-4 rounded-2xl bg-[#FAF8F5] border border-[#211917]/5">
                  <p className="text-xs font-medium text-[#211917]/60">Time Zone</p>
                  <p className="font-semibold text-sm text-[#211917] mt-1">East Africa Time (EAT, UTC+3)</p>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* MODAL 1: EDIT PROFILE MODAL */}
      {editModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4 animate-in fade-in duration-200">
          <div className="w-full max-w-lg rounded-3xl border border-[#211917]/10 bg-white p-6 sm:p-8 shadow-2xl">
            <div className="flex items-center justify-between pb-4 border-b border-[#211917]/10">
              <div className="flex items-center gap-2.5 text-[#264624]">
                <User size={22} />
                <h3 className="font-bold text-lg font-title text-[#211917]">
                  Edit Profile
                </h3>
              </div>
              <button
                onClick={() => setEditModalOpen(false)}
                className="rounded-lg p-1 text-[#211917]/50 hover:bg-[#FAF8F5] hover:text-[#211917]"
              >
                <X size={18} />
              </button>
            </div>

            <form onSubmit={handleSaveProfile} className="mt-5 space-y-4">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-bold uppercase text-[#211917]/70 mb-1.5">
                    First Name
                  </label>
                  <input
                    type="text"
                    required
                    value={editForm.firstName}
                    onChange={(e) =>
                      setEditForm({ ...editForm, firstName: e.target.value })
                    }
                    placeholder="Enter first name"
                    className="w-full rounded-xl border border-[#211917]/20 px-3.5 py-2.5 text-sm focus:border-[#264624] focus:outline-hidden"
                  />
                </div>

                <div>
                  <label className="block text-xs font-bold uppercase text-[#211917]/70 mb-1.5">
                    Last Name
                  </label>
                  <input
                    type="text"
                    required
                    value={editForm.lastName}
                    onChange={(e) =>
                      setEditForm({ ...editForm, lastName: e.target.value })
                    }
                    placeholder="Enter last name"
                    className="w-full rounded-xl border border-[#211917]/20 px-3.5 py-2.5 text-sm focus:border-[#264624] focus:outline-hidden"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-bold uppercase text-[#211917]/70 mb-1.5">
                  Phone Number
                </label>
                <input
                  type="tel"
                  value={editForm.phone}
                  onChange={(e) =>
                    setEditForm({ ...editForm, phone: e.target.value })
                  }
                  placeholder="+255 700 000 000"
                  className="w-full rounded-xl border border-[#211917]/20 px-3.5 py-2.5 text-sm focus:border-[#264624] focus:outline-hidden"
                />
              </div>

              <div>
                <label className="block text-xs font-bold uppercase text-[#211917]/70 mb-1.5">
                  Gender
                </label>
                <select
                  value={editForm.gender}
                  onChange={(e) =>
                    setEditForm({ ...editForm, gender: e.target.value })
                  }
                  className="w-full rounded-xl border border-[#211917]/20 px-3.5 py-2.5 text-sm focus:border-[#264624] focus:outline-hidden bg-white"
                >
                  <option value="MALE">Male</option>
                  <option value="FEMALE">Female</option>
                </select>
              </div>

              <div>
                <label className="block text-xs font-bold uppercase text-[#211917]/70 mb-1.5">
                  Email Address
                </label>
                <input
                  type="email"
                  disabled
                  value={user?.email || ""}
                  className="w-full rounded-xl border border-[#211917]/10 bg-[#FAF8F5] px-3.5 py-2.5 text-sm text-[#211917]/60 cursor-not-allowed"
                />
                <p className="mt-1 text-[11px] text-[#211917]/50">
                  Email is your primary login identifier and is managed by administrators.
                </p>
              </div>

              <div className="flex items-center justify-end gap-2.5 pt-4 border-t border-[#211917]/10">
                <button
                  type="button"
                  onClick={() => setEditModalOpen(false)}
                  className="rounded-xl px-4 py-2 text-xs font-semibold text-[#211917]/70 hover:bg-[#FAF8F5] transition"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={savingProfile}
                  className="rounded-xl bg-[#264624] px-5 py-2.5 text-xs font-bold text-white shadow-xs transition hover:bg-[#1b331a] active:scale-95 disabled:opacity-50"
                >
                  {savingProfile ? "Saving..." : "Save Changes"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* MODAL 2: CHANGE PASSWORD MODAL */}
      {passwordModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4 animate-in fade-in duration-200">
          <div className="w-full max-w-md rounded-3xl border border-[#211917]/10 bg-white p-6 shadow-xl">
            <div className="flex items-center justify-between pb-3 border-b border-[#211917]/10">
              <div className="flex items-center gap-2 text-[#264624]">
                <Key size={20} />
                <h3 className="font-bold text-base font-title">
                  {isTemporary ? "Set Permanent Password" : "Change Password"}
                </h3>
              </div>
              <button
                onClick={() => setPasswordModalOpen(false)}
                className="text-xs font-bold text-[#211917]/50 hover:text-[#211917]"
              >
                ✕
              </button>
            </div>

            <form onSubmit={handlePasswordChange} className="mt-4 space-y-4">
              <div>
                <label className="block text-xs font-bold uppercase text-[#211917]/70 mb-1">
                  New Password
                </label>
                <input
                  type="password"
                  required
                  minLength={4}
                  value={passwordForm.newPassword}
                  onChange={(e) =>
                    setPasswordForm({ ...passwordForm, newPassword: e.target.value })
                  }
                  placeholder="Enter at least 4 characters"
                  className="w-full rounded-xl border border-[#211917]/20 px-3.5 py-2.5 text-sm focus:border-[#264624] focus:outline-hidden"
                />
              </div>

              <div>
                <label className="block text-xs font-bold uppercase text-[#211917]/70 mb-1">
                  Confirm New Password
                </label>
                <input
                  type="password"
                  required
                  minLength={4}
                  value={passwordForm.confirmPassword}
                  onChange={(e) =>
                    setPasswordForm({ ...passwordForm, confirmPassword: e.target.value })
                  }
                  placeholder="Re-type new password"
                  className="w-full rounded-xl border border-[#211917]/20 px-3.5 py-2.5 text-sm focus:border-[#264624] focus:outline-hidden"
                />
              </div>

              <div className="flex items-center justify-end gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setPasswordModalOpen(false)}
                  className="rounded-xl px-4 py-2 text-xs font-semibold text-[#211917]/70 hover:bg-[#FAF8F5]"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={passwordLoading}
                  className="rounded-xl bg-[#264624] px-5 py-2 text-xs font-bold text-white shadow-xs transition hover:bg-[#1b331a] active:scale-95 disabled:opacity-50"
                >
                  {passwordLoading ? "Saving..." : "Save New Password"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* MODAL 3: DELETE ACCOUNT NOTICE MODAL */}
      {deleteModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4 animate-in fade-in duration-200">
          <div className="w-full max-w-md rounded-3xl border border-rose-200 bg-white p-6 shadow-xl">
            <div className="flex items-center gap-2.5 text-rose-600 pb-3 border-b border-[#211917]/10">
              <Warning size={22} />
              <h3 className="font-bold text-base font-title">Account Deletion Policy</h3>
            </div>
            <p className="mt-3 text-xs text-[#211917]/80 leading-relaxed">
              In accordance with enterprise data protection and safari audit regulations, user accounts cannot be deleted directly by individual staff members.
            </p>
            <p className="mt-2 text-xs text-[#211917]/80 leading-relaxed">
              If you require account deactivation or removal, please contact your organization administrator or submit a ticket.
            </p>
            <div className="flex justify-end gap-2 mt-5">
              <button
                type="button"
                onClick={() => setDeleteModalOpen(false)}
                className="rounded-xl bg-[#264624] px-4 py-2 text-xs font-semibold text-white hover:bg-[#1b331a]"
              >
                Understood
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ProfilePage;
