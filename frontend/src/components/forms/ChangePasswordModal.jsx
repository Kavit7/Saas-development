import { useState } from "react";
import { LockKey, Eye, EyeSlash, X, CheckCircle } from "@phosphor-icons/react";
import { apiRequest } from "../../api/api";
import { useAuth } from "../../hooks/useAuth";

const ChangePasswordModal = ({ isOpen, onClose }) => {
  const { token, refreshProfile } = useAuth();
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);

  if (!isOpen) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);

    if (newPassword.length < 4) {
      setError("Nenosiri jipya lazima liwe na angalau herufi 4.");
      return;
    }

    if (newPassword !== confirmPassword) {
      setError("Nenosiri jipya na uthibitisho haviendani.");
      return;
    }

    setLoading(true);
    try {
      await apiRequest(
        "/api/v1/auth/change-password",
        {
          method: "POST",
          body: JSON.stringify({ newPassword }),
        },
        token
      );
      setSuccess(true);
      if (refreshProfile) {
        await refreshProfile();
      }
      setTimeout(() => {
        setSuccess(false);
        setNewPassword("");
        setConfirmPassword("");
        onClose();
      }, 1500);
    } catch (err) {
      setError(err.message || "Imeshindikana kubadilisha nenosiri.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4">
      <div className="relative w-full max-w-md overflow-hidden rounded-2xl bg-white shadow-2xl">
        <div className="flex items-center justify-between border-b border-[#211917]/10 bg-[#FAF8F5] px-6 py-4">
          <div className="flex items-center gap-2">
            <span className="flex h-9 w-9 items-center justify-center rounded-xl bg-[#264624]/10 text-[#264624]">
              <LockKey size={20} weight="fill" />
            </span>
            <div>
              <h3 className="font-serif-title text-base font-bold text-[#264624]">
                Weka Nenosiri Jipya
              </h3>
              <p className="text-xs text-[#211917]/60">Badilisha nenosiri lako la muda</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="flex h-8 w-8 items-center justify-center rounded-full text-[#211917]/50 transition hover:bg-[#211917]/10 hover:text-[#211917]"
          >
            <X size={18} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          {error && (
            <div className="rounded-xl border border-red-200 bg-red-50 p-3 text-xs font-semibold text-red-800">
              {error}
            </div>
          )}

          {success && (
            <div className="flex items-center gap-2 rounded-xl border border-emerald-200 bg-emerald-50 p-3 text-xs font-semibold text-emerald-800">
              <CheckCircle size={18} weight="fill" />
              <span>Nenosiri limebadilishwa kikamilifu! Sasa una nenosiri la kudumu.</span>
            </div>
          )}

          <div>
            <label className="block text-xs font-bold uppercase tracking-wider text-[#211917]/70 mb-1">
              Nenosiri Jipya *
            </label>
            <div className="relative">
              <input
                type={showPassword ? "text" : "password"}
                required
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                placeholder="Weka nenosiri jipya"
                className="w-full rounded-xl border border-[#211917]/20 bg-[#FAF8F5] py-2.5 pl-3.5 pr-10 text-sm text-[#211917] outline-none transition focus:border-[#264624] focus:bg-white"
              />
              <button
                type="button"
                onClick={() => setShowPassword((prev) => !prev)}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-[#211917]/50 hover:text-[#211917]"
              >
                {showPassword ? <EyeSlash size={18} /> : <Eye size={18} />}
              </button>
            </div>
          </div>

          <div>
            <label className="block text-xs font-bold uppercase tracking-wider text-[#211917]/70 mb-1">
              Thibitisha Nenosiri Jipya *
            </label>
            <input
              type={showPassword ? "text" : "password"}
              required
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              placeholder="Rudia nenosiri jipya"
              className="w-full rounded-xl border border-[#211917]/20 bg-[#FAF8F5] py-2.5 px-3.5 text-sm text-[#211917] outline-none transition focus:border-[#264624] focus:bg-white"
            />
          </div>

          <p className="text-[11px] text-[#211917]/60">
            Ukishaweka nenosiri hili, linaanza kutumika mara moja na halita-expire tena baada ya siku 3.
          </p>

          <div className="flex items-center justify-end gap-2 border-t border-[#211917]/10 pt-4">
            <button
              type="button"
              onClick={onClose}
              disabled={loading}
              className="rounded-xl border border-[#211917]/20 px-4 py-2 text-xs font-semibold text-[#211917]/70 hover:bg-[#211917]/5"
            >
              Baadaye
            </button>
            <button
              type="submit"
              disabled={loading || success}
              className="rounded-xl bg-[#264624] px-5 py-2 text-xs font-bold text-white shadow-sm transition hover:bg-[#1b331a] disabled:opacity-50"
            >
              {loading ? "Inahifadhi..." : "Hifadhi Nenosiri"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default ChangePasswordModal;
