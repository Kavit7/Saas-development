import { useState, useEffect } from "react";
import { X, Bed, CalendarCheck, Buildings, WarningCircle, CheckCircle } from "@phosphor-icons/react";
import { getAllData, createData } from "../../api/api";
import { useAuth } from "../../hooks/useAuth";

/**
 * BookingModal Component
 * Modal dialog to allocate a partner property and create an accommodation booking
 * for a specific safari accommodation requirement.
 */
const BookingModal = ({
  open,
  onClose,
  requirement,
  safari,
  onSuccess,
}) => {
  const { token } = useAuth();
  const [properties, setProperties] = useState([]);
  const [loadingProps, setLoadingProps] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  // Form State
  const [selectedPropertyId, setSelectedPropertyId] = useState("");
  const [checkIn, setCheckIn] = useState("");
  const [checkOut, setCheckOut] = useState("");
  const [notes, setNotes] = useState("");

  // Populate initial dates and load properties
  useEffect(() => {
    if (!open) return;

    setError(null);
    setSelectedPropertyId("");
    setNotes(requirement?.specialRequests || "");

    // Default dates
    const initialCheckIn = requirement?.itineraryDay?.date || safari?.startDate || new Date().toISOString().substring(0, 10);
    setCheckIn(initialCheckIn);

    // Default checkout: +1 day
    const checkInDate = new Date(initialCheckIn);
    if (!isNaN(checkInDate.getTime())) {
      checkInDate.setDate(checkInDate.getDate() + 1);
      setCheckOut(checkInDate.toISOString().substring(0, 10));
    }

    // Load available partner properties
    const fetchProperties = async () => {
      setLoadingProps(true);
      setError(null);
      try {
        let res;
        try {
          res = await getAllData("/properties?size=100&sortBy=name&direction=asc", token);
        } catch {
          res = await getAllData("/api/v1/properties?size=100&sortBy=name&direction=asc", token);
        }
        const list = res?.content || res?.data || (Array.isArray(res) ? res : []);
        setProperties(Array.isArray(list) ? list : []);
        if (list.length > 0) {
          setSelectedPropertyId(list[0].id);
        }
      } catch (err) {
        console.error("Failed to load properties for booking:", err);
        setError("Failed to load properties: " + (err.message || "Network error"));
      } finally {
        setLoadingProps(false);
      }
    };

    fetchProperties();
  }, [open, requirement, safari, token]);

  if (!open) return null;

  const selectedProperty = properties.find((p) => String(p.id) === String(selectedPropertyId));

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!selectedPropertyId) {
      setError("Please select a partner property / lodge.");
      return;
    }

    if (!checkIn || !checkOut) {
      setError("Check-in and Check-out dates are required.");
      return;
    }

    if (new Date(checkOut) <= new Date(checkIn)) {
      setError("Check-out date must be after check-in date.");
      return;
    }

    setSubmitting(true);
    setError(null);

    try {
      const payload = {
        accomodationRequirementId: requirement.id,
        propertyId: selectedPropertyId,
        checkIn,
        checkOut,
        notes,
      };

      const result = await createData("/accommodation-bookings", payload, token);
      if (onSuccess) {
        onSuccess(result);
      }
      onClose();
    } catch (err) {
      setError(err?.message || "Failed to create booking.");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-xs animate-in fade-in duration-150">
      <div className="w-full max-w-lg rounded-2xl bg-white shadow-xl border border-slate-200 overflow-hidden font-sans animate-in zoom-in-95 duration-150">
        {/* Header */}
        <div className="flex items-center justify-between border-b border-slate-100 px-6 py-4 bg-slate-50/50">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-lg bg-indigo-50 text-[#101B82] flex items-center justify-center font-bold">
              <Bed size={18} weight="duotone" />
            </div>
            <div>
              <h3 className="font-bold text-slate-900 font-serif-title">
                Allocate & Book Lodge
              </h3>
              <p className="text-xs text-slate-500">
                Destination: {requirement?.destination || "Safari Circuit"} ({requirement?.numberOfRooms || requirement?.numberOfrooms || 1} Room(s))
              </p>
            </div>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="rounded-lg p-1.5 text-slate-400 hover:text-slate-600 hover:bg-slate-100 transition"
          >
            <X size={18} />
          </button>
        </div>

        {/* Form Body */}
        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          {error && (
            <div className="p-3 rounded-xl bg-rose-50 border border-rose-200 text-rose-800 text-xs font-medium flex items-center gap-2">
              <WarningCircle size={16} className="shrink-0" />
              <span>{error}</span>
            </div>
          )}

          {/* Property Select */}
          <div>
            <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">
              Select Partner Property / Lodge <span className="text-rose-500">*</span>
            </label>
            {loadingProps ? (
              <div className="h-10 rounded-xl bg-slate-100 animate-pulse flex items-center px-4 text-xs text-slate-400">
                Loading available lodges...
              </div>
            ) : (
              <select
                value={selectedPropertyId}
                onChange={(e) => setSelectedPropertyId(e.target.value)}
                required
                className="w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2.5 text-xs text-slate-800 font-medium focus:border-[#101B82] focus:ring-1 focus:ring-[#101B82] outline-hidden transition shadow-2xs"
              >
                <option value="">
                  {properties.length === 0
                    ? "-- No lodges available in system --"
                    : "-- Choose Accommodation / Lodge --"}
                </option>
                {properties.map((p) => {
                  const locationInfo = [p.location, p.region, p.country].filter(Boolean).join(", ");
                  const categoryInfo = p.categoryName ? ` • ${p.categoryName}` : "";
                  const tierInfo = p.priceTierName ? ` • ${p.priceTierName}` : "";
                  return (
                    <option key={p.id} value={p.id}>
                      {p.name} {locationInfo ? `(${locationInfo})` : ""}{categoryInfo}{tierInfo}
                    </option>
                  );
                })}
              </select>
            )}

            {/* Selected Property Preview Badge */}
            {selectedProperty && (
              <div className="mt-2.5 rounded-xl border border-indigo-100 bg-indigo-50/50 p-3 text-xs text-slate-700">
                <div className="flex items-center justify-between font-bold text-[#101B82]">
                  <span className="flex items-center gap-1.5">
                    <Buildings size={16} weight="duotone" />
                    {selectedProperty.name}
                  </span>
                  {selectedProperty.verificationStatus && (
                    <span className="rounded-full bg-emerald-100 px-2 py-0.5 text-[10px] font-bold text-emerald-800">
                      {selectedProperty.verificationStatus}
                    </span>
                  )}
                </div>
                <div className="mt-2 grid grid-cols-2 gap-2 text-[11px] text-slate-600">
                  <div>
                    <span className="font-semibold text-slate-400">Location:</span>{" "}
                    {[selectedProperty.location, selectedProperty.region, selectedProperty.country].filter(Boolean).join(", ") || "—"}
                  </div>
                  <div>
                    <span className="font-semibold text-slate-400">Category:</span>{" "}
                    {selectedProperty.categoryName || "Standard Lodge"}
                  </div>
                  {selectedProperty.priceTierName && (
                    <div>
                      <span className="font-semibold text-slate-400">Price Tier:</span>{" "}
                      {selectedProperty.priceTierName}
                    </div>
                  )}
                  {selectedProperty.contactEmail && (
                    <div>
                      <span className="font-semibold text-slate-400">Email:</span>{" "}
                      {selectedProperty.contactEmail}
                    </div>
                  )}
                </div>
              </div>
            )}

            {/* Empty Alert */}
            {!loadingProps && properties.length === 0 && (
              <div className="mt-2 rounded-xl bg-amber-50 border border-amber-200 p-3 text-xs text-amber-800 flex items-start gap-2">
                <WarningCircle size={18} className="shrink-0 text-amber-600 mt-0.5" />
                <div>
                  <p className="font-bold">No partner lodges found</p>
                  <p className="mt-0.5 text-[11px] text-amber-700">
                    Please make sure partner properties have been registered in the system under the <strong>Properties</strong> menu.
                  </p>
                </div>
              </div>
            )}
          </div>

          {/* Dates Row */}
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">
                Check-in Date <span className="text-rose-500">*</span>
              </label>
              <input
                type="date"
                value={checkIn}
                onChange={(e) => setCheckIn(e.target.value)}
                required
                className="w-full rounded-xl border border-slate-200 px-3.5 py-2.5 text-xs text-slate-800 font-medium focus:border-[#101B82] focus:ring-1 focus:ring-[#101B82] outline-hidden transition shadow-2xs"
              />
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">
                Check-out Date <span className="text-rose-500">*</span>
              </label>
              <input
                type="date"
                value={checkOut}
                onChange={(e) => setCheckOut(e.target.value)}
                required
                className="w-full rounded-xl border border-slate-200 px-3.5 py-2.5 text-xs text-slate-800 font-medium focus:border-[#101B82] focus:ring-1 focus:ring-[#101B82] outline-hidden transition shadow-2xs"
              />
            </div>
          </div>

          {/* Notes */}
          <div>
            <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">
              Special Requests & Meal Preferences
            </label>
            <textarea
              rows={3}
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              placeholder="e.g., Full Board, 2 vegetarian meals, honeymoon amenities requested"
              className="w-full rounded-xl border border-slate-200 px-3.5 py-2.5 text-xs text-slate-800 placeholder-slate-400 focus:border-[#101B82] focus:ring-1 focus:ring-[#101B82] outline-hidden transition shadow-2xs resize-none"
            />
          </div>

          {/* Actions */}
          <div className="flex items-center justify-end gap-3 pt-3 border-t border-slate-100">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-xs font-semibold text-slate-600 hover:bg-slate-100 rounded-xl transition"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={submitting || properties.length === 0}
              className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl bg-[#101B82] text-white text-xs font-bold hover:bg-[#0c145e] transition active:scale-95 shadow-xs disabled:opacity-50"
            >
              <CalendarCheck size={16} weight="bold" />
              <span>{submitting ? "Booking Lodge..." : "Allocate & Create Booking"}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default BookingModal;
