import { useState } from "react";
import { 
  PaperPlaneTilt, 
  CheckCircle, 
  XCircle, 
  ShieldCheck, 
  Bed, 
  ArrowsClockwise,
  Eye
} from "@phosphor-icons/react";
import ConfirmBookingModal from "../bookings/ConfirmBookingModal";
import BookingEmailPreviewModal from "../bookings/BookingEmailPreviewModal";

/**
 * BookingActionBanner Component
 * Renders executive status transitions and action controls directly on the
 * booking detail page so reservation managers can preview, dispatch, confirm, or decline bookings.
 */
const BookingActionBanner = ({
  booking,
  canManage = false,
  loading = false,
  onSend,
  onConfirmSuccess,
  onDecline,
}) => {
  const [confirmModalOpen, setConfirmModalOpen] = useState(false);
  const [previewModalOpen, setPreviewModalOpen] = useState(false);

  if (!booking) return null;

  const status = String(booking?.status || "").toUpperCase();
  const isDraft = status === "DRAFT";
  const isProvisional = status === "PROVISIONAL";
  const isConfirmed = status === "CONFIRMED";
  const isCancelled = status === "CANCELLED";

  const getStatusColor = () => {
    if (isConfirmed) return "bg-emerald-50 text-emerald-700 border-emerald-200";
    if (isProvisional) return "bg-amber-50 text-amber-700 border-amber-200";
    if (isCancelled) return "bg-rose-50 text-rose-700 border-rose-200";
    return "bg-indigo-50 text-[#101B82] border-indigo-200";
  };

  return (
    <>
      <div className="rounded-2xl border border-slate-200/90 bg-white p-5 sm:p-6 shadow-xs flex flex-col sm:flex-row sm:items-center justify-between gap-4 font-sans">
        <div className="flex items-start gap-4">
          <div
            className={`flex h-12 w-12 shrink-0 items-center justify-center rounded-2xl border ${
              isConfirmed
                ? "bg-emerald-50 text-emerald-600 border-emerald-200"
                : isProvisional
                ? "bg-amber-50 text-amber-600 border-amber-200"
                : isCancelled
                ? "bg-rose-50 text-rose-600 border-rose-200"
                : "bg-indigo-50 text-[#101B82] border-indigo-200"
            }`}
          >
            <Bed size={26} weight="duotone" />
          </div>

          <div>
            <div className="flex flex-wrap items-center gap-2.5">
              <h4 className="text-base font-bold text-slate-900 font-serif-title">
                Booking Dispatch & Allocation Status
              </h4>
              <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold uppercase tracking-wider border ${getStatusColor()}`}>
                {booking.status || "DRAFT"}
              </span>
            </div>

            <p className="mt-1.5 text-xs text-slate-500 leading-relaxed max-w-xl">
              {isConfirmed
                ? `Booking confirmed with property '${booking.propertyName}'. Official confirmation code: ${booking.confirmationNumber || "VERIFIED"}.`
                : isProvisional
                ? `Inquiry dispatched to '${booking.propertyName}'. Awaiting property response or confirmation number.`
                : isCancelled
                ? `Booking was declined or cancelled. You can allocate a new partner property.`
                : `Draft reservation file created. Dispatch the formal inquiry to notify the partner lodge.`}
            </p>
          </div>
        </div>

        {canManage && (
          <div className="flex flex-wrap items-center gap-2.5 self-end sm:self-auto">
            {/* Preview Email button */}
            <button
              type="button"
              onClick={() => setPreviewModalOpen(true)}
              className="inline-flex items-center gap-1.5 rounded-xl border border-indigo-200 bg-indigo-50/70 px-3.5 py-2.5 text-xs font-bold text-[#101B82] hover:bg-indigo-100 transition active:scale-95 shadow-2xs"
            >
              <Eye size={15} weight="bold" />
              <span>Preview Email</span>
            </button>

            {/* DRAFT -> Send */}
            {isDraft && (
              <button
                type="button"
                onClick={onSend}
                disabled={loading}
                className="inline-flex items-center gap-1.5 rounded-xl bg-[#101B82] px-4 py-2.5 text-xs font-bold text-white hover:bg-[#0c145e] transition active:scale-95 shadow-2xs disabled:opacity-50"
              >
                {loading ? (
                  <ArrowsClockwise size={15} className="animate-spin" />
                ) : (
                  <PaperPlaneTilt size={15} weight="bold" />
                )}
                <span>Send Request to Lodge</span>
              </button>
            )}

            {/* PROVISIONAL -> Confirm or Decline */}
            {isProvisional && (
              <>
                <button
                  type="button"
                  onClick={onDecline}
                  disabled={loading}
                  className="inline-flex items-center gap-1 rounded-xl border border-rose-200 bg-rose-50 px-3.5 py-2 text-xs font-bold text-rose-700 hover:bg-rose-100 transition active:scale-95 disabled:opacity-50"
                >
                  <XCircle size={15} weight="bold" />
                  <span>Decline</span>
                </button>

                <button
                  type="button"
                  onClick={() => setConfirmModalOpen(true)}
                  disabled={loading}
                  className="inline-flex items-center gap-1.5 rounded-xl bg-emerald-600 px-4 py-2 text-xs font-bold text-white hover:bg-emerald-700 transition active:scale-95 shadow-2xs disabled:opacity-50"
                >
                  <CheckCircle size={15} weight="bold" />
                  <span>Enter Confirmation Code</span>
                </button>
              </>
            )}

            {isConfirmed && (
              <div className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-emerald-50 border border-emerald-200 text-emerald-800 text-xs font-bold">
                <ShieldCheck size={16} weight="bold" className="text-emerald-600" />
                <span>Code: {booking.confirmationNumber}</span>
              </div>
            )}
          </div>
        )}
      </div>

      <ConfirmBookingModal
        open={confirmModalOpen}
        onClose={() => setConfirmModalOpen(false)}
        booking={booking}
        onSuccess={onConfirmSuccess}
      />

      <BookingEmailPreviewModal
        open={previewModalOpen}
        onClose={() => setPreviewModalOpen(false)}
        booking={booking}
        onSend={onSend}
        sending={loading}
      />
    </>
  );
};

export default BookingActionBanner;
