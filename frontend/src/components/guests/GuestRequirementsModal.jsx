import { useState, useEffect, useCallback } from "react";
import {
  X,
  Plus,
  Trash,
  PencilSimple,
  ForkKnife,
  FirstAid,
  Wheelchair,
  Bed,
  Sparkle,
  WarningCircle,
  ShieldWarning,
  CheckCircle,
  ArrowsClockwise,
  User,
  NotePencil,
} from "@phosphor-icons/react";
import { getAllData, createData, updateData, deleteData } from "../../api/api";
import { useAuth } from "../../hooks/useAuth";

const REQUIREMENT_TYPES = [
  { value: "DIETARY", label: "Dietary Restriction", icon: ForkKnife },
  { value: "MEDICAL", label: "Medical / Allergy Condition", icon: FirstAid },
  { value: "MOBILITY", label: "Mobility & Physical Access", icon: Wheelchair },
  { value: "ROOM", label: "Room & Bedding Preference", icon: Bed },
  { value: "SPECIAL_REQUEST", label: "Special Occasion / Request", icon: Sparkle },
  { value: "OTHER", label: "Other Requirement", icon: NotePencil },
];

const SEVERITY_LEVELS = [
  { value: "LOW", label: "Low", color: "bg-slate-50 text-slate-700 border-slate-200" },
  { value: "MEDIUM", label: "Medium", color: "bg-blue-50 text-blue-700 border-blue-200" },
  { value: "HIGH", label: "High Priority", color: "bg-amber-50 text-amber-700 border-amber-200" },
  { value: "CRITICAL", label: "Critical / Life-Safety", color: "bg-rose-50 text-rose-700 border-rose-200" },
];

const getTypeConfig = (type) => {
  return REQUIREMENT_TYPES.find((t) => t.value === type) || {
    value: type,
    label: type,
    icon: NotePencil,
  };
};

const getSeverityConfig = (severity) => {
  return SEVERITY_LEVELS.find((s) => s.value === severity) || {
    value: severity,
    label: severity || "MEDIUM",
    color: "bg-blue-50 text-blue-700 border-blue-200",
  };
};

