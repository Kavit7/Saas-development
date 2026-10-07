import { useState, useEffect } from "react";
import { Check, MagnifyingGlass, X } from "@phosphor-icons/react";

const DynamicForm = ({
  fields = [],
  initialValues = {},
  onSubmit,
  onCancel,
  submitLabel = "Save",
  loading = false,
}) => {
  const [formData, setFormData] = useState({});
  const [searchFilters, setSearchFilters] = useState({});

  useEffect(() => {
    const data = {};
    fields.forEach((f) => {
      let val = initialValues[f.name] ?? f.defaultValue;
      if (f.type === "multiselect") {
        if (Array.isArray(val)) {
          data[f.name] = val;
        } else if (val) {
          data[f.name] = [val];
        } else {
          data[f.name] = [];
        }
      } else {
        val = val ?? "";
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
      }
    });
    setFormData(data);
  }, [fields, initialValues]);

  const handleChange = (name, value) => {
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleToggleMulti = (name, value) => {
    setFormData((prev) => {
      const current = Array.isArray(prev[name]) ? prev[name] : [];
      const exists = current.some((item) => String(item) === String(value));
      const updated = exists
        ? current.filter((item) => String(item) !== String(value))
        : [...current, value];
      return { ...prev, [name]: updated };
    });
  };

  const handleSelectAll = (name, options = []) => {
    setFormData((prev) => ({
      ...prev,
      [name]: options.map((opt) => opt.value),
    }));
  };

  const handleClearAll = (name) => {
    setFormData((prev) => ({
      ...prev,
      [name]: [],
    }));
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
          const isFullWidth =
            field.fullWidth ||
            field.type === "textarea" ||
            field.type === "multiselect";

          return (
            <div
              key={field.name}
              className={isFullWidth ? "sm:col-span-2" : "sm:col-span-1"}
            >
              <div className="mb-1.5 flex items-center justify-between">
                <label className="flex items-center text-[13px] font-semibold text-slate-700">
                  <span>{field.label}</span>
                  {field.required && <span className="ml-1 text-rose-500">*</span>}
                </label>

                {field.type === "multiselect" && (
                  <div className="flex items-center gap-2 text-xs font-medium">
                    <span className="text-slate-500 font-mono text-[11px]">
                      {(formData[field.name] || []).length} selected
                    </span>
                    <button
                      type="button"
                      onClick={() => handleSelectAll(field.name, field.options || [])}
                      className="text-[#264624] hover:underline"
                    >
                      Select all
                    </button>
                    <span className="text-slate-300">•</span>
                    <button
                      type="button"
                      onClick={() => handleClearAll(field.name)}
                      className="text-slate-500 hover:text-slate-800 hover:underline"
                    >
                      Clear
                    </button>
                  </div>
                )}
              </div>

              {field.type === "select" ? (
                <select
                  value={formData[field.name] ?? ""}
                  onChange={(e) => handleChange(field.name, e.target.value)}
                  required={field.required}
                  className="w-full rounded-xl border border-slate-200 bg-slate-50/50 px-3.5 py-2.5 text-sm text-slate-800 outline-none transition duration-150 hover:bg-white focus:border-[#264624] focus:bg-white focus:ring-2 focus:ring-[#264624]/15 shadow-2xs"
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
                  className="w-full rounded-xl border border-slate-200 bg-slate-50/50 px-3.5 py-2.5 text-sm text-slate-800 placeholder-slate-400 outline-none transition duration-150 hover:bg-white focus:border-[#264624] focus:bg-white focus:ring-2 focus:ring-[#264624]/15 shadow-2xs"
                />
              ) : field.type === "multiselect" ? (
                <div className="rounded-xl border border-slate-200 bg-slate-50/40 p-3 sm:p-4 space-y-3">
                  {(field.options || []).length > 6 && (
                    <div className="relative">
                      <MagnifyingGlass
                        size={15}
                        className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400"
                      />
                      <input
                        type="text"
                        placeholder={`Filter ${field.label.toLowerCase()}...`}
                        value={searchFilters[field.name] || ""}
                        onChange={(e) =>
                          setSearchFilters((prev) => ({
                            ...prev,
                            [field.name]: e.target.value,
                          }))
                        }
                        className="w-full rounded-lg border border-slate-200 bg-white pl-8 pr-3 py-1.5 text-xs text-slate-800 placeholder-slate-400 outline-none focus:border-[#264624] focus:ring-1 focus:ring-[#264624]"
                      />
                      {searchFilters[field.name] && (
                        <button
                          type="button"
                          onClick={() =>
                            setSearchFilters((prev) => ({ ...prev, [field.name]: "" }))
                          }
                          className="absolute right-2.5 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
                        >
                          <X size={13} />
                        </button>
                      )}
                    </div>
                  )}

                  <div className="flex flex-wrap gap-2 max-h-56 overflow-y-auto pr-1">
                    {(() => {
                      const filterText = (searchFilters[field.name] || "").toLowerCase();
                      const filteredOptions = (field.options || []).filter((opt) =>
                        String(opt.label || "").toLowerCase().includes(filterText)
                      );

                      if (filteredOptions.length === 0) {
                        return (
                          <div className="text-xs text-slate-400 italic py-2">
                            {field.options && field.options.length > 0
                              ? "No matches found."
                              : "No options available."}
                          </div>
                        );
                      }

                      const selectedList = formData[field.name] || [];

                      return filteredOptions.map((opt) => {
                        const isSelected = selectedList.some(
                          (item) => String(item) === String(opt.value)
                        );

                        return (
                          <button
                            key={opt.value}
                            type="button"
                            onClick={() => handleToggleMulti(field.name, opt.value)}
                            className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium transition active:scale-95 border ${
                              isSelected
                                ? "bg-[#264624] text-white border-[#264624] shadow-2xs"
                                : "bg-white text-slate-700 border-slate-200/90 hover:bg-slate-100 hover:border-slate-300"
                            }`}
                          >
                            <span
                              className={`flex h-3.5 w-3.5 items-center justify-center rounded border ${
                                isSelected
                                  ? "border-white bg-white/20 text-white"
                                  : "border-slate-300 bg-white"
                              }`}
                            >
                              {isSelected && <Check size={11} weight="bold" />}
                            </span>
                            <span>{opt.label}</span>
                          </button>
                        );
                      });
                    })()}
                  </div>
                </div>
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
                  className="w-full rounded-xl border border-slate-200 bg-slate-50/50 px-3.5 py-2.5 text-sm text-slate-800 placeholder-slate-400 outline-none transition duration-150 hover:bg-white focus:border-[#264624] focus:bg-white focus:ring-2 focus:ring-[#264624]/15 shadow-2xs"
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
          className="rounded-xl bg-[#264624] hover:bg-[#1b331a] px-6 py-2.5 text-sm font-semibold text-white shadow-md shadow-emerald-950/20 transition active:scale-[0.98] disabled:opacity-50 flex items-center gap-2"
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
