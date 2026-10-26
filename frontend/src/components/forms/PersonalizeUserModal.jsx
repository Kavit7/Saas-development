import { useState } from "react";
import {
  UserGear,
  ShieldCheck,
  Warning,
  Key,
  CheckCircle,
  XCircle,
  PencilSimple,
  Ticket,
  Eye,
} from "@phosphor-icons/react";
import { apiRequest } from "../../api/api";

const PersonalizeUserModal = ({
  user: targetUser,
  token,
  isOpen,
  onClose,
  onSuccess,
  onImpersonate,
}) => {
  const [actionType, setActionType] = useState("IMPERSONATE");
  const [reason, setReason] = useState("");
  const [notes, setNotes] = useState("");
  const [firstName, setFirstName] = useState(targetUser?.firstName || "");
  const [lastName, setLastName] = useState(targetUser?.lastName || "");
  const [phone, setPhone] = useState(targetUser?.phone || "");
  const [roleName, setRoleName] = useState(targetUser?.role || targetUser?.roleName || "SALES_PERSON");
  const [status, setStatus] = useState(targetUser?.status || "ACTIVE");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  if (!isOpen || !targetUser) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!reason.trim()) {
      setError("Please provide a mandatory reason for this personalization intervention.");
      return;
    }

    setLoading(true);
    setError("");

    try {
      if (actionType === "IMPERSONATE") {
        const res = await apiRequest(
          `/api/users/user/${targetUser.id}/impersonate`,
          {
            method: "POST",
            body: JSON.stringify({ reason: reason.trim() }),
          },
          token
        );
        onImpersonate?.(res);
        onClose();
        return;
      }

      const payload = {
        actionType,
        reason: reason.trim(),
        notes: notes.trim(),
      };

      if (actionType === "STATUS_CHANGE") {
        payload.status = status;
      } else if (actionType === "DATA_CORRECTION") {
        payload.firstName = firstName.trim();
        payload.lastName = lastName.trim();
        payload.phone = phone.trim();
        payload.roleName = roleName;
      } else if (actionType === "CUSTOM_INTERVENTION") {
        payload.status = status;
      }

      const res = await apiRequest(
        `/api/users/user/${targetUser.id}/personalize`,
        {
          method: "POST",
          body: JSON.stringify(payload),
        },
        token
      );

      onSuccess?.(res);
      onClose();
    } catch (err) {
      setError(err.message || "Failed to execute personalization intervention.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4 backdrop-blur-2xs">
      <div className="w-full max-w-xl max-h-[90vh] overflow-y-auto rounded-3xl border border-[#211917]/15 bg-white p-6 shadow-2xl">
        {/* Header */}
        <div className="flex items-center justify-between border-b border-[#211917]/10 pb-4">
          <div className="flex items-center gap-2.5">
            <span className="flex h-10 w-10 items-center justify-center rounded-2xl bg-[#264624]/10 text-[#264624]">
              <UserGear size={22} weight="duotone" />
            </span>
            <div>
              <h3 className="text-base font-bold text-[#211917] font-title">
                Account Personalization & Support
              </h3>
              <p className="text-xs text-[#211917]/60">
                Administrative intervention with transparent audit support ticket
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="flex h-8 w-8 items-center justify-center rounded-full text-[#211917]/50 hover:bg-[#FAF8F5] hover:text-[#211917]"
          >
            ✕
          </button>
        </div>

        {/* Target User Info */}
        <div className="mt-4 rounded-2xl border border-[#211917]/10 bg-[#FAF8F5] p-3.5 text-xs">
          <div className="grid grid-cols-2 gap-2 sm:grid-cols-3">
            <div>
              <span className="text-[#211917]/50 block">Target User</span>
              <span className="font-bold text-[#211917]">
                {targetUser.firstName} {targetUser.lastName}
              </span>
            </div>
            <div>
              <span className="text-[#211917]/50 block">Email</span>
              <span className="font-mono text-[#211917] truncate block">{targetUser.email}</span>
            </div>
            <div>
              <span className="text-[#211917]/50 block">Company</span>
              <span className="font-semibold text-[#211917]">{targetUser.companyName || "N/A"}</span>
            </div>
            <div>
              <span className="text-[#211917]/50 block">Current Status</span>
              <span
                className={`font-bold inline-block px-2 py-0.5 rounded-full text-[10px] ${
                  targetUser.status === "ACTIVE"
                    ? "bg-emerald-100 text-emerald-800"
                    : "bg-rose-100 text-rose-800"
                }`}
              >
                {targetUser.status}
              </span>
            </div>
            <div>
              <span className="text-[#211917]/50 block">Current Role</span>
              <span className="font-semibold text-[#211917]">{targetUser.role || "N/A"}</span>
            </div>
          </div>
        </div>

        {error && (
          <div className="mt-3 rounded-xl border border-rose-200 bg-rose-50 p-3 text-xs font-semibold text-rose-800">
            {error}
          </div>
        )}

        {/* Form */}
        <form onSubmit={handleSubmit} className="mt-4 space-y-4">
          {/* Action Selector */}
          <div>
            <label className="block text-xs font-bold uppercase tracking-wider text-[#211917]/70 mb-1.5">
              Select Personalization Action
            </label>
            <div className="grid grid-cols-1 gap-2 sm:grid-cols-2">
              <button
                type="button"
                onClick={() => setActionType("IMPERSONATE")}
                className={`flex items-start gap-2.5 rounded-2xl border p-3.5 text-left transition sm:col-span-2 ${
                  actionType === "IMPERSONATE"
                    ? "border-amber-600 bg-amber-50/90 text-amber-950 ring-2 ring-amber-500/30"
                    : "border-[#211917]/10 bg-white text-[#211917]/70 hover:bg-[#FAF8F5]"
                }`}
              >
                <Eye className="mt-0.5 shrink-0 text-amber-700" size={20} weight="bold" />
                <div>
                  <div className="font-bold text-xs flex items-center gap-1.5 text-amber-950">
                    Ingia kama Mtumiaji (Personalize / Impersonate Session)
                    <span className="rounded-full bg-amber-200 px-2 py-0.5 text-[10px] font-black uppercase text-amber-900">
                      Primary
                    </span>
                  </div>
                  <div className="text-[11px] text-amber-900/80 mt-0.5">
                    Ingia moja kwa moja kwenye akaunti ya mtumiaji huyu na uone mfumo jinsi anavyouona yeye ili kutatua changamoto zake (Support ticket itatengenezwa kiotomatiki kwa uwazi).
                  </div>
                </div>
              </button>

              <button
                type="button"
                onClick={() => setActionType("PASSWORD_RESET")}
                className={`flex items-start gap-2.5 rounded-2xl border p-3 text-left transition ${
                  actionType === "PASSWORD_RESET"
                    ? "border-[#264624] bg-[#264624]/5 text-[#264624]"
                    : "border-[#211917]/10 bg-white text-[#211917]/70 hover:bg-[#FAF8F5]"
                }`}
              >
                <Key className="mt-0.5 shrink-0" size={18} />
                <div>
                  <div className="font-bold text-xs">Reset Default Password</div>
                  <div className="text-[11px] opacity-80">
                    Temporary password (last name), expires in 3 days
                  </div>
                </div>
              </button>

              <button
                type="button"
                onClick={() => setActionType("STATUS_CHANGE")}
                className={`flex items-start gap-2.5 rounded-2xl border p-3 text-left transition ${
                  actionType === "STATUS_CHANGE"
                    ? "border-[#264624] bg-[#264624]/5 text-[#264624]"
                    : "border-[#211917]/10 bg-white text-[#211917]/70 hover:bg-[#FAF8F5]"
                }`}
              >
                <ShieldCheck className="mt-0.5 shrink-0" size={18} />
                <div>
                  <div className="font-bold text-xs">Toggle / Update Status</div>
                  <div className="text-[11px] opacity-80">
                    Activate, unlock, or suspend user access
                  </div>
                </div>
              </button>

              <button
                type="button"
                onClick={() => setActionType("DATA_CORRECTION")}
                className={`flex items-start gap-2.5 rounded-2xl border p-3 text-left transition ${
                  actionType === "DATA_CORRECTION"
                    ? "border-[#264624] bg-[#264624]/5 text-[#264624]"
                    : "border-[#211917]/10 bg-white text-[#211917]/70 hover:bg-[#FAF8F5]"
                }`}
              >
                <PencilSimple className="mt-0.5 shrink-0" size={18} />
                <div>
                  <div className="font-bold text-xs">Data & Phone Correction</div>
                  <div className="text-[11px] opacity-80">
                    Fix phone format, name, or role mismatch
                  </div>
                </div>
              </button>

              <button
                type="button"
                onClick={() => setActionType("CUSTOM_INTERVENTION")}
                className={`flex items-start gap-2.5 rounded-2xl border p-3 text-left transition ${
                  actionType === "CUSTOM_INTERVENTION"
                    ? "border-[#264624] bg-[#264624]/5 text-[#264624]"
                    : "border-[#211917]/10 bg-white text-[#211917]/70 hover:bg-[#FAF8F5]"
                }`}
              >
                <UserGear className="mt-0.5 shrink-0" size={18} />
                <div>
                  <div className="font-bold text-xs">Custom Support Action</div>
                  <div className="text-[11px] opacity-80">
                    Comprehensive personalization with notes
                  </div>
                </div>
              </button>
            </div>
          </div>

          {/* Dynamic Action Fields */}
          {actionType === "STATUS_CHANGE" && (
            <div>
              <label className="block text-xs font-bold uppercase text-[#211917]/70 mb-1">
                New User Status
              </label>
              <select
                value={status}
                onChange={(e) => setStatus(e.target.value)}
                className="w-full rounded-xl border border-[#211917]/20 px-3.5 py-2.5 text-xs font-semibold focus:border-[#264624] focus:outline-hidden"
              >
                <option value="ACTIVE">ACTIVE (Account Unlocked & Working)</option>
                <option value="INACTIVE">INACTIVE (Account Suspended / Blocked)</option>
              </select>
            </div>
          )}

          {actionType === "DATA_CORRECTION" && (
            <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 rounded-2xl border border-[#211917]/10 bg-[#FAF8F5]/50 p-3.5">
              <div>
                <label className="block text-[11px] font-bold uppercase text-[#211917]/70 mb-1">
                  First Name
                </label>
                <input
                  type="text"
                  value={firstName}
                  onChange={(e) => setFirstName(e.target.value)}
                  className="w-full rounded-xl border border-[#211917]/20 px-3 py-2 text-xs focus:border-[#264624] focus:outline-hidden"
                />
              </div>

              <div>
                <label className="block text-[11px] font-bold uppercase text-[#211917]/70 mb-1">
                  Last Name
                </label>
                <input
                  type="text"
                  value={lastName}
                  onChange={(e) => setLastName(e.target.value)}
                  className="w-full rounded-xl border border-[#211917]/20 px-3 py-2 text-xs focus:border-[#264624] focus:outline-hidden"
                />
              </div>

              <div>
                <label className="block text-[11px] font-bold uppercase text-[#211917]/70 mb-1">
                  Phone (+255 format)
                </label>
                <input
                  type="tel"
                  value={phone}
                  onChange={(e) => setPhone(e.target.value)}
                  placeholder="+255 712 345 678"
                  className="w-full rounded-xl border border-[#211917]/20 px-3 py-2 text-xs focus:border-[#264624] focus:outline-hidden"
                />
              </div>

              <div>
                <label className="block text-[11px] font-bold uppercase text-[#211917]/70 mb-1">
                  Role
                </label>
                <select
                  value={roleName}
                  onChange={(e) => setRoleName(e.target.value)}
                  className="w-full rounded-xl border border-[#211917]/20 px-3 py-2 text-xs focus:border-[#264624] focus:outline-hidden"
                >
                  <option value="ADMIN">Company Admin</option>
                  <option value="SALES_PERSON">Sales Person</option>
                  <option value="RESERVATION_MANAGER">Reservation Manager</option>
                  <option value="GUIDE">Guide</option>
                </select>
              </div>
            </div>
          )}

          {/* Mandatory Reason */}
          <div>
            <label className="block text-xs font-bold uppercase tracking-wider text-[#211917]/70 mb-1">
              Mandatory Support Reason / Justification <span className="text-red-500">*</span>
            </label>
            <textarea
              required
              rows={2}
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              placeholder="e.g. User called support line unable to login; requested password reset due to locked phone."
              className="w-full rounded-xl border border-[#211917]/20 px-3.5 py-2 text-xs focus:border-[#264624] focus:outline-hidden"
            />
          </div>

          {/* Notes */}
          <div>
            <label className="block text-xs font-bold uppercase tracking-wider text-[#211917]/70 mb-1">
              Resolution Notes (Optional)
            </label>
            <textarea
              rows={2}
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              placeholder="e.g. Verified caller identity via registered email. Temporary credentials issued."
              className="w-full rounded-xl border border-[#211917]/20 px-3.5 py-2 text-xs focus:border-[#264624] focus:outline-hidden"
            />
          </div>

          {/* Transparency & Security Notice */}
          <div className="flex items-start gap-2.5 rounded-2xl border border-amber-200 bg-amber-50/80 p-3.5 text-xs text-amber-900">
            <Ticket className="mt-0.5 shrink-0 text-amber-700" size={18} />
            <div className="leading-relaxed">
              <span className="font-bold">Customer Transparency Guarantee:</span> A formal Support Ticket will be automatically created under this user's account with your justification notes. The user and their company admin will see this ticket in their Support Ticket dashboard.
            </div>
          </div>

          {/* Actions */}
          <div className="flex items-center justify-end gap-3 pt-2 border-t border-[#211917]/10">
            <button
              type="button"
              onClick={onClose}
              className="rounded-xl px-4 py-2 text-xs font-semibold text-[#211917]/70 hover:bg-[#FAF8F5]"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className={`inline-flex items-center gap-1.5 rounded-xl px-5 py-2.5 text-xs font-bold text-white shadow-xs transition active:scale-95 disabled:opacity-50 ${
                actionType === "IMPERSONATE"
                  ? "bg-amber-600 hover:bg-amber-700"
                  : "bg-[#264624] hover:bg-[#1b331a]"
              }`}
            >
              {actionType === "IMPERSONATE" ? (
                <Eye size={16} weight="bold" />
              ) : (
                <Ticket size={16} />
              )}
              <span>
                {loading
                  ? "Inatekeleza..."
                  : actionType === "IMPERSONATE"
                  ? `Ingia kama ${targetUser.firstName || "Mtumiaji"} (Anza Personalization)`
                  : "Tekeleza Marekebisho & Tengeneza Tiketi"}
              </span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default PersonalizeUserModal;