const GuestRequirementsModal = ({
  open,
  onClose,
  guest,
  canManage = true,
  onUpdated,
}) => {
  const { token } = useAuth();
  const [requirements, setRequirements] = useState([]);
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [showAddForm, setShowAddForm] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [feedback, setFeedback] = useState(null);

  // Form State
  const [formData, setFormData] = useState({
    requirementType: "DIETARY",
    requirementValue: "",
    severity: "MEDIUM",
    notes: "",
  });

  const loadRequirements = useCallback(async () => {
    if (!guest?.id || !token) return;
    setLoading(true);
    setFeedback(null);
    try {
      const res = await getAllData(`/guest/guest-requirements/${guest.id}`, token);
      const list = Array.isArray(res?.data) ? res.data : Array.isArray(res) ? res : [];
      setRequirements(list);
    } catch {
      // Endpoint returns empty list or error if none
      setRequirements([]);
    } finally {
      setLoading(false);
    }
  }, [guest?.id, token]);

  useEffect(() => {
    if (open && guest?.id) {
      loadRequirements();
      setShowAddForm(false);
      setEditingId(null);
      setFeedback(null);
    }
  }, [open, guest?.id, loadRequirements]);

  const resetForm = () => {
    setFormData({
      requirementType: "DIETARY",
      requirementValue: "",
      severity: "MEDIUM",
      notes: "",
    });
    setEditingId(null);
    setShowAddForm(false);
  };

  const handleStartEdit = (req) => {
    setEditingId(req.id);
    setFormData({
      requirementType: req.requirementType || "DIETARY",
      requirementValue: req.requirementValue || "",
      severity: req.severity || "MEDIUM",
      notes: req.notes || "",
    });
    setShowAddForm(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.requirementValue.trim()) {
      setFeedback({ type: "error", message: "Please provide the requirement detail/value." });
      return;
    }

    setSubmitting(true);
    setFeedback(null);
    try {
      if (editingId) {
        // Update existing requirement
        await updateData(`/guest/guest-requirements/${editingId}`, formData, token);
        setFeedback({ type: "success", message: "Guest requirement updated successfully." });
      } else {
        // Create new requirement
        await createData(`/guest/${guest.id}/requirements`, formData, token);
        setFeedback({ type: "success", message: "Guest requirement recorded successfully." });
      }
      resetForm();
      await loadRequirements();
      if (onUpdated) onUpdated();
    } catch (err) {
      setFeedback({
        type: "error",
        message: err.message || "Failed to save requirement. Please try again.",
      });
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (reqId) => {
    if (!window.confirm("Are you sure you want to remove this guest requirement?")) return;
    setSubmitting(true);
    setFeedback(null);
    try {
      await deleteData(`/guest/guest-requirements/${reqId}`, token);
      setFeedback({ type: "success", message: "Requirement removed successfully." });
      await loadRequirements();
      if (onUpdated) onUpdated();
    } catch (err) {
      setFeedback({
        type: "error",
        message: err.message || "Failed to delete requirement.",
      });
    } finally {
      setSubmitting(false);
    }
  };

  if (!open || !guest) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/60 backdrop-blur-xs font-sans overflow-y-auto">
      <div className="relative w-full max-w-2xl max-h-[90vh] flex flex-col rounded-3xl bg-white shadow-2xl border border-slate-200/90 overflow-hidden my-auto">
        {/* Modal Header */}
        <div className="flex items-center justify-between border-b border-slate-100 px-6 py-4.5 bg-gradient-to-r from-slate-50 to-white">
          <div className="flex items-center gap-3">
            <div className="flex h-11 w-11 items-center justify-center rounded-2xl bg-[#264624]/10 text-[#264624] border border-indigo-100 shadow-2xs">
              <FirstAid size={24} weight="duotone" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h3 className="text-base font-bold text-slate-900 font-serif-title">
                  Guest Requirements & Preferences
                </h3>
                <span className="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold bg-indigo-50 border border-indigo-200 text-[#264624]">
                  {requirements.length} Configured
                </span>
              </div>
              <p className="text-xs text-slate-500 font-medium">
                Guest: <span className="text-slate-800 font-semibold">{guest.firstName} {guest.lastName}</span>
                {guest.passportNumber ? ` • Passport: ${guest.passportNumber}` : ""}
              </p>
            </div>
          </div>

          <button
            type="button"
            onClick={onClose}
            className="rounded-xl p-2 text-slate-400 hover:bg-slate-100 hover:text-slate-600 transition"
          >
            <X size={18} weight="bold" />
          </button>
        </div>

        {/* Modal Body */}
        <div className="flex-1 overflow-y-auto p-6 space-y-5">
          {/* Notification Feedback Banner */}
          {feedback && (
            <div
              className={`flex items-center gap-2.5 rounded-xl p-3 text-xs font-medium border ${
                feedback.type === "success"
                  ? "bg-emerald-50 text-emerald-800 border-emerald-200"
                  : "bg-rose-50 text-rose-800 border-rose-200"
              }`}
            >
              {feedback.type === "success" ? (
                <CheckCircle size={16} weight="bold" className="text-emerald-600 shrink-0" />
              ) : (
                <WarningCircle size={16} weight="bold" className="text-rose-600 shrink-0" />
              )}
              <span>{feedback.message}</span>
            </div>
          )}

          {/* Action Header */}
          <div className="flex items-center justify-between gap-3">
            <p className="text-xs text-slate-500 leading-relaxed">
              Vital health, dietary, and room accessibility requirements used by Reservation Managers when coordinating lodge bookings.
            </p>

            {canManage && !showAddForm && (
              <button
                type="button"
                onClick={() => {
                  resetForm();
                  setShowAddForm(true);
                }}
                className="inline-flex items-center gap-1.5 shrink-0 px-3.5 py-1.5 rounded-xl bg-[#264624] text-white text-xs font-bold hover:bg-[#1b331a] transition active:scale-95 shadow-2xs"
              >
                <Plus size={14} weight="bold" />
                <span>Add Requirement</span>
              </button>
            )}
          </div>

          {/* Add / Edit Form Card */}
          {showAddForm && (
            <form
              onSubmit={handleSubmit}
              className="rounded-2xl border border-indigo-100 bg-gradient-to-br from-indigo-50/40 via-white to-slate-50/50 p-4.5 space-y-4 shadow-xs"
            >
              <div className="flex items-center justify-between border-b border-indigo-100/70 pb-2">
                <span className="text-xs font-bold text-[#264624] uppercase tracking-wider font-serif-title">
                  {editingId ? "Edit Guest Requirement" : "Record New Guest Requirement"}
                </span>
                <button
                  type="button"
                  onClick={resetForm}
                  className="text-xs text-slate-400 hover:text-slate-600 font-semibold"
                >
                  Cancel
                </button>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3.5">
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">
                    Requirement Type <span className="text-rose-500">*</span>
                  </label>
                  <select
                    disabled={!!editingId}
                    value={formData.requirementType}
                    onChange={(e) => setFormData({ ...formData, requirementType: e.target.value })}
                    className="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-xs font-medium text-slate-800 focus:border-[#264624] focus:ring-1 focus:ring-[#264624] focus:outline-hidden disabled:bg-slate-100"
                  >
                    {REQUIREMENT_TYPES.map((t) => (
                      <option key={t.value} value={t.value}>
                        {t.label}
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">
                    Severity Level <span className="text-rose-500">*</span>
                  </label>
                  <select
                    value={formData.severity}
                    onChange={(e) => setFormData({ ...formData, severity: e.target.value })}
                    className="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-xs font-medium text-slate-800 focus:border-[#264624] focus:ring-1 focus:ring-[#264624] focus:outline-hidden"
                  >
                    {SEVERITY_LEVELS.map((s) => (
                      <option key={s.value} value={s.value}>
                        {s.label}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  Requirement Details / Value <span className="text-rose-500">*</span>
                </label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Strict Peanut Allergy, Diabetic, Wheelchair Access, King Bed..."
                  value={formData.requirementValue}
                  onChange={(e) => setFormData({ ...formData, requirementValue: e.target.value })}
                  className="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-xs font-medium text-slate-800 placeholder-slate-400 focus:border-[#264624] focus:ring-1 focus:ring-[#264624] focus:outline-hidden"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  Operational Notes & Action Guidelines
                </label>
                <textarea
                  rows={2}
                  placeholder="Provide explicit instructions for reservation team and lodge kitchen..."
                  value={formData.notes}
                  onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
                  className="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-xs font-medium text-slate-800 placeholder-slate-400 focus:border-[#264624] focus:ring-1 focus:ring-[#264624] focus:outline-hidden"
                />
              </div>

              <div className="flex items-center justify-end gap-2 pt-1">
                <button
                  type="button"
                  onClick={resetForm}
                  className="px-3.5 py-1.5 rounded-xl border border-slate-200 text-xs font-semibold text-slate-600 hover:bg-slate-100 transition"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submitting}
                  className="inline-flex items-center gap-1.5 px-4 py-1.5 rounded-xl bg-[#264624] text-white text-xs font-bold hover:bg-[#1b331a] transition active:scale-95 shadow-2xs disabled:opacity-50"
                >
                  {submitting && <ArrowsClockwise size={13} className="animate-spin" />}
                  <span>{editingId ? "Update Requirement" : "Save Requirement"}</span>
                </button>
              </div>
            </form>
          )}

          {/* List of Requirements */}
          {loading ? (
            <div className="py-12 text-center text-slate-400">
              <ArrowsClockwise size={28} className="animate-spin mx-auto text-[#264624] mb-2" />
              <p className="text-xs font-semibold">Loading guest requirements...</p>
            </div>
          ) : requirements.length === 0 ? (
            <div className="rounded-2xl border border-dashed border-slate-200 bg-slate-50/60 p-8 text-center">
              <ShieldWarning size={32} className="mx-auto text-slate-300 mb-2" />
              <p className="text-sm font-semibold text-slate-700 font-serif-title">
                No Special Requirements Configured
              </p>
              <p className="text-xs text-slate-400 mt-1 max-w-sm mx-auto">
                No dietary restrictions, medical alerts, or mobility requests have been recorded for this guest.
              </p>
            </div>
          ) : (
            <div className="space-y-3">
              {requirements.map((req) => {
                const typeConfig = getTypeConfig(req.requirementType);
                const IconComponent = typeConfig.icon;
                const severityConfig = getSeverityConfig(req.severity);
                const isCritical = req.severity === "CRITICAL" || req.severity === "HIGH";

                return (
                  <div
                    key={req.id}
                    className={`rounded-2xl border p-4 transition-all shadow-2xs space-y-2.5 ${
                      isCritical
                        ? "border-rose-200/90 bg-rose-50/30"
                        : "border-slate-200/80 bg-white"
                    }`}
                  >
                    <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
                      <div className="flex items-center gap-2.5">
                        <div
                          className={`flex h-8 w-8 shrink-0 items-center justify-center rounded-xl border ${
                            isCritical
                              ? "bg-rose-50 text-rose-600 border-rose-200"
                              : "bg-[#264624]/10 text-[#264624] border-indigo-100"
                          }`}
                        >
                          <IconComponent size={16} weight="duotone" />
                        </div>

                        <div>
                          <div className="flex flex-wrap items-center gap-2">
                            <span className="text-xs font-bold text-slate-900 font-serif-title">
                              {typeConfig.label}
                            </span>
                            <span
                              className={`inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold uppercase tracking-wider border ${severityConfig.color}`}
                            >
                              {req.severity || "MEDIUM"}
                            </span>
                          </div>
                          <span className="text-xs font-semibold text-slate-800 mt-0.5 block">
                            {req.requirementValue}
                          </span>
                        </div>
                      </div>

                      {canManage && (
                        <div className="flex items-center gap-1.5 self-end sm:self-auto">
                          <button
                            type="button"
                            onClick={() => handleStartEdit(req)}
                            className="p-1.5 rounded-lg text-slate-400 hover:text-[#264624] hover:bg-slate-100 transition"
                            title="Edit Requirement"
                          >
                            <PencilSimple size={14} />
                          </button>
                          <button
                            type="button"
                            onClick={() => handleDelete(req.id)}
                            className="p-1.5 rounded-lg text-slate-400 hover:text-rose-600 hover:bg-rose-50 transition"
                            title="Delete Requirement"
                          >
                            <Trash size={14} />
                          </button>
                        </div>
                      )}
                    </div>

                    {req.notes && (
                      <div className="rounded-xl bg-slate-50/80 border border-slate-100 p-2.5 text-xs text-slate-600 leading-relaxed">
                        <span className="font-semibold text-slate-700">Instructions: </span>
                        <span>{req.notes}</span>
                      </div>
                    )}

                    <div className="flex items-center justify-between text-[10px] text-slate-400 pt-1 border-t border-slate-100">
                      <span>Recorded: {req.createdAt ? new Date(req.createdAt).toLocaleDateString() : "Active"}</span>
                      {req.createdBy && <span>By: {req.createdBy}</span>}
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>

        {/* Modal Footer */}
        <div className="border-t border-slate-100 px-6 py-3.5 bg-slate-50/60 flex items-center justify-between">
          <span className="text-xs text-slate-500">
            {requirements.length} requirement{requirements.length === 1 ? "" : "s"} on file
          </span>
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-2 rounded-xl bg-slate-200 text-slate-700 text-xs font-semibold hover:bg-slate-300 transition"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  );
};

export default GuestRequirementsModal;
