import { useState, useEffect } from "react";
import {
  Receipt,
  Bank,
  CheckCircle,
  WarningCircle,
  ShieldCheck,
  PencilSimple,
  UploadSimple,
  DownloadSimple,
  Copy,
  Check,
  ArrowsClockwise,
  CreditCard,
  CalendarBlank,
  FilePdf,
  FileXls,
  Sparkle
} from "@phosphor-icons/react";
import { apiRequest } from "../../api/api";
import { useAuth } from "../../hooks/useAuth";
import InvoiceManualEditModal from "../bookings/InvoiceManualEditModal";
import InvoiceVerifyModal from "../bookings/InvoiceVerifyModal";

/**
 * AccommodationInvoiceSection
 * Displays vendor bill (Accounts Payable) received from partner lodge/property.
 * Displays extracted banking coordinates, installment payment plans,
 * human-in-the-loop verification, and manual fallback editing.
 */
const AccommodationInvoiceSection = ({
  booking,
  canManage = false,
}) => {
  const { token } = useAuth();
  const [invoice, setInvoice] = useState(null);
  const [loading, setLoading] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState(null);
  const [copiedField, setCopiedField] = useState(null);

  const [editModalOpen, setEditModalOpen] = useState(false);
  const [verifyModalOpen, setVerifyModalOpen] = useState(false);

  const loadInvoice = async () => {
    if (!booking?.id) return;
    setLoading(true);
    setError(null);
    try {
      const data = await apiRequest(`/invoices/booking/${booking.id}`, {
        method: "GET",
      }, token);
      setInvoice(data);
    } catch (err) {
      // 404 means no invoice attached yet
      if (err?.message?.includes("not found") || err?.message?.includes("No invoice")) {
        setInvoice(null);
      } else {
        setError(err?.message || "Failed to load invoice details.");
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadInvoice();
  }, [booking?.id, token]);

  const handleFileUpload = async (event) => {
    const file = event.target.files?.[0];
    if (!file || !booking?.id) return;

    setUploading(true);
    setError(null);

    const formData = new FormData();
    formData.append("file", file);

    try {
      const response = await fetch(`/api/v1/invoices/booking/${booking.id}/upload`, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`,
        },
        body: formData,
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || "Failed to upload and process invoice document.");
      }

      const updated = await response.json();
      setInvoice(updated);
    } catch (err) {
      setError(err?.message || "Invoice upload failed.");
    } finally {
      setUploading(false);
      event.target.value = "";
    }
  };

  const handleDownloadFile = async () => {
    if (!invoice?.id) return;
    try {
      const response = await fetch(`/api/v1/invoices/${invoice.id}/download`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      if (!response.ok) throw new Error("Could not download invoice file");
      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = invoice.fileName || "invoice.pdf";
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      alert("Failed to download file: " + err.message);
    }
  };

  const handleCopy = (text, fieldKey) => {
    if (!text) return;
    navigator.clipboard.writeText(text);
    setCopiedField(fieldKey);
    setTimeout(() => setCopiedField(null), 2000);
  };

  const bank = invoice?.bankDetails;
  const paymentPlan = invoice?.paymentPlan || [];
  const isVerified = Boolean(invoice?.verified);
  const needsReview = Boolean(invoice?.needsManualReview);

  return (
    <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-xs font-sans space-y-6">
      {/* Header Bar */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-slate-100 pb-5">
        <div className="flex items-start gap-3.5">
          <div className="w-11 h-11 rounded-2xl bg-gradient-to-br from-[#264624]/10 via-indigo-50 to-blue-50/50 border border-indigo-100 text-[#264624] flex items-center justify-center shrink-0">
            <Receipt size={24} weight="duotone" />
          </div>
          <div>
            <div className="flex flex-wrap items-center gap-2.5">
              <h3 className="text-base font-bold text-slate-900 font-serif-title">
                Lodge Vendor Bill & Payment Instructions
              </h3>
              {invoice && (
                <>
                  <span className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[11px] font-bold uppercase tracking-wider border ${
                    isVerified
                      ? "bg-emerald-50 text-emerald-700 border-emerald-200"
                      : "bg-amber-50 text-amber-700 border-amber-200"
                  }`}>
                    {isVerified ? (
                      <>
                        <ShieldCheck size={14} weight="fill" className="text-emerald-600" />
                        <span>Verified Coordinates</span>
                      </>
                    ) : (
                      <>
                        <WarningCircle size={14} weight="fill" className="text-amber-600" />
                        <span>Unverified Bill</span>
                      </>
                    )}
                  </span>

                  {invoice.aiExtracted && (
                    <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-bold bg-purple-50 text-purple-700 border border-purple-200">
                      <Sparkle size={12} weight="fill" />
                      <span>Gemini AI Detected</span>
                    </span>
                  )}
                </>
              )}
            </div>
            <p className="mt-1 text-xs text-slate-500">
              Official supplier invoice issued by property for this safari stay, including bank payout details and payment milestones.
            </p>
          </div>
        </div>

        {/* Top Actions */}
        <div className="flex flex-wrap items-center gap-2 self-end sm:self-auto">
          {loading && (
            <ArrowsClockwise size={16} className="animate-spin text-slate-400" />
          )}

          {invoice && canManage && (
            <>
              <button
                type="button"
                onClick={() => setEditModalOpen(true)}
                className="inline-flex items-center gap-1.5 px-3 py-2 rounded-xl border border-slate-200 bg-slate-50 hover:bg-slate-100 text-slate-700 text-xs font-semibold transition active:scale-95 shadow-2xs"
              >
                <PencilSimple size={14} weight="bold" />
                <span>Edit / Fill Manually</span>
              </button>

              {!isVerified && (
                <button
                  type="button"
                  onClick={() => setVerifyModalOpen(true)}
                  className="inline-flex items-center gap-1.5 px-3.5 py-2 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-bold transition active:scale-95 shadow-2xs"
                >
                  <ShieldCheck size={15} weight="bold" />
                  <span>Verify Payment Info</span>
                </button>
              )}
            </>
          )}

          {/* Upload Button */}
          {canManage && (
            <label className={`inline-flex items-center gap-1.5 px-3.5 py-2 rounded-xl border border-dashed border-[#264624]/40 bg-indigo-50/50 hover:bg-indigo-100/60 text-[#264624] text-xs font-bold transition active:scale-95 shadow-2xs cursor-pointer ${
              uploading ? "opacity-50 pointer-events-none" : ""
            }`}>
              <UploadSimple size={15} weight="bold" />
              <span>{uploading ? "Analyzing Document..." : invoice ? "Replace Document" : "Upload Lodge Invoice (PDF/Excel)"}</span>
              <input
                type="file"
                accept=".pdf,.xlsx,.xls"
                className="hidden"
                onChange={handleFileUpload}
                disabled={uploading}
              />
            </label>
          )}
        </div>
      </div>

      {error && (
        <div className="p-3.5 rounded-xl bg-rose-50 border border-rose-200 text-rose-800 text-xs font-medium flex items-center gap-2">
          <WarningCircle size={18} weight="fill" className="text-rose-600 shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {/* Manual Review Alert Banner (When AI fails or key is missing) */}
      {needsReview && invoice && (
        <div className="p-4 rounded-xl bg-amber-50 border border-amber-200 text-amber-900 text-xs flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div className="flex items-start gap-2.5">
            <WarningCircle size={20} weight="fill" className="text-amber-600 shrink-0 mt-0.5" />
            <div>
              <p className="font-bold text-amber-950">Action Needed: Manual Review Required</p>
              <p className="mt-0.5 text-amber-800">
                {invoice.extractionError || "Automatic banking coordinate extraction could not complete. Your file has been preserved safely."}
                {" "}Please review the attached document and click &quot;Edit / Fill Manually&quot; to input the banking coordinates.
              </p>
            </div>
          </div>
          {canManage && (
            <button
              type="button"
              onClick={() => setEditModalOpen(true)}
              className="px-3.5 py-1.5 rounded-lg bg-amber-600 text-white text-xs font-bold hover:bg-amber-700 transition shrink-0 active:scale-95"
            >
              Fill Coordinates Manually
            </button>
          )}
        </div>
      )}

      {/* Verification Details Banner */}
      {isVerified && invoice && (
        <div className="p-3.5 rounded-xl bg-emerald-50/70 border border-emerald-200/80 text-emerald-900 text-xs flex items-center justify-between gap-3">
          <div className="flex items-center gap-2">
            <ShieldCheck size={18} weight="fill" className="text-emerald-600" />
            <span>
              <strong>Payment Coordinates Approved</strong> by {invoice.verifiedByName || "Management"}
              {invoice.verifiedAt ? ` on ${new Date(invoice.verifiedAt).toLocaleDateString()}` : ""}.
              {invoice.verificationNotes ? ` (${invoice.verificationNotes})` : ""}
            </span>
          </div>
        </div>
      )}

      {!invoice && !loading && (
        <div className="p-8 rounded-2xl border-2 border-dashed border-slate-200 text-center bg-slate-50/50">
          <div className="w-12 h-12 rounded-2xl bg-indigo-50 text-[#264624] flex items-center justify-center mx-auto mb-3">
            <CreditCard size={24} weight="duotone" />
          </div>
          <h4 className="text-sm font-bold text-slate-800">No Lodge Invoice Recorded Yet</h4>
          <p className="text-xs text-slate-500 max-w-md mx-auto mt-1 mb-4 leading-relaxed">
            When the lodge emails back with an invoice attachment (.pdf or .xlsx), the system will automatically extract bank coordinates and payment milestones. You can also upload their invoice document directly here.
          </p>
          {canManage && (
            <label className="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl bg-[#264624] text-white text-xs font-bold hover:bg-[#1b331a] transition active:scale-95 cursor-pointer shadow-xs">
              <UploadSimple size={16} weight="bold" />
              <span>Upload Lodge Bill (PDF / Excel)</span>
              <input
                type="file"
                accept=".pdf,.xlsx,.xls"
                className="hidden"
                onChange={handleFileUpload}
                disabled={uploading}
              />
            </label>
          )}
        </div>
      )}

      {invoice && (
        <>
          {/* Summary Row */}
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-3.5">
            <div className="p-3.5 rounded-xl bg-slate-50 border border-slate-100">
              <span className="block text-[10px] font-bold uppercase tracking-wider text-slate-400">Invoice Number</span>
              <span className="mt-1 block font-bold text-slate-900 text-sm font-mono truncate">
                {invoice.invoiceNumber || "N/A"}
              </span>
            </div>

            <div className="p-3.5 rounded-xl bg-slate-50 border border-slate-100">
              <span className="block text-[10px] font-bold uppercase tracking-wider text-slate-400">Total Bill Amount</span>
              <span className="mt-1 block font-bold text-[#264624] text-sm">
                {invoice.currency || "USD"} {invoice.amount != null ? Number(invoice.amount).toLocaleString(undefined, { minimumFractionDigits: 2 }) : "0.00"}
              </span>
            </div>

            <div className="p-3.5 rounded-xl bg-slate-50 border border-slate-100">
              <span className="block text-[10px] font-bold uppercase tracking-wider text-slate-400">Final Due Date</span>
              <span className="mt-1 block font-bold text-slate-900 text-sm flex items-center gap-1.5">
                <CalendarBlank size={14} className="text-slate-400" />
                {invoice.dueDate || "Upon Receipt"}
              </span>
            </div>

            <div className="p-3.5 rounded-xl bg-slate-50 border border-slate-100 flex items-center justify-between">
              <div>
                <span className="block text-[10px] font-bold uppercase tracking-wider text-slate-400">Attached File</span>
                <span className="mt-1 block font-semibold text-slate-800 text-xs truncate max-w-[120px]" title={invoice.fileName}>
                  {invoice.fileName || "invoice.pdf"}
                </span>
              </div>
              {invoice.fileName && (
                <button
                  type="button"
                  onClick={handleDownloadFile}
                  title="Download invoice file"
                  className="p-1.5 rounded-lg bg-white border border-slate-200 text-slate-600 hover:text-[#264624] hover:bg-slate-50 transition shadow-2xs"
                >
                  <DownloadSimple size={15} weight="bold" />
                </button>
              )}
            </div>
          </div>

          {/* Grid: Bank Details & Payment Plan */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-5 pt-2">
            
            {/* 1. Banking Coordinates Card */}
            <div className="rounded-xl border border-slate-200/90 bg-white p-5 space-y-4">
              <div className="flex items-center justify-between border-b border-slate-100 pb-3">
                <div className="flex items-center gap-2">
                  <Bank size={18} weight="duotone" className="text-[#264624]" />
                  <h4 className="text-xs font-bold uppercase tracking-wider text-slate-800">
                    Lodge Bank Payout Coordinates
                  </h4>
                </div>
                {bank?.currency && (
                  <span className="text-[10px] font-bold px-2 py-0.5 rounded-md bg-indigo-50 text-[#264624] border border-indigo-100">
                    {bank.currency} Account
                  </span>
                )}
              </div>

              {bank && (bank.accountNumber || bank.bankName) ? (
                <div className="space-y-2.5 text-xs">
                  <div className="flex items-center justify-between py-1 border-b border-slate-50">
                    <span className="text-slate-400 font-medium">Bank Name:</span>
                    <span className="font-bold text-slate-900">{bank.bankName || "-"}</span>
                  </div>

                  <div className="flex items-center justify-between py-1 border-b border-slate-50">
                    <span className="text-slate-400 font-medium">Beneficiary / Account Name:</span>
                    <span className="font-bold text-slate-900 text-right">{bank.accountName || "-"}</span>
                  </div>

                  <div className="flex items-center justify-between py-1.5 px-2.5 rounded-lg bg-slate-50 border border-slate-100">
                    <div>
                      <span className="block text-[10px] font-bold text-slate-400 uppercase">Account Number</span>
                      <span className="font-mono font-bold text-slate-900 text-sm tracking-wide">
                        {bank.accountNumber || "-"}
                      </span>
                    </div>
                    {bank.accountNumber && (
                      <button
                        type="button"
                        onClick={() => handleCopy(bank.accountNumber, "accNo")}
                        className="inline-flex items-center gap-1 px-2 py-1 rounded-md bg-white border border-slate-200 text-[11px] font-semibold text-slate-600 hover:text-[#264624] shadow-2xs"
                      >
                        {copiedField === "accNo" ? (
                          <>
                            <Check size={12} weight="bold" className="text-emerald-600" />
                            <span className="text-emerald-600">Copied</span>
                          </>
                        ) : (
                          <>
                            <Copy size={12} weight="bold" />
                            <span>Copy</span>
                          </>
                        )}
                      </button>
                    )}
                  </div>

                  {bank.swiftCode && (
                    <div className="flex items-center justify-between py-1 border-b border-slate-50">
                      <span className="text-slate-400 font-medium">SWIFT / BIC:</span>
                      <span className="font-mono font-bold text-slate-800">{bank.swiftCode}</span>
                    </div>
                  )}

                  {bank.branch && (
                    <div className="flex items-center justify-between py-1 border-b border-slate-50">
                      <span className="text-slate-400 font-medium">Branch / Location:</span>
                      <span className="text-slate-700 font-medium">{bank.branch}</span>
                    </div>
                  )}

                  {bank.iban && (
                    <div className="flex items-center justify-between py-1 border-b border-slate-50">
                      <span className="text-slate-400 font-medium">IBAN:</span>
                      <span className="font-mono text-slate-800 font-semibold">{bank.iban}</span>
                    </div>
                  )}

                  {bank.intermediaryBankName && (
                    <div className="p-2.5 rounded-lg bg-indigo-50/40 border border-indigo-100 text-[11px] text-indigo-950">
                      <span className="font-bold block">Intermediary Correspondent Bank:</span>
                      <span>{bank.intermediaryBankName} (SWIFT: {bank.intermediarySwiftCode || "N/A"})</span>
                    </div>
                  )}

                  {bank.lipaNamba && (
                    <div className="flex items-center justify-between py-1">
                      <span className="text-slate-400 font-medium">Lipa Namba / Till:</span>
                      <span className="font-mono font-bold text-emerald-700">{bank.lipaNamba}</span>
                    </div>
                  )}

                  {bank.notes && (
                    <div className="text-[11px] text-slate-500 italic pt-1 border-t border-slate-100">
                      Note: {bank.notes}
                    </div>
                  )}
                </div>
              ) : (
                <div className="text-center py-6 text-slate-400 text-xs">
                  <p>No banking coordinates available yet.</p>
                  {canManage && (
                    <button
                      type="button"
                      onClick={() => setEditModalOpen(true)}
                      className="mt-2 text-[#264624] font-bold hover:underline"
                    >
                      Click here to enter bank coordinates manually
                    </button>
                  )}
                </div>
              )}
            </div>

            {/* 2. Payment Plan & Installment Schedule */}
            <div className="rounded-xl border border-slate-200/90 bg-white p-5 space-y-4">
              <div className="flex items-center justify-between border-b border-slate-100 pb-3">
                <div className="flex items-center gap-2">
                  <CreditCard size={18} weight="duotone" className="text-[#264624]" />
                  <h4 className="text-xs font-bold uppercase tracking-wider text-slate-800">
                    Payment Plan &amp; Installments
                  </h4>
                </div>
                <span className="text-[10px] font-bold text-slate-400">
                  {paymentPlan.length} {paymentPlan.length === 1 ? "Milestone" : "Milestones"}
                </span>
              </div>

              {paymentPlan.length > 0 ? (
                <div className="space-y-2">
                  <table className="w-full text-xs">
                    <thead>
                      <tr className="border-b border-slate-100 text-[10px] text-slate-400 uppercase font-bold text-left">
                        <th className="pb-2">Milestone</th>
                        <th className="pb-2 text-right">Amount</th>
                        <th className="pb-2 text-right">Due Date</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-50">
                      {paymentPlan.map((item, idx) => (
                        <tr key={idx} className="hover:bg-slate-50/50">
                          <td className="py-2.5 font-medium text-slate-800">
                            {item.milestone}
                            {item.percentage ? ` (${item.percentage}%)` : ""}
                          </td>
                          <td className="py-2.5 text-right font-bold text-slate-900 font-mono">
                            {invoice.currency || "USD"} {item.amount != null ? Number(item.amount).toLocaleString(undefined, { minimumFractionDigits: 2 }) : "-"}
                          </td>
                          <td className="py-2.5 text-right text-slate-600">
                            {item.dueDate || "N/A"}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              ) : (
                <div className="text-center py-6 text-slate-400 text-xs">
                  <p>No installment plan specified. Full payment required on due date.</p>
                </div>
              )}

              {/* Extraction Confidence / Notes */}
              {invoice.extractionNotes && (
                <div className="p-3 rounded-lg bg-slate-50 border border-slate-100 text-[11px] text-slate-600 leading-relaxed">
                  <span className="font-bold text-slate-700 block mb-0.5">Extraction Remarks:</span>
                  {invoice.extractionNotes}
                </div>
              )}
            </div>

          </div>
        </>
      )}

      {/* Edit Modal */}
      {invoice && (
        <InvoiceManualEditModal
          open={editModalOpen}
          onClose={() => setEditModalOpen(false)}
          invoice={invoice}
          onSuccess={(updated) => {
            setInvoice(updated);
            setEditModalOpen(false);
          }}
        />
      )}

      {/* Verify Modal */}
      {invoice && (
        <InvoiceVerifyModal
          open={verifyModalOpen}
          onClose={() => setVerifyModalOpen(false)}
          invoice={invoice}
          onSuccess={(updated) => {
            setInvoice(updated);
            setVerifyModalOpen(false);
          }}
        />
      )}
    </div>
  );
};

export default AccommodationInvoiceSection;
