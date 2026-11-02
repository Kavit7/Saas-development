import { useState, useEffect, useMemo } from "react";
import { Check, MagnifyingGlass, X, CaretDown } from "@phosphor-icons/react";

const DynamicForm = ({
  fields = [],
  initialValues = {},
  onSubmit,
  onCancel,
  submitLabel,
  cancelLabel = "Cancel",
  singular = "Record",
  mode = "create",
  loading = false,
  sectionTitle,
  sectionDescription,
  embedded = false,
}) => {
  const [formData, setFormData] = useState({});
  const [searchFilters, setSearchFilters] = useState({});

  const shortSingular = singular ? singular.trim().split(/\s+/).pop() : "Record";
  const finalSubmitLabel =
    submitLabel || (mode === "create" ? `Save ${shortSingular}` : "Save Changes");

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

  // Group fields into structured sections
  const groupedSections = useMemo(() => {
    const hasCustomSections = visibleFields.some((f) => f.section);
    if (hasCustomSections) {
      const map = {};
      visibleFields.forEach((f) => {
        const sec = f.section || "General Information";
        if (!map[sec]) map[sec] = [];
        map[sec].push(f);
      });
      return Object.entries(map).map(([title, items]) => ({ title, items }));
    }

    // Smart grouping for Safari entity
    if (singular.toLowerCase().includes("safari")) {
      const expeditionFields = visibleFields.filter((f) =>
        [
          "clientId",
          "clientName",
          "salesPersonName",
          "referenceNumber",
          "startDate",
          "endDate",
          "numberOfPassengers",
          "status",
        ].includes(f.name)
      );
      const notesFields = visibleFields.filter((f) =>
        ["notes", "specialRequests", "description"].includes(f.name)
      );
      const otherFields = visibleFields.filter(
        (f) => !expeditionFields.includes(f) && !notesFields.includes(f)
      );

      const groups = [];
      if (expeditionFields.length > 0 || otherFields.length > 0) {
        groups.push({
          title: "Expedition Details",
          items: [...expeditionFields, ...otherFields],
        });
      }
      if (notesFields.length > 0) {
        groups.push({
          title: "Special Instructions & Notes",
          items: notesFields,
        });
      }

      return groups.length > 0 ? groups : [{ title: null, items: visibleFields }];
    }

    return [{ title: null, items: visibleFields }];
  }, [visibleFields, singular]);

  // Shared fields content
  const renderFieldGrid = () => (
    <div className="space-y-6">
      {groupedSections.map((group, gIdx) => (
        <div key={gIdx} className="space-y-3">
          {group.title && groupedSections.length > 1 && (
            <h3 className="text-xs sm:text-sm font-bold text-[#28332C] tracking-tight">
              {group.title}
            </h3>
          )}

          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-x-5 gap-y-4">
            {group.items.map((field) => {
              const isFullWidth =
                field.fullWidth ||
                field.type === "textarea" ||
                field.type === "multiselect";

              return (
                <div
                  key={field.name}
                  className={isFullWidth ? "col-span-1 sm:col-span-2 md:col-span-3" : "col-span-1"}
                >
                  <div className="mb-1.5 flex items-center justify-between">
                    <label className="text-xs font-medium text-slate-700 tracking-tight block">
                      <span>{field.label}</span>
                      {field.required && <span className="ml-0.5 text-rose-500">*</span>}
                    </label>

                    {field.type === "multiselect" && (
                      <div className="flex items-center gap-2 text-[11px] font-medium">
                        <span className="text-slate-400 font-mono text-[10px]">
                          {(formData[field.name] || []).length} selected
                        </span>
                        <button
                          type="button"
                          onClick={() => handleSelectAll(field.name, field.options || [])}
                          className="text-[#35634B] hover:underline"
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
                    <div className="relative">
                      <select
                        value={formData[field.name] ?? ""}
                        onChange={(e) => handleChange(field.name, e.target.value)}
                        required={field.required}
                        aria-label={field.label}
                        className="h-10 w-full appearance-none rounded-md border border-slate-200/90 bg-white px-3.5 pr-8 text-xs text-slate-800 outline-none transition focus:border-[#35634B] focus:ring-1 focus:ring-[#35634B]/20 shadow-2xs cursor-pointer hover:border-slate-300"
                      >
                        <option value="">Select {field.label.toLowerCase()}...</option>
                        {(field.options || []).map((opt) => (
                          <option key={opt.value} value={opt.value}>
                            {opt.label}
                          </option>
                        ))}
                      </select>
                      <CaretDown
                        size={13}
                        className="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-slate-400"
                      />
                    </div>
                  ) : field.type === "textarea" ? (
                    <textarea
                      rows={3}
                      value={formData[field.name] ?? ""}
                      onChange={(e) => handleChange(field.name, e.target.value)}
                      required={field.required}
                      placeholder={field.placeholder || "Type here"}
                      className="w-full rounded-md border border-slate-200/90 bg-white p-3.5 text-xs text-slate-800 placeholder:text-slate-400 outline-none transition focus:border-[#35634B] focus:ring-1 focus:ring-[#35634B]/20 shadow-2xs hover:border-slate-300"
                    />
                  ) : field.type === "multiselect" ? (
                    <div className="rounded-md border border-slate-200 bg-slate-50/40 p-3 space-y-2.5">
                      {(field.options || []).length > 6 && (
                        <div className="relative">
                          <MagnifyingGlass
                            size={14}
                            className="absolute left-2.5 top-1/2 -translate-y-1/2 text-slate-400"
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
                            className="h-9 w-full rounded border border-slate-200 bg-white pl-8 pr-3 text-xs text-slate-800 placeholder:text-slate-400 outline-none focus:border-[#35634B] focus:ring-1 focus:ring-[#35634B]/20"
                          />
                          {searchFilters[field.name] && (
                            <button
                              type="button"
                              onClick={() =>
                                setSearchFilters((prev) => ({ ...prev, [field.name]: "" }))
                              }
                              className="absolute right-2.5 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
                            >
                              <X size={12} />
                            </button>
                          )}
                        </div>
                      )}

                      <div className="flex flex-wrap gap-1.5 max-h-48 overflow-y-auto pr-1">
                        {(() => {
                          const filterText = (searchFilters[field.name] || "").toLowerCase();
                          const filteredOptions = (field.options || []).filter((opt) =>
                            String(opt.label || "").toLowerCase().includes(filterText)
                          );

                          if (filteredOptions.length === 0) {
                            return (
                              <div className="text-xs text-slate-400 italic py-1">
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
                                className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded text-xs font-medium transition border ${
                                  isSelected
                                    ? "bg-[#35634B] text-white border-[#35634B] shadow-2xs"
                                    : "bg-white text-slate-700 border-slate-200/90 hover:bg-slate-100"
                                }`}
                              >
                                <span
                                  className={`flex h-3 w-3 items-center justify-center rounded border ${
                                    isSelected
                                      ? "border-white bg-white/20 text-white"
                                      : "border-slate-300 bg-white"
                                  }`}
                                >
                                  {isSelected && <Check size={10} weight="bold" />}
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
                      placeholder={field.placeholder || "Type here"}
                      className="h-10 w-full rounded-md border border-slate-200/90 bg-white px-3.5 text-xs text-slate-800 placeholder:text-slate-400 outline-none transition focus:border-[#35634B] focus:ring-1 focus:ring-[#35634B]/20 shadow-2xs hover:border-slate-300"
                    />
                  )}
                </div>
              );
            })}
          </div>
        </div>
      ))}
    </div>
  );

  // If embedded in a parent card (e.g. SubFormView)
  if (embedded) {
    return (
      <form onSubmit={handleSubmit} className="space-y-6 font-sans">
        {renderFieldGrid()}

        <div className="flex items-center justify-between pt-4 border-t border-slate-100">
          {onCancel ? (
            <button
              type="button"
              onClick={onCancel}
              disabled={loading}
              className="px-7 py-2.5 rounded-lg border border-slate-300 bg-white text-slate-700 hover:bg-slate-50 hover:text-slate-900 text-xs sm:text-sm font-semibold transition shadow-2xs disabled:opacity-50"
            >
              {cancelLabel || "Cancel"}
            </button>
          ) : (
            <div />
          )}

          <button
            type="submit"
            disabled={loading}
            className="px-8 py-2.5 rounded-lg bg-[#35634B] hover:bg-[#284C39] text-white text-xs sm:text-sm font-semibold transition shadow-sm active:scale-[0.98] disabled:opacity-50 flex items-center gap-2"
          >
            {loading && (
              <span className="inline-block h-3.5 w-3.5 animate-spin rounded-full border-2 border-white border-t-transparent" />
            )}
            <span>{finalSubmitLabel}</span>
          </button>
        </div>
      </form>
    );
  }

  // Standard Standalone Form Container matching form_example.jpg
  return (
    <form onSubmit={handleSubmit} className="max-w-4xl mx-auto space-y-6 font-sans">
      {/* Main Card Container */}
      <div className="bg-white rounded-xl border border-slate-200/90 shadow-sm overflow-hidden">
        {/* Shaded Section Header matching format */}
        <div className="bg-[#F8FAF8] border-b border-slate-200/80 px-6 py-4 sm:px-8 sm:py-5">
          <h2 className="text-sm sm:text-base font-bold text-[#28332C] tracking-tight">
            {sectionTitle || `${singular} Details`}
          </h2>
          <p className="text-xs text-slate-500 mt-0.5">
            {sectionDescription ||
              `Please complete the required fields below to configure this ${singular.toLowerCase()}.`}
          </p>
        </div>

        {/* Form Fields Body (3-Column Grid) */}
        <div className="p-6 sm:p-8 bg-white">
          {renderFieldGrid()}
        </div>
      </div>

      {/* Outside Bottom Action Buttons using system colors */}
      <div className="flex items-center justify-between px-1 pt-1">
        {onCancel ? (
          <button
            type="button"
            onClick={onCancel}
            disabled={loading}
            className="px-8 py-2.5 rounded-lg border border-slate-300 bg-white text-slate-700 hover:bg-slate-50 hover:text-slate-900 text-xs sm:text-sm font-semibold transition shadow-2xs disabled:opacity-50"
          >
            {cancelLabel || "Cancel"}
          </button>
        ) : (
          <div />
        )}

        <button
          type="submit"
          disabled={loading}
          className="inline-flex items-center justify-center gap-2 rounded-xl bg-gradient-to-r from-[#264624] via-[#2D532B] to-[#35634B] px-8 py-2.5 text-xs sm:text-sm font-semibold text-white shadow-xs hover:shadow-md hover:shadow-[#264624]/20 transition-all active:scale-[0.98] disabled:opacity-50 cursor-pointer border border-white/10"
        >
          {loading ? (
            <span className="inline-block h-3.5 w-3.5 animate-spin rounded-full border-2 border-white border-t-transparent" />
          ) : (
            <Check size={16} weight="bold" />
          )}
          <span>{finalSubmitLabel}</span>
        </button>
      </div>
    </form>
  );
};

export default DynamicForm;
