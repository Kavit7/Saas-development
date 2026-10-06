import { useState } from "react";
import { 
  X, 
  Bed, 
  CalendarCheck, 
  PaperPlaneTilt, 
  CheckCircle, 
  XCircle, 
  MapPin, 
  Compass, 
  User,
  Note,
  Eye
} from "@phosphor-icons/react";
import { apiRequest } from "../../api/api";
import { useAuth } from "../../hooks/useAuth";
import ConfirmBookingModal from "./ConfirmBookingModal";
import BookingEmailPreviewModal from "./BookingEmailPreviewModal";

/**
 * BookingDetailModal Component
 * Presents full operational details of an accommodation booking, including
 * status flow transitions (Send to Lodge, Confirm with Code, Decline/Cancel).
 */
const BookingDetailModal = ({
  open,
  onClose,
  booking,
  onUpdated,
}) => {
  const { token, hasAccess } = useAuth();
  const [actionLoading, setActionLoading] = useState(false);
  const [confirmModalOpen, setConfirmModalOpen] = useState(false);
  const [previewModalOpen, setPreviewModalOpen] = useState(false);
  const [error, setError] = useState(null);

  if (!open || !booking) return null;

  const canManage = hasAccess(["ADMIN", "RESERVATION_MANAGER", "SUPER_ADMIN"]);

  // Handler for dispatching request to lodge
  const handleSendToLodge = async () => {
    setActionLoading(true);
    setError(null);
    try {
      const res = await apiRequest(`/accommodation-bookings/${booking.id}/send`, {
        method: "POST",
      }, token);
      if (onUpdated) onUpdated(res);
      onClose();
    } catch (err) {
      setError(err?.message || "Failed to dispatch booking request.");
    } finally {
      setActionLoading(false);
    }
  };

  // Handler for declining booking
  const handleDecline = async () => {
    if (!window.confirm("Are you sure you want to decline or cancel this booking?")) return;
    setActionLoading(true);
    setError(null);
    try {
      const res = await apiRequest(`/accommodation-bookings/${booking.id}/decline`, {
        method: "POST",
      }, token);
      if (onUpdated) onUpdated(res);
      onClose();
    } catch (err) {
      setError(err?.message || "Failed to decline booking.");
    } finally {
      setActionLoading(false);
    }
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case "CONFIRMED":
        return "bg-emerald-50 text-emerald-700 border-emerald-200";
      case "PROVISIONAL":
        return "bg-amber-50 text-amber-700 border-amber-200";
      case "CANCELLED":
        return "bg-rose-50 text-rose-700 border-rose-200";
      case "DRAFT":
      default:
        return "bg-slate-100 text-slate-700 border-slate-200";
    }
  };

  return (
    <>
      <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-xs animate-in fade-in duration-150">
        <div className="w-full max-w-xl rounded-2xl bg-white shadow-xl border border-slate-200 overflow-hidden font-sans animate-in zoom-in-95 duration-150">
          {/* Header */}
          <div className="flex items-center justify-between border-b border-slate-100 px-6 py-4 bg-slate-50/50">
            <div className="flex items-center gap-2.5">
              <div className="w-8 h-8 rounded-lg bg-indigo-50 text-[#101B82] flex items-center justify-center font-bold">
                <Bed size={18} weight="duotone" />
              </div>
              <div>
                <h3 className="font-bold text-slate-900 font-serif-title">
                  {booking.referenceNumber}
                </h3>
                <p className="text-xs text-slate-500">Lodge Accommodation File</p>
              </div>
            </div>
            <div className="flex items-center gap-3">
              <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-bold uppercase tracking-wider border ${getStatusBadge(booking.status)}`}>
                {booking.status}
              </span>
              <button
                type="button"
                onClick={onClose}
                className="rounded-lg p-1.5 text-slate-400 hover:text-slate-600 hover:bg-slate-100 transition"
              >
                <X size={18} />
              </button>
            </div>
          </div>

          {/* Body */}
          <div className="p-6 space-y-5">
            {error && (
              <div className="p-3 rounded-xl bg-rose-50 border border-rose-200 text-rose-800 text-xs font-medium">
                {error}
              </div>
            )}

            {/* Quick Metrics */}
            <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
              <div className="p-3 rounded-xl bg-slate-50 border border-slate-100">
                <span className="block text-[10px] font-bold uppercase tracking-wider text-slate-400">
                  Lodge / Property
                </span>
                <span className="mt-1 block font-bold text-slate-900 text-sm font-serif-title">
                  {booking.propertyName || "Lodge"}
                </span>
              </div>

              <div className="p-3 rounded-xl bg-slate-50 border border-slate-100">
                <span className="block text-[10px] font-bold uppercase tracking-wider text-slate-400">
                  Destination
                </span>
                <span className="mt-1 block font-bold text-slate-900 text-sm font-serif-title flex items-center gap-1">
                  <MapPin size={14} className="text-[#101B82]" weight="fill" />
                  {booking.destination || "Circuit"}
                </span>
              </div>

              <div className="p-3 rounded-xl bg-slate-50 border border-slate-100">
                <span className="block text-[10px] font-bold uppercase tracking-wider text-slate-400">
                  Confirmation #
                </span>
                <span className="mt-1 block font-bold text-emerald-700 text-sm">
                  {booking.confirmationNumber || "Pending Code"}
                </span>
              </div>
            </div>

            {/* Detailed Info */}
            <div className="space-y-2.5 text-xs text-slate-600 border border-slate-100 rounded-xl p-4 bg-slate-50/40">
              <div className="flex justify-between py-1 border-b border-slate-100">
                <span className="text-slate-400 font-medium">Safari File:</span>
                <span className="font-semibold text-slate-800">{booking.safariReference || "Safari"}</span>
              </div>
              <div className="flex justify-between py-1 border-b border-slate-100">
                <span className="text-slate-400 font-medium">Traveler / Group:</span>
                <span className="font-semibold text-slate-800">{booking.clientName || "Direct Client"}</span>
              </div>
              <div className="flex justify-between py-1 border-b border-slate-100">
                <span className="text-slate-400 font-medium">Check-in:</span>
                <span className="font-semibold text-slate-800">{booking.checkIn}</span>
              </div>
              <div className="flex justify-between py-1 border-b border-slate-100">
                <span className="text-slate-400 font-medium">Check-out:</span>
                <span className="font-semibold text-slate-800">{booking.checkOut}</span>
              </div>
              <div className="flex justify-between py-1 border-b border-slate-100">
                <span className="text-slate-400 font-medium">Rooms Requested:</span>
                <span className="font-semibold text-slate-800">{booking.roomsCount || 1} Room(s)</span>
              </div>
              <div className="flex justify-between py-1">
                <span className="text-slate-400 font-medium">Assigned Officer:</span>
                <span className="font-semibold text-slate-800">{booking.reservationManagerName || "Staff"}</span>
              </div>
            </div>

            {/* Notes */}
            {booking.notes && (
              <div className="rounded-xl bg-indigo-50/50 border border-indigo-100/70 p-3 text-xs text-indigo-950">
                <span className="font-bold block text-indigo-900 mb-0.5">Special Meal & Stay Notes:</span>
                <p className="leading-relaxed">{booking.notes}</p>
              </div>
            )}
          </div>

          {/* Action Footer */}
          <div className="flex flex-wrap items-center justify-between gap-3 px-6 py-4 bg-slate-50/70 border-t border-slate-100">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-xs font-semibold text-slate-600 hover:bg-slate-200/60 rounded-xl transition"
            >
              Close
            </button>

            {canManage && (
              <div className="flex items-center gap-2">
                {/* Preview Email button */}
                <button
                  type="button"
                  onClick={() => setPreviewModalOpen(true)}
                  className="inline-flex items-center gap-1.5 px-3 py-2 rounded-xl border border-indigo-200 bg-indigo-50/70 text-[#101B82] text-xs font-bold hover:bg-indigo-100 transition active:scale-95 shadow-2xs"
                >
                  <Eye size={15} weight="bold" />
                  <span>Preview Email</span>
                </button>

                {/* 1. If DRAFT -> Can Send to Lodge */}
                {booking.status === "DRAFT" && (
                  <button
                    type="button"
                    onClick={handleSendToLodge}
                    disabled={actionLoading}
                    className="inline-flex items-center gap-1.5 px-4 py-2 rounded-xl bg-[#101B82] text-white text-xs font-bold hover:bg-[#0c145e] transition active:scale-95 shadow-xs disabled:opacity-50"
                  >
                    <PaperPlaneTilt size={15} weight="bold" />
                    <span>{actionLoading ? "Sending..." : "Send Request to Lodge"}</span>
                  </button>
                )}

                {/* 2. If PROVISIONAL -> Can Confirm with code or Decline */}
                {booking.status === "PROVISIONAL" && (
                  <>
                    <button
                      type="button"
                      onClick={handleDecline}
                      disabled={actionLoading}
                      className="inline-flex items-center gap-1 px-3 py-2 rounded-xl border border-rose-200 bg-rose-50 text-rose-700 text-xs font-bold hover:bg-rose-100 transition active:scale-95 disabled:opacity-50"
                    >
                      <XCircle size={15} weight="bold" />
                      <span>Decline</span>
                    </button>

                    <button
                      type="button"
                      onClick={() => setConfirmModalOpen(true)}
                      disabled={actionLoading}
                      className="inline-flex items-center gap-1.5 px-4 py-2 rounded-xl bg-emerald-600 text-white text-xs font-bold hover:bg-emerald-700 transition active:scale-95 shadow-xs disabled:opacity-50"
                    >
                      <CheckCircle size={15} weight="bold" />
                      <span>Confirm with Code</span>
                    </button>
                  </>
                )}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Confirmation Modal */}
      <ConfirmBookingModal
        open={confirmModalOpen}
        onClose={() => setConfirmModalOpen(false)}
        booking={booking}
        onSuccess={(updated) => {
          if (onUpdated) onUpdated(updated);
          onClose();
        }}
      />

      {/* Email Preview Modal */}
      <BookingEmailPreviewModal
        open={previewModalOpen}
        onClose={() => setPreviewModalOpen(false)}
        booking={booking}
        onSend={handleSendToLodge}
        sending={actionLoading}
      />
    </>
  );
};

export default BookingDetailModal;
