import { useState, useEffect } from "react";
import { 
  X, 
  EnvelopeSimple, 
  PaperPlaneTilt, 
  ArrowsClockwise, 
  Code, 
  Desktop,
  WarningCircle
} from "@phosphor-icons/react";
import { apiRequest } from "../../api/api";
import { useAuth } from "../../hooks/useAuth";

/**
 * BookingEmailPreviewModal
 * Displays live, high-fidelity rendering of the accommodation dispatch email
 * before it is transmitted to the partner property / lodge.
 */
const BookingEmailPreviewModal = ({
  open,
  onClose,
  booking,
  onSend,
  sending = false,
}) => {
  const { token } = useAuth();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [previewData, setPreviewData] = useState(null);
  const [activeTab, setActiveTab] = useState("html"); // 'html' or 'plain'

  useEffect(() => {
    if (!open || !booking?.id) {
      setPreviewData(null);
      setError(null);
      return;
    }

    let isMounted = true;
    const fetchPreview = async () => {
      setLoading(true);
      setError(null);
      try {
        const data = await apiRequest(`/accommodation-bookings/${booking.id}/preview`, {
          method: "GET",
        }, token);
        if (isMounted) {
          setPreviewData(data);
        }
      } catch (err) {
        if (isMounted) {
          setError(err?.message || "Failed to load email preview from server.");
        }
      } finally {
        if (isMounted) {
          setLoading(false);
        }
      }
    };

    fetchPreview();

    return () => {
      isMounted = false;
    };
  }, [open, booking?.id, token]);

  if (!open) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-3 sm:p-4 bg-black/60 backdrop-blur-xs animate-in fade-in duration-150">
      <div className="w-full max-w-4xl max-h-[92vh] flex flex-col rounded-2xl bg-white shadow-2xl border border-slate-200 overflow-hidden font-sans animate-in zoom-in-95 duration-150">
        
        {/* Modal Header */}
        <div className="flex items-center justify-between border-b border-slate-200 px-6 py-4 bg-slate-50/80">
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-xl bg-emerald-50 border border-emerald-200 text-[#264624] flex items-center justify-center">
              <EnvelopeSimple size={20} weight="duotone" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h3 className="font-bold text-slate-900 font-serif-title text-base sm:text-lg">
                  Lodge Reservation Email Preview
                </h3>
                <span className="font-mono text-xs font-semibold px-2 py-0.5 rounded-full bg-slate-200/70 text-slate-700">
                  {booking?.referenceNumber}
                </span>
              </div>
              <p className="text-xs text-slate-500">
                Visual inspection of the executive dispatch template sent to the lodge reservations desk
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2">
            {/* View Mode Tabs */}
            <div className="hidden sm:flex items-center p-1 bg-slate-200/70 rounded-xl text-xs font-semibold">
              <button
                type="button"
                onClick={() => setActiveTab("html")}
                className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg transition ${
                  activeTab === "html"
                    ? "bg-white text-[#264624] shadow-xs font-bold"
                    : "text-slate-600 hover:text-slate-900"
                }`}
              >
                <Desktop size={14} weight="bold" />
                <span>Executive HTML</span>
              </button>
              <button
                type="button"
                onClick={() => setActiveTab("plain")}
                className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg transition ${
                  activeTab === "plain"
                    ? "bg-white text-[#264624] shadow-xs font-bold"
                    : "text-slate-600 hover:text-slate-900"
                }`}
              >
                <Code size={14} weight="bold" />
                <span>Plain Text</span>
              </button>
            </div>

            <button
              type="button"
              onClick={onClose}
              className="rounded-lg p-2 text-slate-400 hover:text-slate-600 hover:bg-slate-200/60 transition"
            >
              <X size={18} weight="bold" />
            </button>
          </div>
        </div>

        {/* Envelope Metadata Bar */}
        {previewData && (
          <div className="border-b border-slate-200 bg-slate-100/60 px-6 py-2.5 text-xs text-slate-600 space-y-1">
            <div className="flex flex-wrap items-center gap-x-6 gap-y-1">
              <div>
                <span className="text-slate-400 font-semibold uppercase text-[10px] tracking-wider mr-1.5">To:</span>
                <span className="font-semibold text-slate-800">{previewData.recipient}</span>
              </div>
              <div>
                <span className="text-slate-400 font-semibold uppercase text-[10px] tracking-wider mr-1.5">From:</span>
                <span className="text-slate-700">{previewData.from}</span>
              </div>
            </div>
            <div>
              <span className="text-slate-400 font-semibold uppercase text-[10px] tracking-wider mr-1.5">Subject:</span>
              <span className="font-bold text-slate-900">{previewData.subject}</span>
            </div>
          </div>
        )}

        {/* Mobile Tab Toggle */}
        <div className="sm:hidden flex items-center justify-center p-2 bg-slate-100 border-b border-slate-200">
          <div className="flex items-center p-1 bg-slate-200/70 rounded-xl text-xs font-semibold w-full">
            <button
              type="button"
              onClick={() => setActiveTab("html")}
              className={`flex-1 py-1.5 rounded-lg text-center transition ${
                activeTab === "html"
                  ? "bg-white text-[#264624] shadow-xs font-bold"
                  : "text-slate-600"
              }`}
            >
              Executive HTML
            </button>
            <button
              type="button"
              onClick={() => setActiveTab("plain")}
              className={`flex-1 py-1.5 rounded-lg text-center transition ${
                activeTab === "plain"
                  ? "bg-white text-[#264624] shadow-xs font-bold"
                  : "text-slate-600"
              }`}
            >
              Plain Text
            </button>
          </div>
        </div>

        {/* Modal Body / Preview Pane */}
        <div className="flex-1 overflow-y-auto bg-slate-200/40 p-4 sm:p-6 min-h-[380px] flex flex-col justify-center items-center">
          {loading && (
            <div className="flex flex-col items-center justify-center py-12 text-slate-500 gap-3">
              <ArrowsClockwise size={32} className="animate-spin text-[#264624]" />
              <p className="text-xs font-medium">Generating email layout preview...</p>
            </div>
          )}

          {error && !loading && (
            <div className="max-w-md w-full p-4 rounded-2xl bg-rose-50 border border-rose-200 text-rose-800 text-xs flex items-start gap-3">
              <WarningCircle size={20} className="text-rose-600 shrink-0 mt-0.5" weight="fill" />
              <div>
                <p className="font-bold">Failed to load preview</p>
                <p className="mt-1 text-rose-700">{error}</p>
              </div>
            </div>
          )}

          {!loading && !error && previewData && (
            <>
              {activeTab === "html" ? (
                <div className="w-full h-[540px] rounded-xl overflow-hidden shadow-md border border-slate-300 bg-white">
                  <iframe
                    title="Accommodation Email Preview"
                    srcDoc={previewData.html}
                    className="w-full h-full border-0"
                    sandbox="allow-same-origin"
                  />
                </div>
              ) : (
                <div className="w-full h-[540px] rounded-xl overflow-auto shadow-md border border-slate-800 bg-slate-900 p-5 text-slate-100 font-mono text-xs leading-relaxed whitespace-pre-wrap select-text">
                  {previewData.plainText}
                </div>
              )}
            </>
          )}
        </div>

        {/* Modal Footer */}
        <div className="flex items-center justify-between px-6 py-4 border-t border-slate-200 bg-slate-50/90">
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-2 text-xs font-semibold text-slate-600 hover:bg-slate-200/60 rounded-xl transition"
          >
            Close Preview
          </button>

          {booking?.status === "DRAFT" && onSend && (
            <button
              type="button"
              onClick={() => {
                onSend();
                onClose();
              }}
              disabled={sending || loading}
              className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl bg-[#264624] text-white text-xs font-bold hover:bg-[#1e381c] transition active:scale-95 shadow-xs disabled:opacity-50"
            >
              {sending ? (
                <ArrowsClockwise size={16} className="animate-spin" />
              ) : (
                <PaperPlaneTilt size={16} weight="bold" />
              )}
              <span>{sending ? "Dispatching..." : "Send Request to Lodge"}</span>
            </button>
          )}
        </div>

      </div>
    </div>
  );
};

export default BookingEmailPreviewModal;
