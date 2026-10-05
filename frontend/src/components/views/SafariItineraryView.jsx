import { 
  Compass, 
  CalendarBlank, 
  MapPin, 
  PencilSimple, 
  Bed, 
  Plus, 
  Trash, 
  CalendarCheck,
  CheckCircle,
  WarningCircle,
  ArrowsClockwise
} from "@phosphor-icons/react";
import { useAuth } from "../../hooks/useAuth";

const getStatusBadgeClass = (status) => {
  const s = String(status || "").toUpperCase();
  if (s === "CONFIRMED" || s === "ACTIVE" || s === "VERIFIED" || s === "COMPLETED") {
    return "bg-emerald-50 text-emerald-700 border-emerald-200/90";
  }
  if (s === "PENDING" || s === "DRAFT") {
    return "bg-amber-50 text-amber-700 border-amber-200/90";
  }
  if (s === "CANCELLED" || s === "INACTIVE" || s === "REJECTED") {
    return "bg-rose-50 text-rose-700 border-rose-200/90";
  }
  return "bg-indigo-50 text-[#101B82] border-indigo-200/90";
};

const SafariItineraryView = ({
  days = [],
  requirements = [],
  bookings = [],
  canUpdate = false,
  canCreate = false,
  canDelete = false,
  canBook = false,
  onEditDay,
  onAddRequirement,
  onEditRequirement,
  onDeleteRequirement,
  onBookLodge,
}) => {
  const { user } = useAuth();
  const normalizedRole = String(user?.role_name || user?.role || "").toUpperCase().replace(/^ROLE_/, "").trim();
  const isSalesPerson = ["SALES_PERSON", "SALE", "SALES", "SALESPERSON"].includes(normalizedRole);

  // Helper to match booking for a day/requirement
  const getBookingForDay = (day, req) => {
    return (bookings || []).find((b) => {
      if (req && b.requirementId && String(b.requirementId) === String(req.id)) {
        return true;
      }
      if (b.checkIn && day.date && b.checkIn === day.date) {
        return true;
      }
      return false;
    });
  };

  const isDayConfirmed = (day) => {
    const req = requirements.find((r) => String(r.itineraryDayId) === String(day.id));
    const b = getBookingForDay(day, req);
    const bookingConfirmed = b && String(b.status || "").toUpperCase() === "CONFIRMED";
    const reqConfirmed = req && (
      String(req.status || "").toUpperCase() === "COMPLETED" ||
      String(req.status || "").toUpperCase() === "CONFIRMED"
    );
    return bookingConfirmed || reqConfirmed;
  };

  if (days.length === 0) {
    return (
      <div className="rounded-2xl border border-slate-200/90 bg-white p-12 text-center shadow-xs">
        <Compass size={40} className="mx-auto text-slate-300 mb-3" />
        <h3 className="text-base font-bold text-slate-800 font-serif-title">
          No Itinerary Days Generated
        </h3>
        <p className="text-xs text-slate-500 mt-1 max-w-sm mx-auto">
          The itinerary schedule for this safari has not been configured yet.
        </p>
      </div>
    );
  }

  // Booking Progress Calculations across all days
  const totalDays = days.length;
  const confirmedCount = days.filter(isDayConfirmed).length;
  const progressPercent = totalDays > 0 ? Math.round((confirmedCount / totalDays) * 100) : 0;
  const pendingCount = Math.max(totalDays - confirmedCount, 0);

  return (
    <div className="space-y-5 animate-in fade-in duration-200 font-sans">
      {/* 1. Executive Safari Booking Progress Tracker Banner */}
      <div className="rounded-2xl border border-slate-200/90 bg-white p-5 shadow-xs space-y-3.5">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-indigo-50 text-[#101B82] flex items-center justify-center font-bold">
              <CalendarCheck size={22} weight="duotone" />
            </div>
            <div>
              <h3 className="font-bold text-slate-900 text-sm sm:text-base font-serif-title">
                Safari Accommodation & Route Booking Tracker
              </h3>
              <p className="text-xs text-slate-500">
                Tracking room confirmation status across {totalDays} scheduled itinerary days
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold bg-emerald-50 text-emerald-800 border border-emerald-200">
              <CheckCircle size={14} weight="bold" className="text-emerald-600" />
              <span>{confirmedCount} of {totalDays} Confirmed</span>
            </span>

            {pendingCount > 0 && (
              <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold bg-amber-50 text-amber-800 border border-amber-200">
                <WarningCircle size={14} weight="bold" className="text-amber-600" />
                <span>{pendingCount} Pending</span>
              </span>
            )}
          </div>
        </div>

        {/* Visual Progress Bar */}
        <div className="space-y-1.5">
          <div className="flex justify-between text-xs text-slate-600 font-medium">
            <span>Overall Booking Status</span>
            <span className="font-bold text-[#101B82]">{progressPercent}% Completed</span>
          </div>
          <div className="w-full h-2.5 rounded-full bg-slate-100 overflow-hidden">
            <div
              className="h-full rounded-full bg-gradient-to-r from-[#101B82] via-indigo-600 to-emerald-500 transition-all duration-300"
              style={{ width: `${progressPercent}%` }}
            />
          </div>
        </div>
      </div>

      {/* 2. List of Scheduled Itinerary Days */}
      {days.map((day) => {
        const req = requirements.find((r) => String(r.itineraryDayId) === String(day.id));
        const booking = getBookingForDay(day, req);
        const isDestinationConfigured = day.destination && day.destination !== "Destination not set";

        const isBookingConfirmed = booking && String(booking.status || "").toUpperCase() === "CONFIRMED";
        const isRequirementConfirmed =
          isBookingConfirmed ||
          (req && (
            String(req.status || "").toUpperCase() === "COMPLETED" ||
            String(req.status || "").toUpperCase() === "CONFIRMED"
          ));

        const isBookingPending =
          !isRequirementConfirmed &&
          booking &&
          String(booking.status || "").toUpperCase() !== "CANCELLED" &&
          (
            String(booking.status || "").toUpperCase() === "PROVISIONAL" ||
            String(booking.status || "").toUpperCase() === "DRAFT" ||
            (req && (
              String(req.status || "").toUpperCase() === "IN_PROGRESS" ||
              String(req.status || "").toUpperCase() === "AWAITING_RESPONSE"
            ))
          );

        return (
          <div
            key={day.id}
            className="rounded-2xl border border-slate-200/90 bg-white p-5 shadow-xs transition-all hover:border-slate-300 space-y-4"
          >
            {/* Day Header Row */}
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-b border-slate-100 pb-3.5">
              <div className="flex flex-wrap items-center gap-3">
                <span className="inline-flex items-center px-3 py-1 rounded-lg bg-[#101B82] text-white text-xs font-bold shadow-2xs">
                  Day {day.dayNumber}
                </span>

                <div className="flex items-center gap-1.5 text-xs text-slate-500 font-medium">
                  <CalendarBlank size={15} className="text-slate-400" />
                  <span>{day.date || "Date not set"}</span>
                </div>

                <div className="flex items-center gap-1.5 text-sm font-bold text-slate-900 font-serif-title">
                  <MapPin size={16} className={isDestinationConfigured ? "text-[#101B82]" : "text-amber-500"} weight="fill" />
                  <span className={isDestinationConfigured ? "text-slate-900" : "text-amber-700 italic"}>
                    {day.destination || "Destination pending configuration"}
                  </span>
                </div>
              </div>

              {canUpdate && (
                <button
                  type="button"
                  onClick={() => onEditDay(day)}
                  className={`inline-flex items-center gap-1.5 self-start sm:self-auto rounded-xl px-3.5 py-1.5 text-xs font-bold transition active:scale-95 shadow-2xs ${
                    !isDestinationConfigured
                      ? "bg-gradient-to-r from-[#101B82] to-indigo-600 text-white hover:from-[#0d176f] hover:to-indigo-700"
                      : "border border-slate-200 bg-slate-50/70 text-slate-700 hover:bg-slate-100 hover:text-[#101B82]"
                  }`}
                >
                  <PencilSimple size={13} weight="bold" />
                  <span>
                    {!isDestinationConfigured ? "Configure Day Itinerary" : "Edit Day Details"}
                  </span>
                </button>
              )}
            </div>

            {/* Day Notes & Activities */}
            {day.notes && (
              <div className="rounded-xl bg-slate-50/70 border border-slate-100 p-3 text-xs text-slate-600 leading-relaxed">
                <span className="font-semibold text-slate-700">Scheduled Activities & Route: </span>
                <span>{day.notes}</span>
              </div>
            )}

            {/* Accommodation Requirement Card */}
            <div>
              {req ? (
                <div className="rounded-xl border border-indigo-100/90 bg-gradient-to-br from-indigo-50/40 via-white to-slate-50/60 p-4 shadow-2xs space-y-3">
                  <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 border-b border-indigo-100/60 pb-2.5">
                    <div className="flex items-center gap-2">
                      <div className="flex h-7 w-7 items-center justify-center rounded-lg bg-[#101B82]/10 text-[#101B82]">
                        <Bed size={16} weight="duotone" />
                      </div>
                      <span className="text-xs font-bold text-slate-900 font-serif-title">
                        Accommodation Requirement
                      </span>
                      <span
                        className={`inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold uppercase tracking-wider border ${getStatusBadgeClass(
                          isRequirementConfirmed
                            ? "COMPLETED"
                            : isBookingPending
                            ? (booking?.status || "IN_PROGRESS")
                            : req.status
                        )}`}
                      >
                        {isRequirementConfirmed
                          ? "CONFIRMED"
                          : isBookingPending
                          ? (booking?.status ? String(booking.status).toUpperCase() : (req.status || "IN_PROGRESS"))
                          : (req.status || "PENDING")}
                      </span>
                    </div>

                    <div className="flex items-center gap-2 self-end sm:self-auto">
                      {canUpdate && (
                        <button
                          type="button"
                          onClick={() => onEditRequirement(day, req)}
                          className="inline-flex items-center gap-1 text-xs font-semibold text-[#101B82] hover:text-[#0c145e] hover:underline"
                        >
                          <PencilSimple size={13} />
                          <span>Edit Requirement</span>
                        </button>
                      )}
                      {canDelete && (
                        <button
                          type="button"
                          onClick={() => onDeleteRequirement(req.id)}
                          className="inline-flex items-center gap-1 text-xs font-semibold text-rose-600 hover:text-rose-700 hover:underline ml-2"
                        >
                          <Trash size={13} />
                          <span>Delete</span>
                        </button>
                      )}
                    </div>
                  </div>

                  {/* Requirement Metrics */}
                  <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 text-xs">
                    <div className="rounded-lg bg-white/80 p-2.5 border border-slate-100">
                      <span className="block text-[10px] font-bold uppercase tracking-wider text-slate-400">
                        Category
                      </span>
                      <span className="mt-0.5 block font-semibold text-slate-900">
                        {req.categoryName || "N/A"}
                      </span>
                    </div>

                    <div className="rounded-lg bg-white/80 p-2.5 border border-slate-100">
                      <span className="block text-[10px] font-bold uppercase tracking-wider text-slate-400">
                        Price Tier
                      </span>
                      <span className="mt-0.5 block font-semibold text-slate-900">
                        {req.priceTierName || "N/A"}
                      </span>
                    </div>

                    <div className="rounded-lg bg-white/80 p-2.5 border border-slate-100">
                      <span className="block text-[10px] font-bold uppercase tracking-wider text-slate-400">
                        Rooms Requested
                      </span>
                      <span className="mt-0.5 block font-semibold text-slate-900">
                        {req.numberOfrooms} Room(s)
                      </span>
                    </div>

                    <div className="rounded-lg bg-white/80 p-2.5 border border-slate-100">
                      <span className="block text-[10px] font-bold uppercase tracking-wider text-slate-400">
                        Room Types
                      </span>
                      <div className="mt-1 flex flex-wrap gap-1">
                        {req.roomRequirements?.length ? (
                          req.roomRequirements.map((r, i) => (
                            <span
                              key={i}
                              className="inline-flex items-center px-1.5 py-0.5 rounded bg-indigo-50 border border-indigo-200/60 text-[10px] font-bold text-[#101B82]"
                            >
                              {r.roomType}: {r.quantity}
                            </span>
                          ))
                        ) : (
                          <span className="font-semibold text-slate-800">None</span>
                        )}
                      </div>
                    </div>
                  </div>

                  {(req.roomPreferences || req.specialRequests) && (
                    <div className="rounded-lg bg-white/90 p-2.5 border border-slate-100 text-xs text-slate-600 space-y-1">
                      {req.roomPreferences && (
                        <p>
                          <strong className="text-slate-700">Preferences:</strong> {req.roomPreferences}
                        </p>
                      )}
                      {req.specialRequests && (
                        <p>
                          <strong className="text-slate-700">Special Requests:</strong> {req.specialRequests}
                        </p>
                      )}
                    </div>
                  )}

                  {/* Booking Trigger / Status Bar */}
                  <div className="flex flex-wrap items-center justify-between gap-2 pt-2 border-t border-indigo-100/60">
                    <div className="text-[11px] text-slate-500">
                      Requirement Status:{" "}
                      <span className="font-semibold text-slate-800">
                        {isRequirementConfirmed
                          ? "CONFIRMED"
                          : isBookingPending
                          ? (booking?.status ? String(booking.status).toUpperCase() : (req?.status || "IN_PROGRESS"))
                          : (req?.status || "PENDING")}
                      </span>
                    </div>

                    {/* ONLY show Allocate & Book button if user has permission (NOT Sales Person) AND requirement is NOT already confirmed or pending */}
                    {!isSalesPerson && canBook && !isRequirementConfirmed && !isBookingPending && (
                      <button
                        type="button"
                        onClick={() => onBookLodge && onBookLodge(req, day)}
                        className="inline-flex items-center gap-1.5 px-3.5 py-1.5 rounded-xl bg-[#101B82] text-white text-xs font-bold hover:bg-[#0c145e] transition active:scale-95 shadow-2xs"
                      >
                        <CalendarCheck size={14} weight="bold" />
                        <span>Allocate & Book Lodge</span>
                      </button>
                    )}

                    {/* Pending / In-progress booking */}
                    {isBookingPending && (
                      <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-xl bg-amber-50 border border-amber-200 text-amber-800 text-xs font-bold">
                        <ArrowsClockwise size={14} weight="bold" className="text-amber-600 animate-spin" />
                        <span>
                          {String(booking?.status || "").toUpperCase() === "PROVISIONAL"
                            ? `Inquiry Dispatched${booking.propertyName ? ` (${booking.propertyName})` : ""}`
                            : `Draft Booking Allocated${booking?.propertyName ? ` (${booking.propertyName})` : ""}`}
                        </span>
                      </span>
                    )}

                    {/* Confirmed booking */}
                    {isRequirementConfirmed && (
                      <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-xl bg-emerald-50 border border-emerald-200 text-emerald-800 text-xs font-bold">
                        <CheckCircle size={15} weight="bold" className="text-emerald-600" />
                        <span>
                          Lodge Booked & Confirmed
                          {booking?.propertyName ? ` — ${booking.propertyName}` : ""}
                          {booking?.confirmationNumber ? ` (#${booking.confirmationNumber})` : ""}
                        </span>
                      </span>
                    )}
                  </div>
                </div>
              ) : (
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 rounded-xl border border-dashed border-slate-200 bg-slate-50/50 p-3.5">
                  <div className="flex items-center gap-2 text-xs text-slate-500">
                    <Bed size={16} className="text-slate-400" />
                    <span>No accommodation requirement configured for Day {day.dayNumber}.</span>
                  </div>

                  {canCreate && (
                    <button
                      type="button"
                      onClick={() => onAddRequirement(day)}
                      className="inline-flex items-center gap-1.5 self-start sm:self-auto rounded-xl bg-gradient-to-r from-[#101B82] to-indigo-600 px-3.5 py-2 text-xs font-semibold text-white hover:from-[#0d176f] hover:to-indigo-700 transition active:scale-95 shadow-2xs"
                    >
                      <Plus size={14} weight="bold" />
                      <span>Add Accommodation Requirement</span>
                    </button>
                  )}
                </div>
              )}
            </div>
          </div>
        );
      })}
    </div>
  );
};

export default SafariItineraryView;
