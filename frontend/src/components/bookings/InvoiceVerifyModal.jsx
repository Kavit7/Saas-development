import { useState } from "react";
import { 
  X, 
  ShieldCheck, 
  ArrowsClockwise, 
  Bank,
  CheckCircle,
  WarningCircle
} from "@phosphor-icons/react";
import { apiRequest } from "../../api/api";
import { useAuth } from "../../hooks/useAuth";

const InvoiceVerifyModal = ({
  open,
  onClose,
  invoice,
  onSuccess,
}) => {
  const { token } = useAuth();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [notes, setNotes] = useState("");

  if (!open || !invoice) return null;

  const bank = invoice.bankDetails;

  const handleVerify = async () => {
    setLoading(true);
    setError(null);
    try {
      const updated = await apiRequest(`/invoices/${invoice.id}/verify`, {
        method: "POST",
        body: JSON.stringify({
          verified: true,
          verificationNotes: notes.trim() || "Confirmed against property bank contract.",
        }),
      }, token);

      if (onSuccess) onSuccess(updated);
    } catch (err) {
      setError(err?.message || "Failed to verify invoice coordinates.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-3 sm:p-4 bg-black/60 backdrop-blur-xs animate-in fade-in duration-150">
      <div className="w-full max-w-lg rounded-2xl bg-white shadow-2xl border border-slate-200 overflow-hidden font-sans animate-in zoom-in-95 duration-150">
        
        {/* Header */}
        <div className="flex items-center justify-between border-b border-slate-200 px-6 py-4 bg-emerald-50/60">
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-xl bg-emerald-100 text-emerald-800 flex items-center justify-center font-bold">
              <ShieldCheck size={20} weight="fill" />
            </div>
            <div>
              <h3 className="font-bold text-slate-900 font-serif-title text-base">
                Verify Payment Coordinates
              </h3>
              <p className="text-xs text-slate-500">
                Reservation Manager verification before Salesperson payment release
              </p>
            </div>
          </div>

          <button
            type="button"
            onClick={onClose}
            className="rounded-lg p-2 text-slate-400 hover:text-slate-600 hover:bg-slate-200/60 transition"
          >
            <X size={18} weight="bold" />
          </button>
        </div>

        {/* Body */}
        <div className="p-6 space-y-4">
          {error && (
            <div className="p-3 rounded-xl bg-rose-50 border border-rose-200 text-rose-800 text-xs font-medium">
              {error}
            </div>
          )}

          <p className="text-xs text-slate-600 leading-relaxed">
            Please confirm that the bank coordinates and bill total below match the official contract agreement with <strong>{invoice.propertyName || "the property"}</strong>:
          </p>

          {/* Review Card */}
          <div className="p-4 rounded-xl bg-slate-50 border border-slate-200 text-xs space-y-2">
            <div className="flex justify-between border-b border-slate-100 pb-1.5">
              <span className="text-slate-500">Invoice Reference:</span>
              <span className="font-bold font-mono text-slate-900">{invoice.invoiceNumber}</span>
            </div>
            <div className="flex justify-between border-b border-slate-100 pb-1.5">
              <span className="text-slate-500">Total Amount:</span>
              <span className="font-bold text-[#264624]">
                {invoice.currency || "USD"} {invoice.amount != null ? Number(invoice.amount).toLocaleString(undefined, { minimumFractionDigits: 2 }) : "-"}
              </span>
            </div>
            <div className="flex justify-between border-b border-slate-100 pb-1.5">
              <span className="text-slate-500">Beneficiary Name:</span>
              <span className="font-bold text-slate-900">{bank?.accountName || "N/A"}</span>
            </div>
            <div className="flex justify-between border-b border-slate-100 pb-1.5">
              <span className="text-slate-500">Bank &amp; Account Number:</span>
              <span className="font-mono font-bold text-slate-900">
                {bank?.bankName ? `${bank.bankName} - ` : ""}{bank?.accountNumber || "N/A"}
              </span>
            </div>
            {bank?.swiftCode && (
              <div className="flex justify-between">
                <span className="text-slate-500">SWIFT / BIC:</span>
                <span className="font-mono font-semibold text-slate-800">{bank.swiftCode}</span>
              </div>
            )}
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Verification Sign-off Notes (Optional)
            </label>
            <textarea
              rows={2}
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              placeholder="e.g. Account number confirmed via lodge reservations direct phone line."
              className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-600"
            />
          </div>
        </div>

        {/* Footer */}
        <div className="flex items-center justify-between px-6 py-4 bg-slate-50/80 border-t border-slate-200">
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-2 text-xs font-semibold text-slate-600 hover:bg-slate-200/60 rounded-xl transition"
          >
            Cancel
          </button>

          <button
            type="button"
            onClick={handleVerify}
            disabled={loading}
            className="inline-flex items-center gap-1.5 px-4 py-2 rounded-xl bg-emerald-600 text-white text-xs font-bold hover:bg-emerald-700 transition active:scale-95 shadow-xs disabled:opacity-50"
          >
            {loading ? (
              <ArrowsClockwise size={16} className="animate-spin" />
            ) : (
              <ShieldCheck size={16} weight="bold" />
            )}
            <span>{loading ? "Approving..." : "Approve & Verify Coordinates"}</span>
          </button>
        </div>

      </div>
    </div>
  );
};

export default InvoiceVerifyModal;
