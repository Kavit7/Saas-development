import { useState, useEffect } from "react";

const DynamicForm = ({
  fields = [],
  initialValues = {},
  onSubmit,
  onCancel,
  submitLabel = "Save",
  loading = false,
}) => {
  const [formData, setFormData] = useState({});

  useEffect(() => {
    const data = {};
    fields.forEach((f) => {
      let val = initialValues[f.name] ?? f.defaultValue ?? "";
      if (val && f.type === "datetime-local") {
        const d = new Date(val);
        if (!isNaN(d.getTime())) {
          const pad = (n) => String(n).padStart(2, "0");
          val = `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
        }
      } else if (val && f.type === "date") {
        if (typeof val === "string" && val.length > 10 && val.includes("T")) {
          val = val.substring(0, 10);
        } else if (Array.isArray(val) && val.length >= 3) {
          const [y, m, d] = val;
          val = `${y}-${String(m).padStart(2, "0")}-${String(d).padStart(2, "0")}`;
        }
      }
      data[f.name] = val;
    });
    setFormData(data);
  }, [fields, initialValues]);

  const handleChange = (name, value) => {
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (onSubmit) {
      onSubmit(formData);
    }
  };

  const visibleFields = fields.filter((f) => f.editable !== false);

  return (
    <form onSubmit={handleSubmit} className="space-y-5">
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        {visibleFields.map((field) => {
          const isFullWidth = field.fullWidth || field.type === "textarea";
          return (
            <div
              key={field.name}
              className={isFullWidth ? "sm:col-span-2" : "sm:col-span-1"}
            >
              <label className="mb-1.5 flex items-center text-[13px] font-semibold text-slate-700">
                <span>{field.label}</span>
                {field.required && <span className="ml-1 text-rose-500">*</span>}
              </label>

              {field.type === "select" ? (
                <select
                  value={formData[field.name] ?? ""}
                  onChange={(e) => handleChange(field.name, e.target.value)}
                  required={field.required}
                  className="w-full rounded-xl border border-slate-200 bg-slate-50/50 px-3.5 py-2.5 text-sm text-slate-800 outline-none transition duration-150 hover:bg-white focus:border-[#101B82] focus:bg-white focus:ring-2 focus:ring-[#101B82]/15 shadow-2xs"
                >
                  <option value="">Select {field.label}...</option>
                  {(field.options || []).map((opt) => (
                    <option key={opt.value} value={opt.value}>
                      {opt.label}
                    </option>
                  ))}
                </select>
              ) : field.type === "textarea" ? (
                <textarea
                  rows={3}
                  value={formData[field.name] ?? ""}
                  onChange={(e) => handleChange(field.name, e.target.value)}
                  required={field.required}
                  placeholder={field.placeholder || `Enter ${field.label.toLowerCase()}...`}
                  className="w-full rounded-xl border border-slate-200 bg-slate-50/50 px-3.5 py-2.5 text-sm text-slate-800 placeholder-slate-400 outline-none transition duration-150 hover:bg-white focus:border-[#101B82] focus:bg-white focus:ring-2 focus:ring-[#101B82]/15 shadow-2xs"
                />
              ) : (
                <input
                  type={field.type || "text"}
                  value={formData[field.name] ?? ""}
                  onChange={(e) =>
                    handleChange(
                      field.name,
                      field.type === "number"
                        ? e.target.value === ""
                          ? ""
                          : Number(e.target.value)
                        : e.target.value
                    )
                  }
                  required={field.required}
                  placeholder={field.placeholder || `Enter ${field.label.toLowerCase()}...`}
                  className="w-full rounded-xl border border-slate-200 bg-slate-50/50 px-3.5 py-2.5 text-sm text-slate-800 placeholder-slate-400 outline-none transition duration-150 hover:bg-white focus:border-[#101B82] focus:bg-white focus:ring-2 focus:ring-[#101B82]/15 shadow-2xs"
                />
              )}
            </div>
          );
        })}
      </div>

      <div className="flex items-center justify-end gap-3 pt-4 border-t border-slate-100">
        {onCancel && (
          <button
            type="button"
            onClick={onCancel}
            disabled={loading}
            className="rounded-xl border border-slate-200 px-4 py-2.5 text-sm font-semibold text-slate-600 hover:text-slate-900 hover:bg-slate-50 transition"
          >
            Cancel
          </button>
        )}
        <button
          type="submit"
          disabled={loading}
          className="rounded-xl bg-gradient-to-r from-[#101B82] to-[#1E2EAA] hover:from-[#0c1566] hover:to-[#172382] px-6 py-2.5 text-sm font-semibold text-white shadow-md shadow-indigo-950/20 transition active:scale-[0.98] disabled:opacity-50 flex items-center gap-2"
        >
          {loading && (
            <span className="inline-block h-4 w-4 animate-spin rounded-full border-2 border-white border-t-transparent" />
          )}
          {loading ? "Saving..." : submitLabel}
        </button>
      </div>
    </form>
  );
};

export default DynamicForm;
