import { useState, useEffect } from "react";
import { 
  X, 
  PencilSimple, 
  Bank, 
  CreditCard, 
  Plus, 
  Trash, 
  ArrowsClockwise,
  Check
} from "@phosphor-icons/react";
import { apiRequest } from "../../api/api";
import { useAuth } from "../../hooks/useAuth";

const InvoiceManualEditModal = ({
  open,
  onClose,
  invoice,
  onSuccess,
}) => {
  const { token } = useAuth();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const [formData, setFormData] = useState({
    invoiceNumber: "",
    amount: "",
    currency: "USD",
    dueDate: "",
    status: "PENDING",
    notes: "",
    bankDetails: {
      bankName: "",
      accountName: "",
      accountNumber: "",
      currency: "USD",
      swiftCode: "",
      branch: "",
      iban: "",
      country: "Tanzania",
      intermediaryBankName: "",
      intermediarySwiftCode: "",
      lipaNamba: "",
      notes: "",
    },
    paymentPlan: [],
  });

  useEffect(() => {
    if (invoice) {
      setFormData({
        invoiceNumber: invoice.invoiceNumber || "",
        amount: invoice.amount != null ? String(invoice.amount) : "",
        currency: invoice.currency || "USD",
        dueDate: invoice.dueDate || "",
        status: invoice.status || "PENDING",
        notes: invoice.verificationNotes || "",
        bankDetails: {
          bankName: invoice.bankDetails?.bankName || "",
          accountName: invoice.bankDetails?.accountName || "",
          accountNumber: invoice.bankDetails?.accountNumber || "",
          currency: invoice.bankDetails?.currency || invoice.currency || "USD",
          swiftCode: invoice.bankDetails?.swiftCode || "",
          branch: invoice.bankDetails?.branch || "",
          iban: invoice.bankDetails?.iban || "",
          country: invoice.bankDetails?.country || "Tanzania",
          intermediaryBankName: invoice.bankDetails?.intermediaryBankName || "",
          intermediarySwiftCode: invoice.bankDetails?.intermediarySwiftCode || "",
          lipaNamba: invoice.bankDetails?.lipaNamba || "",
          notes: invoice.bankDetails?.notes || "",
        },
        paymentPlan: Array.isArray(invoice.paymentPlan) ? [...invoice.paymentPlan] : [],
      });
      setError(null);
    }
  }, [invoice, open]);

  if (!open || !invoice) return null;

  const handleBankChange = (field, value) => {
    setFormData((prev) => ({
      ...prev,
      bankDetails: {
        ...prev.bankDetails,
        [field]: value,
      },
    }));
  };

  const handleAddPlanRow = () => {
    setFormData((prev) => ({
      ...prev,
      paymentPlan: [
        ...prev.paymentPlan,
        {
          milestone: `Installment ${prev.paymentPlan.length + 1}`,
          percentage: null,
          amount: null,
          dueDate: "",
          status: "PENDING",
          notes: "",
        },
      ],
    }));
  };

  const handleRemovePlanRow = (index) => {
    setFormData((prev) => ({
      ...prev,
      paymentPlan: prev.paymentPlan.filter((_, i) => i !== index),
    }));
  };

  const handlePlanRowChange = (index, field, value) => {
    setFormData((prev) => {
      const updated = [...prev.paymentPlan];
      updated[index] = {
        ...updated[index],
        [field]: value,
      };
      return { ...prev, paymentPlan: updated };
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      const payload = {
        invoiceNumber: formData.invoiceNumber.trim(),
        amount: formData.amount ? parseFloat(formData.amount) : null,
        currency: formData.currency,
        dueDate: formData.dueDate || null,
        status: formData.status,
        notes: formData.notes,
        bankDetails: formData.bankDetails,
        paymentPlan: formData.paymentPlan.map((p) => ({
          milestone: p.milestone,
          percentage: p.percentage ? parseFloat(p.percentage) : null,
          amount: p.amount ? parseFloat(p.amount) : null,
          dueDate: p.dueDate || null,
          status: p.status || "PENDING",
          notes: p.notes,
        })),
      };

      const updated = await apiRequest(`/invoices/${invoice.id}/manual-entry`, {
        method: "PUT",
        body: JSON.stringify(payload),
      }, token);

      if (onSuccess) onSuccess(updated);
    } catch (err) {
      setError(err?.message || "Failed to update invoice.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-3 sm:p-4 bg-black/60 backdrop-blur-xs animate-in fade-in duration-150">
      <div className="w-full max-w-3xl max-h-[92vh] flex flex-col rounded-2xl bg-white shadow-2xl border border-slate-200 overflow-hidden font-sans animate-in zoom-in-95 duration-150">
        
        {/* Modal Header */}
        <div className="flex items-center justify-between border-b border-slate-200 px-6 py-4 bg-slate-50/80">
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-xl bg-indigo-50 border border-indigo-100 text-[#264624] flex items-center justify-center">
              <PencilSimple size={20} weight="bold" />
            </div>
            <div>
              <h3 className="font-bold text-slate-900 font-serif-title text-base sm:text-lg">
                Edit &amp; Complete Invoice Payment Coordinates
              </h3>
              <p className="text-xs text-slate-500">
                Manually record or correct lodge bank payout details and installment payment schedules
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

        {/* Modal Body */}
        <form onSubmit={handleSubmit} className="flex-1 overflow-y-auto p-6 space-y-6">
          {error && (
            <div className="p-3.5 rounded-xl bg-rose-50 border border-rose-200 text-rose-800 text-xs font-medium">
              {error}
            </div>
          )}

          {/* Section 1: Invoice Overview */}
          <div className="space-y-4">
            <h4 className="text-xs font-bold uppercase tracking-wider text-slate-800 border-b border-slate-100 pb-2">
              1. Invoice Overview &amp; Totals
            </h4>
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  Invoice Number *
                </label>
                <input
                  type="text"
                  required
                  value={formData.invoiceNumber}
                  onChange={(e) => setFormData({ ...formData, invoiceNumber: e.target.value })}
                  placeholder="e.g. INV-2024-001"
                  className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-[#264624]/20 focus:border-[#264624]"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  Total Bill Amount *
                </label>
                <div className="flex gap-2">
                  <select
                    value={formData.currency}
                    onChange={(e) => setFormData({ ...formData, currency: e.target.value })}
                    className="w-24 px-2.5 py-2 text-xs rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-[#264624]/20 focus:border-[#264624] bg-white"
                  >
                    <option value="USD">USD</option>
                    <option value="TZS">TZS</option>
                    <option value="KES">KES</option>
                    <option value="EUR">EUR</option>
                    <option value="GBP">GBP</option>
                  </select>
                  <input
                    type="number"
                    step="0.01"
                    required
                    value={formData.amount}
                    onChange={(e) => setFormData({ ...formData, amount: e.target.value })}
                    placeholder="0.00"
                    className="flex-1 px-3 py-2 text-xs rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-[#264624]/20 focus:border-[#264624]"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  Due Date
                </label>
                <input
                  type="date"
                  value={formData.dueDate}
                  onChange={(e) => setFormData({ ...formData, dueDate: e.target.value })}
                  className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-[#264624]/20 focus:border-[#264624]"
                />
              </div>
            </div>
          </div>

          {/* Section 2: Bank Payout Coordinates */}
          <div className="space-y-4">
            <h4 className="text-xs font-bold uppercase tracking-wider text-slate-800 border-b border-slate-100 pb-2 flex items-center gap-2">
              <Bank size={16} className="text-[#264624]" />
              <span>2. Lodge Bank Coordinates (Payout Details)</span>
            </h4>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  Bank Name *
                </label>
                <input
                  type="text"
                  value={formData.bankDetails.bankName}
                  onChange={(e) => handleBankChange("bankName", e.target.value)}
                  placeholder="e.g. Stanbic Bank Tanzania, CRDB Bank"
                  className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-[#264624]/20 focus:border-[#264624]"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  Beneficiary / Account Name *
                </label>
                <input
                  type="text"
                  value={formData.bankDetails.accountName}
                  onChange={(e) => handleBankChange("accountName", e.target.value)}
                  placeholder="e.g. Serengeti Serena Safari Lodge Ltd"
                  className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-[#264624]/20 focus:border-[#264624]"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  Account Number *
                </label>
                <input
                  type="text"
                  value={formData.bankDetails.accountNumber}
                  onChange={(e) => handleBankChange("accountNumber", e.target.value)}
                  placeholder="e.g. 9120000123456"
                  className="w-full px-3 py-2 text-xs font-mono font-bold rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-[#264624]/20 focus:border-[#264624]"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  SWIFT / BIC Code
                </label>
                <input
                  type="text"
                  value={formData.bankDetails.swiftCode}
                  onChange={(e) => handleBankChange("swiftCode", e.target.value)}
                  placeholder="e.g. SBICETZX"
                  className="w-full px-3 py-2 text-xs font-mono rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-[#264624]/20 focus:border-[#264624]"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  Branch
                </label>
                <input
                  type="text"
                  value={formData.bankDetails.branch}
                  onChange={(e) => handleBankChange("branch", e.target.value)}
                  placeholder="e.g. Arusha Branch"
                  className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-[#264624]/20 focus:border-[#264624]"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  IBAN (International)
                </label>
                <input
                  type="text"
                  value={formData.bankDetails.iban}
                  onChange={(e) => handleBankChange("iban", e.target.value)}
                  placeholder="Optional IBAN code"
                  className="w-full px-3 py-2 text-xs font-mono rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-[#264624]/20 focus:border-[#264624]"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  Intermediary Bank (If required)
                </label>
                <input
                  type="text"
                  value={formData.bankDetails.intermediaryBankName}
                  onChange={(e) => handleBankChange("intermediaryBankName", e.target.value)}
                  placeholder="e.g. Citibank New York"
                  className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-[#264624]/20 focus:border-[#264624]"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  Lipa Namba / M-Pesa Till
                </label>
                <input
                  type="text"
                  value={formData.bankDetails.lipaNamba}
                  onChange={(e) => handleBankChange("lipaNamba", e.target.value)}
                  placeholder="Optional local mobile money till"
                  className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-[#264624]/20 focus:border-[#264624]"
                />
              </div>
            </div>
          </div>

          {/* Section 3: Payment Plan & Installments */}
          <div className="space-y-4">
            <div className="flex items-center justify-between border-b border-slate-100 pb-2">
              <h4 className="text-xs font-bold uppercase tracking-wider text-slate-800 flex items-center gap-2">
                <CreditCard size={16} className="text-[#264624]" />
                <span>3. Payment Plan &amp; Installments</span>
              </h4>
              <button
                type="button"
                onClick={handleAddPlanRow}
                className="inline-flex items-center gap-1 px-2.5 py-1 rounded-lg bg-indigo-50 text-[#264624] text-xs font-bold hover:bg-indigo-100 transition shadow-2xs"
              >
                <Plus size={13} weight="bold" />
                <span>Add Milestone</span>
              </button>
            </div>

            {formData.paymentPlan.length > 0 ? (
              <div className="space-y-3">
                {formData.paymentPlan.map((row, idx) => (
                  <div key={idx} className="flex flex-wrap sm:flex-nowrap items-center gap-2.5 p-3 rounded-xl bg-slate-50 border border-slate-200/80">
                    <div className="flex-1 min-w-[140px]">
                      <input
                        type="text"
                        placeholder="Milestone (e.g. Deposit 30%)"
                        value={row.milestone}
                        onChange={(e) => handlePlanRowChange(idx, "milestone", e.target.value)}
                        className="w-full px-2.5 py-1.5 text-xs rounded-lg border border-slate-200 bg-white"
                      />
                    </div>
                    <div className="w-24">
                      <input
                        type="number"
                        placeholder="%"
                        value={row.percentage != null ? row.percentage : ""}
                        onChange={(e) => handlePlanRowChange(idx, "percentage", e.target.value)}
                        className="w-full px-2.5 py-1.5 text-xs rounded-lg border border-slate-200 bg-white"
                      />
                    </div>
                    <div className="w-32">
                      <input
                        type="number"
                        step="0.01"
                        placeholder="Amount"
                        value={row.amount != null ? row.amount : ""}
                        onChange={(e) => handlePlanRowChange(idx, "amount", e.target.value)}
                        className="w-full px-2.5 py-1.5 text-xs rounded-lg border border-slate-200 bg-white font-mono"
                      />
                    </div>
                    <div className="w-36">
                      <input
                        type="date"
                        value={row.dueDate || ""}
                        onChange={(e) => handlePlanRowChange(idx, "dueDate", e.target.value)}
                        className="w-full px-2.5 py-1.5 text-xs rounded-lg border border-slate-200 bg-white"
                      />
                    </div>
                    <button
                      type="button"
                      onClick={() => handleRemovePlanRow(idx)}
                      className="p-1.5 rounded-lg text-rose-500 hover:bg-rose-50 hover:text-rose-700 transition"
                      title="Remove milestone"
                    >
                      <Trash size={16} />
                    </button>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-xs text-slate-400 italic">
                No installment milestones. Click &quot;Add Milestone&quot; to specify deposit and balance schedule.
              </p>
            )}
          </div>

          {/* Modal Footer */}
          <div className="flex items-center justify-between pt-4 border-t border-slate-200">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-xs font-semibold text-slate-600 hover:bg-slate-100 rounded-xl transition"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl bg-[#264624] text-white text-xs font-bold hover:bg-[#1b331a] transition active:scale-95 shadow-xs disabled:opacity-50"
            >
              {loading ? (
                <ArrowsClockwise size={16} className="animate-spin" />
              ) : (
                <Check size={16} weight="bold" />
              )}
              <span>{loading ? "Saving Changes..." : "Save Invoice Details"}</span>
            </button>
          </div>
        </form>

      </div>
    </div>
  );
};

export default InvoiceManualEditModal;
