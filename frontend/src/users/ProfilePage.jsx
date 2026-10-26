import { useState } from "react";
import { useAuth } from "../hooks/useAuth";
import { apiRequest } from "../api/api";
import {
  User,
  Envelope,
  Phone,
  Briefcase,
  ShieldCheck,
  Key,
  Clock,
  CheckCircle,
  Warning,
  ArrowsClockwise,
} from "@phosphor-icons/react";

const ProfilePage = () => {
  const { user, token, refreshProfile } = useAuth();
  const [modalOpen, setModalOpen] = useState(false);
  const [passwordForm, setPasswordForm] = useState({
    currentPassword: "",
    newPassword: "",
    confirmPassword: "",
  });
  const [loading, setLoading] = useState(false);
  const [refreshing, setRefreshing] = useState(false);
  const [message, setMessage] = useState({ text: "", type: "" });

  const role = String(user?.role_name || user?.role || "")
    .toUpperCase()
    .replace(/^ROLE_/, "")
    .replace(/_/g, " ")
    .trim();

  const handleRefresh = async () => {
    setRefreshing(true);
    try {
      await refreshProfile();
      setMessage({ text: "Profile updated successfully.", type: "success" });
    } catch {
      setMessage({ text: "Failed to reload profile.", type: "error" });
    } finally {
      setRefreshing(false);
    }
  };

  const handlePasswordChange = async (e) => {
    e.preventDefault();
    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      setMessage({ text: "New passwords do not match.", type: "error" });
      return;
    }
    if (passwordForm.newPassword.length < 4) {
      setMessage({ text: "New password must be at least 4 characters long.", type: "error" });
      return;
    }

    setLoading(true);
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
      setModalOpen(false);
      await refreshProfile();
    } catch (err) {
      setMessage({ text: err.message || "Failed to change password.", type: "error" });
    } finally {
      setLoading(false);
    }
  };

  const isTemporary = Boolean(user?.mustChangePassword);

  return (
    <div className="mx-auto max-w-5xl space-y-6 p-4 sm:p-6 lg:p-8">
      {/* Top Banner / Actions */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-black tracking-tight text-[#264624] font-title sm:text-3xl">
            My Account & Profile
          </h1>
          <p className="mt-1 text-sm text-[#211917]/70">
            View your personal credentials, assigned system role, and security settings.
          </p>
        </div>

        <button
          onClick={handleRefresh}
          disabled={refreshing}
          className="inline-flex items-center gap-2 rounded-xl border border-[#211917]/15 bg-white px-4 py-2 text-xs font-semibold text-[#264624] shadow-xs transition hover:bg-[#FAF8F5] active:scale-95 disabled:opacity-50"
        >
          <ArrowsClockwise className={refreshing ? "animate-spin" : ""} size={16} />
          <span>{refreshing ? "Refreshing..." : "Refresh"}</span>
        </button>
      </div>

      {message.text && (
        <div
          className={`rounded-2xl border p-4 text-sm font-medium ${
            message.type === "success"
              ? "border-emerald-200 bg-emerald-50 text-emerald-800"
              : "border-rose-200 bg-rose-50 text-rose-800"
          }`}
        >
          {message.text}
        </div>
      )}

      {/* Profile Header Card */}
      <div className="overflow-hidden rounded-3xl border border-[#211917]/10 bg-white shadow-xs">
        <div className="h-28 bg-gradient-to-r from-[#264624] to-[#3b6638]" />
        <div className="relative px-6 pb-6 pt-0 sm:px-8">
          <div className="-mt-14 mb-4 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
            <div className="flex items-end gap-4">
              <div className="flex h-24 w-24 shrink-0 items-center justify-center rounded-2xl border-4 border-white bg-[#264624] text-3xl font-black text-white shadow-md">
                {(user?.firstName?.charAt(0) || user?.email?.charAt(0) || "U").toUpperCase()}
              </div>
              <div className="pb-1">
                <h2 className="text-xl font-bold text-[#211917] sm:text-2xl">
                  {user?.firstName ? `${user.firstName} ${user.lastName || ""}` : user?.email || "User Profile"}
                </h2>
                <p className="text-xs text-[#211917]/60 font-mono">{user?.email}</p>
              </div>
            </div>

            <div className="flex flex-wrap items-center gap-2">
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
      </div>

      {/* Details Grid */}
      <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
        {/* Contact & Company Details */}
        <div className="rounded-3xl border border-[#211917]/10 bg-white p-6 shadow-xs space-y-4">
          <h3 className="text-sm font-bold uppercase tracking-wider text-[#211917]/60">
            Account Information
          </h3>

          <div className="space-y-3">
            <div className="flex items-start gap-3 rounded-2xl border border-[#211917]/5 bg-[#FAF8F5] p-3.5">
              <User className="mt-0.5 text-[#264624]" size={18} />
              <div className="min-w-0">
                <p className="text-[11px] font-medium text-[#211917]/60">Full Name</p>
                <p className="font-semibold text-sm text-[#211917]">
                  {user?.firstName ? `${user.firstName} ${user.lastName || ""}` : "Not set"}
                </p>
              </div>
            </div>

            <div className="flex items-start gap-3 rounded-2xl border border-[#211917]/5 bg-[#FAF8F5] p-3.5">
              <Envelope className="mt-0.5 text-[#264624]" size={18} />
              <div className="min-w-0">
                <p className="text-[11px] font-medium text-[#211917]/60">Email Address</p>
                <p className="font-semibold text-sm text-[#211917] truncate">{user?.email || "—"}</p>
              </div>
            </div>

            <div className="flex items-start gap-3 rounded-2xl border border-[#211917]/5 bg-[#FAF8F5] p-3.5">
              <Phone className="mt-0.5 text-[#264624]" size={18} />
              <div className="min-w-0">
                <p className="text-[11px] font-medium text-[#211917]/60">Phone Number</p>
                <p className="font-semibold text-sm text-[#211917]">{user?.phone || "No phone registered"}</p>
              </div>
            </div>

            <div className="flex items-start gap-3 rounded-2xl border border-[#211917]/5 bg-[#FAF8F5] p-3.5">
              <Briefcase className="mt-0.5 text-[#264624]" size={18} />
              <div className="min-w-0">
                <p className="text-[11px] font-medium text-[#211917]/60">Company / Tenant</p>
                <p className="font-semibold text-sm text-[#211917]">
                  {user?.companyName || "Global Platform"}
                </p>
              </div>
            </div>
          </div>
        </div>

        {/* Security & Password Card */}
        <div className="rounded-3xl border border-[#211917]/10 bg-white p-6 shadow-xs flex flex-col justify-between">
          <div className="space-y-4">
            <h3 className="text-sm font-bold uppercase tracking-wider text-[#211917]/60">
              Security & Credentials
            </h3>

            {isTemporary ? (
              <div className="rounded-2xl border border-amber-300 bg-amber-50 p-4 text-amber-900">
                <div className="flex items-start gap-3">
                  <Warning className="shrink-0 text-amber-600 mt-0.5" size={20} />
                  <div>
                    <h4 className="font-bold text-sm">Temporary Default Password</h4>
                    <p className="mt-1 text-xs text-amber-800 leading-relaxed">
                      You are currently using your temporary default password (last name). For security, this password will expire in 3 days. Please change your password to make it permanent.
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

            <div className="rounded-2xl border border-[#211917]/10 bg-[#FAF8F5] p-4 text-xs text-[#211917]/70 space-y-1.5">
              <p className="font-semibold text-[#211917]">Security Recommendations:</p>
              <ul className="list-disc pl-4 space-y-1">
                <li>Never share your credentials with colleagues.</li>
                <li>Each team member must log in with their own registered account.</li>
                <li>Admins can reset passwords for staff if locked out.</li>
              </ul>
            </div>
          </div>

          <div className="mt-6">
            <button
              onClick={() => setModalOpen(true)}
              className="w-full inline-flex items-center justify-center gap-2 rounded-2xl bg-[#264624] px-5 py-3 text-sm font-bold text-white shadow-sm transition hover:bg-[#1b331a] active:scale-95"
            >
              <Key size={18} />
              <span>{isTemporary ? "Set Permanent Password Now" : "Change Password"}</span>
            </button>
          </div>
        </div>
      </div>

      {/* Change Password Modal */}
      {modalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
          <div className="w-full max-w-md rounded-3xl border border-[#211917]/10 bg-white p-6 shadow-xl">
            <div className="flex items-center justify-between pb-3 border-b border-[#211917]/10">
              <div className="flex items-center gap-2 text-[#264624]">
                <Key size={20} />
                <h3 className="font-bold text-base font-title">
                  {isTemporary ? "Set Permanent Password" : "Change Password"}
                </h3>
              </div>
              <button
                onClick={() => setModalOpen(false)}
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
                  onClick={() => setModalOpen(false)}
                  className="rounded-xl px-4 py-2 text-xs font-semibold text-[#211917]/70 hover:bg-[#FAF8F5]"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={loading}
                  className="rounded-xl bg-[#264624] px-5 py-2 text-xs font-bold text-white shadow-xs transition hover:bg-[#1b331a] active:scale-95 disabled:opacity-50"
                >
                  {loading ? "Saving..." : "Save New Password"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default ProfilePage;
