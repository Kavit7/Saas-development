import { useState } from "react";
import { X, CheckCircle, ShieldCheck } from "@phosphor-icons/react";
import { createData } from "../../api/api";
import { useAuth } from "../../hooks/useAuth";

/**
 * ConfirmBookingModal Component
 * Allows reservation managers to record the official confirmation code
 * returned by the property after accepting the reservation.
 */
const ConfirmBookingModal = ({
  open,
  onClose,
  booking,
  onSuccess,
}) => {
  const { token } = useAuth();
  const [confirmationNumber, setConfirmationNumber] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  if (!open || !booking) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!confirmationNumber.trim()) {
      setError("Confirmation number is required.");
      return;
    }

    setSubmitting(true);
    setError(null);

    try {
      const result = await createData(
        `/accommodation-bookings/${booking.id}/confirm`,
        { confirmationNumber: confirmationNumber.trim() },
        token
      );
      if (onSuccess) {
        onSuccess(result);
      }
      onClose();
    } catch (err) {
      setError(err?.message || "Failed to confirm booking.");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-xs animate-in fade-in duration-150">
      <div className="w-full max-w-md rounded-2xl bg-white shadow-xl border border-slate-200 overflow-hidden font-sans animate-in zoom-in-95 duration-150">
        <div className="flex items-center justify-between border-b border-slate-100 px-6 py-4 bg-slate-50/50">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-lg bg-emerald-50 text-emerald-600 flex items-center justify-center">
              <CheckCircle size={18} weight="duotone" />
            </div>
            <div>
              <h3 className="font-bold text-slate-900 font-serif-title">
                Confirm Property Booking
              </h3>
              <p className="text-xs text-slate-500">Ref: {booking.referenceNumber}</p>
            </div>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="rounded-lg p-1.5 text-slate-400 hover:text-slate-600 hover:bg-slate-100 transition"
          >
            <X size={18} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          {error && (
            <div className="p-3 rounded-xl bg-rose-50 border border-rose-200 text-rose-800 text-xs font-medium">
              {error}
            </div>
          )}

          <div className="rounded-xl bg-slate-50 border border-slate-100 p-3 space-y-1 text-xs">
            <div className="flex justify-between">
              <span className="text-slate-500">Property:</span>
              <span className="font-semibold text-slate-800">{booking.propertyName}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-slate-500">Dates:</span>
              <span className="font-semibold text-slate-800">{booking.checkIn} to {booking.checkOut}</span>
            </div>
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">
              Property Confirmation Code <span className="text-rose-500">*</span>
            </label>
            <input
              type="text"
              value={confirmationNumber}
              onChange={(e) => setConfirmationNumber(e.target.value)}
              placeholder="e.g. LODGE-RES-98214"
              required
              autoFocus
              className="w-full rounded-xl border border-slate-200 px-3.5 py-2.5 text-xs text-slate-800 font-semibold focus:border-[#264624] focus:ring-1 focus:ring-[#264624] outline-hidden transition shadow-2xs"
            />
            <p className="text-[11px] text-slate-400 mt-1">
              Enter the official voucher or confirmation reference provided by the lodge.
            </p>
          </div>

          <div className="flex items-center justify-end gap-3 pt-3 border-t border-slate-100">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-xs font-semibold text-slate-600 hover:bg-slate-100 rounded-xl transition"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={submitting}
              className="inline-flex items-center gap-1.5 px-5 py-2.5 rounded-xl bg-emerald-600 text-white text-xs font-bold hover:bg-emerald-700 transition active:scale-95 shadow-xs disabled:opacity-50"
            >
              <ShieldCheck size={16} weight="bold" />
              <span>{submitting ? "Confirming..." : "Verify & Confirm"}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default ConfirmBookingModal;
