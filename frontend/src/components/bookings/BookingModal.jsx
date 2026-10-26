import { useState, useEffect } from "react";
import { X, Bed, CalendarCheck, Buildings, WarningCircle, CheckCircle, Clock } from "@phosphor-icons/react";
import { getAllData, createData } from "../../api/api";
import { useAuth } from "../../hooks/useAuth";
import PropertyPickerModal from "./PropertyPickerModal";

/**
 * BookingModal Component
 * Modal dialog to allocate a partner property and create an accommodation booking
 * for a specific safari accommodation requirement.
 * 
 * Features:
 * - Dedicated wide PropertyPickerModal with tag filters and search
 * - Automatic stay duration calculation from requirement.numberOfNights
 * - Strict prevention and disabled state for previously declined lodges
 * - ACID compliance and strict check-in / check-out date checks
 */
const BookingModal = ({
  open,
  onClose,
  requirement,
  day,
  days = [],
  safari,
  onSuccess,
}) => {
  const { token, user } = useAuth();
  const normalizedRole = String(user?.role_name || user?.role || "").toUpperCase().replace(/^ROLE_/, "").trim();
  const isSalesPerson = ["SALES_PERSON", "SALE", "SALES", "SALESPERSON"].includes(normalizedRole);

  const [properties, setProperties] = useState([]);
  const [loadingProps, setLoadingProps] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  // Property Picker & Decline History
  const [pickerOpen, setPickerOpen] = useState(false);
  const [declinedPropertyIds, setDeclinedPropertyIds] = useState([]);

  // Form State
  const [selectedPropertyId, setSelectedPropertyId] = useState("");
  const [checkIn, setCheckIn] = useState("");
  const [checkOut, setCheckOut] = useState("");
  const [notes, setNotes] = useState("");

  const durationNights = parseInt(requirement?.numberOfNights, 10) || 1;

  // Resolve the itinerary day
  const resolvedDay =
    day ||
    (Array.isArray(days) && requirement?.itineraryDayId
      ? days.find((d) => String(d.id) === String(requirement.itineraryDayId))
      : null);

  const formatDateString = (val) => {
    if (!val) return "";
    if (Array.isArray(val) && val.length >= 3) {
      const [y, m, d] = val;
      return `${y}-${String(m).padStart(2, "0")}-${String(d).padStart(2, "0")}`;
    }
    if (typeof val === "string") {
      return val.includes("T") ? val.substring(0, 10) : val;
    }
    return String(val);
  };

  const computeCheckOutDate = (checkInStr, nights) => {
    if (!checkInStr || checkInStr.length < 10) return "";
    const [y, m, d] = checkInStr.substring(0, 10).split("-").map(Number);
    const dt = new Date(Date.UTC(y, m - 1, d));
    dt.setUTCDate(dt.getUTCDate() + nights);
    return dt.toISOString().substring(0, 10);
  };

  // Populate initial dates, load decline history, and load properties
  useEffect(() => {
    if (!open || isSalesPerson) return;

    setError(null);
    setSelectedPropertyId("");
    setNotes(requirement?.specialRequests || "");
    setPickerOpen(false);

    // Prioritized automatic check-in date calculation:
    let initialCheckIn = "";
    if (resolvedDay?.date) {
      initialCheckIn = formatDateString(resolvedDay.date);
    } else if (requirement?.itineraryDayDate) {
      initialCheckIn = formatDateString(requirement.itineraryDayDate);
    } else if (requirement?.itineraryDay?.date) {
      initialCheckIn = formatDateString(requirement.itineraryDay.date);
    } else if (requirement?.date) {
      initialCheckIn = formatDateString(requirement.date);
    } else if (safari?.startDate && resolvedDay?.dayNumber && resolvedDay.dayNumber > 1) {
      const sStart = formatDateString(safari.startDate);
      if (sStart && sStart.length === 10) {
        const [y, m, d] = sStart.split("-").map(Number);
        const dt = new Date(Date.UTC(y, m - 1, d));
        dt.setUTCDate(dt.getUTCDate() + (resolvedDay.dayNumber - 1));
        initialCheckIn = dt.toISOString().substring(0, 10);
      }
    } else if (safari?.startDate) {
      initialCheckIn = formatDateString(safari.startDate);
    }

    if (!initialCheckIn) {
      initialCheckIn = new Date().toISOString().substring(0, 10);
    } else if (initialCheckIn.length > 10) {
      initialCheckIn = initialCheckIn.substring(0, 10);
    }

    setCheckIn(initialCheckIn);
    setCheckOut(computeCheckOutDate(initialCheckIn, durationNights));

    // Fetch previously declined bookings for this specific requirement
    const fetchBookingHistory = async () => {
      if (!requirement?.id) return [];
      try {
        let bRes;
        try {
          bRes = await getAllData(`/accommodation-bookings/requirement/${requirement.id}`, token);
        } catch {
          bRes = await getAllData(`/api/v1/accommodation-bookings/requirement/${requirement.id}`, token);
        }
        const bList = Array.isArray(bRes) ? bRes : bRes?.content || bRes?.data || [];
        const declined = bList
          .filter((b) => String(b.status).toUpperCase() === "CANCELLED")
          .map((b) => String(b.propertyId));
        setDeclinedPropertyIds(declined);
        return declined;
      } catch (err) {
        console.warn("Could not load booking decline history:", err);
        return [];
      }
    };

    // Load available partner properties
    const fetchProperties = async () => {
      setLoadingProps(true);
      setError(null);
      try {
        const declined = await fetchBookingHistory();

        let res;
        try {
          res = await getAllData("/properties?size=100&sortBy=name&direction=asc", token);
        } catch {
          res = await getAllData("/api/v1/properties?size=100&sortBy=name&direction=asc", token);
        }
        const list = res?.content || res?.data || (Array.isArray(res) ? res : []);
        setProperties(Array.isArray(list) ? list : []);

        // Pick first available property that is NOT declined
        const availableFirst = list.find((p) => !(declined || []).includes(String(p.id)));
        if (availableFirst) {
          setSelectedPropertyId(availableFirst.id);
        } else if (list.length > 0) {
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
  }, [open, requirement, safari, token, durationNights]);

  if (!open || isSalesPerson) return null;

  const selectedProperty = properties.find((p) => String(p.id) === String(selectedPropertyId));
  const isSelectedDeclined = declinedPropertyIds.some(
    (dId) => String(dId) === String(selectedPropertyId)
  );

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!selectedPropertyId) {
      setError("Please select a partner property / lodge.");
      return;
    }

    if (isSelectedDeclined) {
      setError("This property previously declined this accommodation requirement and cannot be re-allocated. Please choose an alternative lodge.");
      return;
    }

    if (!checkIn || !checkOut) {
      setError("Check-in and Check-out dates are required.");
      return;
    }

    if (new Date(checkOut) <= new Date(checkIn)) {
      setError("Check-out date must be strictly after check-in date.");
      return;
    }

    // Safari boundary checks
    if (safari?.startDate && new Date(checkIn) < new Date(safari.startDate)) {
      setError(`Check-in date cannot be before safari start date (${safari.startDate}).`);
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
    <>
      <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-xs animate-in fade-in duration-150">
        <div className="w-full max-w-lg rounded-2xl bg-white shadow-xl border border-slate-200 overflow-hidden font-sans animate-in zoom-in-95 duration-150">
          {/* Header */}
          <div className="flex items-center justify-between border-b border-slate-100 px-6 py-4 bg-slate-50/50">
            <div className="flex items-center gap-2.5">
              <div className="w-9 h-9 rounded-xl bg-[#264624]/10 text-[#264624] flex items-center justify-center font-bold">
                <Bed size={20} weight="duotone" />
              </div>
              <div>
                <h3 className="font-bold text-slate-900 font-serif-title">
                  Allocate & Book Lodge
                </h3>
                <p className="text-xs text-slate-500">
                  {resolvedDay?.dayNumber ? `Day ${resolvedDay.dayNumber}` : ""}
                  {resolvedDay?.date ? ` (${resolvedDay.date})` : ""}
                  {resolvedDay ? " • " : ""}
                  {requirement?.destination || resolvedDay?.destination || "Safari Circuit"} ({requirement?.numberOfRooms || requirement?.numberOfrooms || 1} Room(s) • {durationNights} Night(s))
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
                <WarningCircle size={16} className="shrink-0 text-rose-600" />
                <span>{error}</span>
              </div>
            )}

            {/* Property Selector */}
            <div>
              <div className="flex items-center justify-between mb-1.5">
                <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider">
                  Partner Lodge / Accommodation <span className="text-rose-500">*</span>
                </label>
                <button
                  type="button"
                  onClick={() => setPickerOpen(true)}
                  className="text-xs font-bold text-[#264624] hover:underline flex items-center gap-1"
                >
                  Browse Lodges ({properties.length}) →
                </button>
              </div>

              {loadingProps ? (
                <div className="h-14 rounded-2xl bg-slate-100 animate-pulse flex items-center px-4 text-xs text-slate-400">
                  Loading available lodges...
                </div>
              ) : selectedProperty ? (
                <div
                  className={`rounded-2xl border p-4 transition ${
                    isSelectedDeclined
                      ? "border-rose-300 bg-rose-50/60"
                      : "border-slate-200 bg-slate-50/60 hover:bg-white hover:border-slate-300"
                  }`}
                >
                  {isSelectedDeclined && (
                    <div className="mb-2.5 p-2 rounded-xl bg-rose-100 text-rose-800 text-xs font-bold flex items-center gap-1.5">
                      <WarningCircle size={16} className="shrink-0 text-rose-600" />
                      <span>Property Unavailable: This lodge previously declined this booking!</span>
                    </div>
                  )}

                  <div className="flex items-start justify-between gap-3">
                    <div className="flex items-center gap-3">
                      <div className="w-10 h-10 rounded-xl bg-[#264624]/10 text-[#264624] flex items-center justify-center font-bold shrink-0">
                        <Buildings size={22} weight="duotone" />
                      </div>
                      <div>
                        <h4 className="font-bold text-slate-900 text-sm">{selectedProperty.name}</h4>
                        <p className="text-xs text-slate-500 mt-0.5">
                          {[selectedProperty.location, selectedProperty.region, selectedProperty.country]
                            .filter(Boolean)
                            .join(", ") || "Circuit Location"}
                        </p>
                      </div>
                    </div>

                    <button
                      type="button"
                      onClick={() => setPickerOpen(true)}
                      className="px-3 py-1.5 rounded-xl bg-white border border-slate-200 text-xs font-bold text-[#264624] hover:bg-slate-50 transition shadow-2xs shrink-0"
                    >
                      Change Lodge
                    </button>
                  </div>

                  <div className="mt-3 flex flex-wrap items-center gap-1.5 text-[11px]">
                    {selectedProperty.categoryName && (
                      <span className="px-2 py-0.5 rounded-md bg-slate-200/70 text-slate-700 font-medium">
                        {selectedProperty.categoryName}
                      </span>
                    )}
                    {selectedProperty.priceTierName && (
                      <span className="px-2 py-0.5 rounded-md bg-amber-50 border border-amber-200/70 text-amber-850 font-semibold">
                        {selectedProperty.priceTierName}
                      </span>
                    )}
                    {selectedProperty.tags?.slice(0, 3).map((t, i) => (
                      <span key={i} className="px-2 py-0.5 rounded-md bg-[#264624]/10 text-[#264624] text-[10px] font-bold">
                        #{t.name}
                      </span>
                    ))}
                  </div>
                </div>
              ) : (
                <button
                  type="button"
                  onClick={() => setPickerOpen(true)}
                  className="w-full flex items-center justify-between p-4 rounded-2xl border-2 border-dashed border-[#264624]/30 bg-[#264624]/5 hover:bg-[#264624]/10 transition group text-left"
                >
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-xl bg-[#264624] text-white flex items-center justify-center font-bold shadow-2xs">
                      <Buildings size={20} />
                    </div>
                    <div>
                      <p className="text-xs font-bold text-slate-900">Choose Partner Lodge / Accommodation</p>
                      <p className="text-[11px] text-slate-500">Filter by tags, category, and price tiers</p>
                    </div>
                  </div>
                  <span className="text-xs font-bold text-[#264624] group-hover:underline">
                    Browse Lodges →
                  </span>
                </button>
              )}
            </div>

            {/* Dates Row with Duration Tag */}
            <div className="space-y-1.5">
              <div className="flex items-center justify-between text-xs text-slate-500">
                <span className="font-bold text-slate-700 uppercase tracking-wider text-[11px]">
                  Reservation Schedule
                </span>
                <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-md bg-[#264624]/10 text-[#264624] font-bold text-[11px]">
                  <Clock size={13} weight="bold" />
                  {durationNights} Night{durationNights > 1 ? "s" : ""} Stay
                </span>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-[11px] font-semibold text-slate-600 mb-1">
                    Check-in Date <span className="text-emerald-700 font-bold text-[10px]">(Auto-set)</span>
                  </label>
                  <input
                    type="date"
                    value={checkIn}
                    readOnly
                    required
                    className="w-full rounded-xl border border-slate-200 bg-slate-50/80 px-3.5 py-2.5 text-xs text-slate-800 font-bold cursor-not-allowed outline-hidden shadow-2xs"
                  />
                </div>

                <div>
                  <label className="block text-[11px] font-semibold text-slate-600 mb-1">
                    Check-out Date <span className="text-emerald-700 font-bold text-[10px]">(+{durationNights} night{durationNights > 1 ? "s" : ""})</span>
                  </label>
                  <input
                    type="date"
                    value={checkOut}
                    readOnly
                    required
                    className="w-full rounded-xl border border-slate-200 bg-slate-50/80 px-3.5 py-2.5 text-xs text-slate-800 font-bold cursor-not-allowed outline-hidden shadow-2xs"
                  />
                </div>
              </div>
              <p className="text-[11px] text-slate-500 italic">
                Check-in and Check-out are automatically anchored to Day {resolvedDay?.dayNumber || ""} date and stay duration ({durationNights} night{durationNights > 1 ? "s" : ""}).
              </p>
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
                className="w-full rounded-xl border border-slate-200 px-3.5 py-2.5 text-xs text-slate-800 placeholder-slate-400 focus:border-[#264624] focus:ring-1 focus:ring-[#264624] outline-hidden transition shadow-2xs resize-none"
              />
            </div>

            {/* Actions */}
            <div className="flex items-center justify-between pt-3 border-t border-slate-100">
              <button
                type="button"
                onClick={onClose}
                className="px-4 py-2 text-xs font-semibold text-slate-600 hover:bg-slate-100 rounded-xl transition"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={submitting || !selectedPropertyId || isSelectedDeclined}
                className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl bg-[#264624] text-white text-xs font-bold hover:bg-[#1b331a] transition active:scale-95 shadow-xs disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <CalendarCheck size={16} weight="bold" />
                <span>
                  {submitting
                    ? "Booking Lodge..."
                    : isSelectedDeclined
                    ? "Lodge Unavailable"
                    : `Allocate & Book (${durationNights} Night${durationNights > 1 ? "s" : ""})`}
                </span>
              </button>
            </div>
          </form>
        </div>
      </div>

      {/* Wide Dedicated Property Picker Modal */}
      <PropertyPickerModal
        open={pickerOpen}
        onClose={() => setPickerOpen(false)}
        properties={properties}
        selectedPropertyId={selectedPropertyId}
        onSelectProperty={(prop) => {
          setSelectedPropertyId(prop.id);
          setError(null);
        }}
        declinedPropertyIds={declinedPropertyIds}
        requirement={requirement}
      />
    </>
  );
};

export default BookingModal;
