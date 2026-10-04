import { ArrowLeft, MapPin, CalendarBlank, Bed, Plus, Trash } from "@phosphor-icons/react";

const AccommodationFormView = ({
  mode = "create",
  itineraryDay,
  safariTitle = "Safari",
  dynamicOptions = {},
  reqForm,
  setReqForm,
  onSubmit,
  onCancel,
  loading = false,
}) => {
  return (
    <div className="space-y-5 animate-in fade-in duration-200 font-sans">
      {/* Top Navigation Bar */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 rounded-2xl border border-slate-200/90 bg-white p-4 shadow-xs">
        <div className="flex flex-wrap items-center gap-3">
          <button
            type="button"
            onClick={onCancel}
            className="inline-flex items-center gap-2 rounded-xl border border-slate-200 bg-slate-50/80 px-3.5 py-2 text-xs font-semibold text-slate-700 hover:border-slate-300 hover:bg-slate-100 hover:text-[#101B82] transition active:scale-95 group shadow-2xs"
          >
            <ArrowLeft size={16} weight="bold" className="transition-transform group-hover:-translate-x-0.5" />
            <span>Cancel & Back</span>
          </button>
          <div className="h-5 w-px bg-slate-200 hidden sm:block" />
          <div className="flex items-center gap-1.5 text-xs text-slate-500 font-medium">
            <span className="text-slate-400">{safariTitle}</span>
            <span className="text-slate-300">/</span>
            <span className="text-slate-400">Day {itineraryDay?.dayNumber}</span>
            <span className="text-slate-300">/</span>
            <span className="text-slate-900 font-semibold">
              {mode === "edit" ? "Edit Accommodation" : "New Accommodation Requirement"}
            </span>
          </div>
        </div>
      </div>

      {/* Main Form Card */}
      <div className="relative overflow-hidden rounded-2xl border border-slate-200/90 bg-white p-6 sm:p-8 shadow-xs">
        <div className="absolute top-0 inset-x-0 h-1.5 bg-gradient-to-r from-[#101B82] via-indigo-600 to-blue-500" />

        <div className="mb-6 border-b border-slate-100 pb-5">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
            <div className="flex items-center gap-3">
              <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-indigo-50 border border-indigo-100 text-[#101B82]">
                <Bed size={26} weight="duotone" />
              </div>
              <div>
                <h2 className="text-xl sm:text-2xl font-bold tracking-tight text-slate-900 font-serif-title">
                  {mode === "edit" ? "Edit Accommodation Requirement" : "Configure Accommodation Requirement"}
                </h2>
                <p className="text-xs sm:text-sm text-slate-500 mt-0.5">
                  Set lodging specifications, price tier, and room breakdown for this day.
                </p>
              </div>
            </div>

            {/* Destination & Date Banner */}
            <div className="flex flex-wrap items-center gap-3 rounded-xl bg-slate-50/90 border border-slate-200/80 px-4 py-2.5 text-xs">
              <div className="flex items-center gap-1.5">
                <span className="inline-flex px-2 py-0.5 rounded bg-[#101B82] text-white font-bold text-[11px]">
                  Day {itineraryDay?.dayNumber}
                </span>
              </div>
              <div className="flex items-center gap-1.5 font-bold text-slate-900">
                <MapPin size={16} className="text-[#101B82]" weight="fill" />
                <span>{itineraryDay?.destination || "Destination not set"}</span>
              </div>
              <div className="flex items-center gap-1.5 text-slate-500 font-medium">
                <CalendarBlank size={15} />
                <span>{itineraryDay?.date || "—"}</span>
              </div>
            </div>
          </div>
        </div>

        <form onSubmit={onSubmit} className="space-y-6 max-w-3xl">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
            <div>
              <label className="block text-[13px] font-semibold text-slate-700 mb-1.5">
                Property Category <span className="text-rose-500">*</span>
              </label>
              <select
                required
                value={reqForm.categoryId}
                onChange={(e) => setReqForm({ ...reqForm, categoryId: e.target.value })}
                className="w-full rounded-xl border border-slate-200 bg-slate-50/50 px-3.5 py-2.5 text-sm text-slate-800 outline-none transition duration-150 hover:bg-white focus:border-[#101B82] focus:bg-white focus:ring-2 focus:ring-[#101B82]/15 shadow-2xs"
              >
                <option value="">Select Category...</option>
                {(dynamicOptions.categories || []).map((cat) => (
                  <option key={cat.value} value={cat.value}>
                    {cat.label}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-[13px] font-semibold text-slate-700 mb-1.5">
                Price Tier <span className="text-rose-500">*</span>
              </label>
              <select
                required
                value={reqForm.pricetierId}
                onChange={(e) => setReqForm({ ...reqForm, pricetierId: e.target.value })}
                className="w-full rounded-xl border border-slate-200 bg-slate-50/50 px-3.5 py-2.5 text-sm text-slate-800 outline-none transition duration-150 hover:bg-white focus:border-[#101B82] focus:bg-white focus:ring-2 focus:ring-[#101B82]/15 shadow-2xs"
              >
                <option value="">Select Price Tier...</option>
                {(dynamicOptions.priceTiers || []).map((tier) => (
                  <option key={tier.value} value={tier.value}>
                    {tier.label}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div>
            <label className="block text-[13px] font-semibold text-slate-700 mb-1.5">
              Total Number of Rooms <span className="text-rose-500">*</span>
            </label>
            <input
              type="number"
              min="1"
              required
              value={reqForm.numberOfrooms}
              onChange={(e) =>
                setReqForm({ ...reqForm, numberOfrooms: parseInt(e.target.value, 10) || 1 })
              }
              className="w-full sm:w-48 rounded-xl border border-slate-200 bg-slate-50/50 px-3.5 py-2.5 text-sm text-slate-800 outline-none transition duration-150 hover:bg-white focus:border-[#101B82] focus:bg-white focus:ring-2 focus:ring-[#101B82]/15 shadow-2xs"
            />
          </div>

          {/* Room Requirements Breakdown */}
          <div className="space-y-3 rounded-2xl border border-slate-200/80 bg-slate-50/40 p-4 sm:p-5">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 border-b border-slate-200/70 pb-3">
              <div>
                <label className="text-[13px] font-bold text-slate-800 block">
                  Room Types Breakdown <span className="text-rose-500">*</span>
                </label>
                <p className="text-xs text-slate-500">
                  Assign quantities for each room type so they sum up to {reqForm.numberOfrooms} room(s).
                </p>
              </div>

              <button
                type="button"
                onClick={() =>
                  setReqForm({
                    ...reqForm,
                    roomRequirements: [
                      ...reqForm.roomRequirements,
                      { roomType: dynamicOptions.roomTypes?.[0]?.value || "Standard", quantity: 1 },
                    ],
                  })
                }
                className="inline-flex items-center gap-1.5 text-xs font-semibold text-[#101B82] hover:text-[#0c145e] hover:underline"
              >
                <Plus size={14} weight="bold" />
                <span>Add Another Room Type</span>
              </button>
            </div>

            <div className="space-y-2.5">
              {reqForm.roomRequirements.map((row, idx) => (
                <div key={idx} className="flex items-center gap-3 bg-white p-2.5 rounded-xl border border-slate-200/80 shadow-2xs">
                  <div className="flex-1">
                    <select
                      required
                      value={row.roomType}
                      onChange={(e) => {
                        const updated = [...reqForm.roomRequirements];
                        updated[idx].roomType = e.target.value;
                        setReqForm({ ...reqForm, roomRequirements: updated });
                      }}
                      className="w-full rounded-lg border border-slate-200 bg-slate-50/50 px-3 py-2 text-sm text-slate-800 outline-none transition hover:bg-white focus:border-[#101B82] focus:bg-white focus:ring-2 focus:ring-[#101B82]/15"
                    >
                      <option value="">Select Room Type</option>
                      {(dynamicOptions.roomTypes || []).map((rt) => (
                        <option key={rt.value} value={rt.value}>
                          {rt.label}
                        </option>
                      ))}
                    </select>
                  </div>

                  <div className="w-28">
                    <input
                      type="number"
                      min="1"
                      required
                      placeholder="Quantity"
                      value={row.quantity}
                      onChange={(e) => {
                        const updated = [...reqForm.roomRequirements];
                        updated[idx].quantity = parseInt(e.target.value, 10) || 1;
                        setReqForm({ ...reqForm, roomRequirements: updated });
                      }}
                      className="w-full rounded-lg border border-slate-200 bg-slate-50/50 px-3 py-2 text-sm text-slate-800 outline-none transition hover:bg-white focus:border-[#101B82] focus:bg-white focus:ring-2 focus:ring-[#101B82]/15 text-center font-semibold"
                    />
                  </div>

                  {reqForm.roomRequirements.length > 1 && (
                    <button
                      type="button"
                      onClick={() => {
                        const updated = reqForm.roomRequirements.filter((_, i) => i !== idx);
                        setReqForm({ ...reqForm, roomRequirements: updated });
                      }}
                      className="p-2 text-rose-500 hover:bg-rose-50 rounded-xl transition"
                      title="Remove room type"
                    >
                      <Trash size={16} />
                    </button>
                  )}
                </div>
              ))}
            </div>

            <div className="text-xs text-slate-500 flex items-center justify-between pt-2 border-t border-slate-200/50">
              <span>
                Total assigned:{" "}
                <strong
                  className={
                    reqForm.roomRequirements.reduce((sum, r) => sum + (parseInt(r.quantity, 10) || 0), 0) ===
                    parseInt(reqForm.numberOfrooms, 10)
                      ? "text-emerald-600 font-bold"
                      : "text-amber-600 font-bold"
                  }
                >
                  {reqForm.roomRequirements.reduce((sum, r) => sum + (parseInt(r.quantity, 10) || 0), 0)}
                </strong>{" "}
                of {reqForm.numberOfrooms} room(s)
              </span>
            </div>
          </div>

          <div>
            <label className="block text-[13px] font-semibold text-slate-700 mb-1.5">
              Room Preferences
            </label>
            <textarea
              rows={2}
              placeholder="e.g. Ground floor, interconnected rooms, ocean view, king bed"
              value={reqForm.roomPreferences}
              onChange={(e) => setReqForm({ ...reqForm, roomPreferences: e.target.value })}
              className="w-full rounded-xl border border-slate-200 bg-slate-50/50 px-3.5 py-2.5 text-sm text-slate-800 placeholder-slate-400 outline-none transition hover:bg-white focus:border-[#101B82] focus:bg-white focus:ring-2 focus:ring-[#101B82]/15 shadow-2xs"
            />
          </div>

          <div>
            <label className="block text-[13px] font-semibold text-slate-700 mb-1.5">
              Special Requests
            </label>
            <textarea
              rows={2}
              placeholder="e.g. Late check-in, honeymoon setup, gluten-free dining"
              value={reqForm.specialRequests}
              onChange={(e) => setReqForm({ ...reqForm, specialRequests: e.target.value })}
              className="w-full rounded-xl border border-slate-200 bg-slate-50/50 px-3.5 py-2.5 text-sm text-slate-800 placeholder-slate-400 outline-none transition hover:bg-white focus:border-[#101B82] focus:bg-white focus:ring-2 focus:ring-[#101B82]/15 shadow-2xs"
            />
          </div>

          <div className="flex items-center justify-end gap-3 pt-4 border-t border-slate-100">
            <button
              type="button"
              onClick={onCancel}
              className="rounded-xl border border-slate-200 px-5 py-2.5 text-xs font-semibold text-slate-600 hover:bg-slate-50 hover:text-slate-900 transition shadow-2xs"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="rounded-xl bg-gradient-to-r from-[#101B82] to-[#1E2EAA] px-6 py-2.5 text-xs font-semibold uppercase tracking-wider text-white shadow-sm hover:shadow hover:from-[#0d176f] hover:to-[#17248e] transition active:scale-[0.98] disabled:opacity-50"
            >
              {loading ? "Saving..." : "Save Requirement"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default AccommodationFormView;
