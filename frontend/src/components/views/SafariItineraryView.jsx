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
  XCircle,
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
  return "bg-[#264624]/10 text-[#264624] border-[#264624]/20";
};

const formatDisplayDate = (val) => {
  if (!val) return "Date not set";
  if (Array.isArray(val) && val.length >= 3) {
    const [y, m, d] = val;
    return `${y}-${String(m).padStart(2, "0")}-${String(d).padStart(2, "0")}`;
  }
  if (typeof val === "string") {
    return val.includes("T") ? val.substring(0, 10) : val;
  }
  return String(val);
};

const SafariItineraryView = ({
  days = [],
  requirements = [],
  bookings = [],
  safariStatus = "DRAFT",
  canUpdate = false,
  canCreate = false,
  canDelete = false,
  canBook = false,
  actionLoading = false,
  onRegenerateDays,
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
    const dayDate = formatDisplayDate(day?.date);
    return (bookings || []).find((b) => {
      if (req && b.requirementId && String(b.requirementId) === String(req.id)) {
        return true;
      }
      if (b.checkIn && dayDate && (b.checkIn === dayDate || b.checkIn === day?.date)) {
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
      <div className="rounded-2xl border border-slate-200/90 bg-white p-12 text-center shadow-xs space-y-4">
        <Compass size={44} className="mx-auto text-slate-300" />
        <div>
          <h3 className="text-base font-bold text-slate-800 font-serif-title">
            No Itinerary Days Generated
          </h3>
          <p className="text-xs text-slate-500 mt-1 max-w-sm mx-auto">
            The itinerary schedule for this safari has not been generated or synchronized yet.
          </p>
        </div>
        {canUpdate && onRegenerateDays && (
          <button
            type="button"
            onClick={onRegenerateDays}
            disabled={actionLoading}
            className="inline-flex items-center gap-2 rounded-xl bg-[#264624] px-5 py-2.5 text-xs font-bold text-white hover:bg-[#1b331a] transition active:scale-95 shadow-md disabled:opacity-50"
          >
            <ArrowsClockwise size={16} className={actionLoading ? "animate-spin" : ""} weight="bold" />
            <span>{actionLoading ? "Generating Days..." : "Generate / Retry Days"}</span>
          </button>
        )}
      </div>
    );
  }

  // Booking & Destination Calculations across all days
  const totalDays = days.length;
  const configuredDestinationsCount = days.filter(
    (d) => d.destination && !d.destination.toLowerCase().includes("not set") && !d.destination.toLowerCase().includes("pending")
  ).length;
  const allDestinationsConfigured = totalDays > 0 && configuredDestinationsCount === totalDays;

  const confirmedCount = days.filter(isDayConfirmed).length;
  const allBookingsConfirmed = totalDays > 0 && confirmedCount === totalDays;
  const progressPercent = totalDays > 0 ? Math.round((confirmedCount / totalDays) * 100) : 0;
  const pendingCount = Math.max(totalDays - confirmedCount, 0);

  // Status Badge Class & Label
  const effectiveStatus = (safariStatus || "").toUpperCase();
  const getSafariStatusBadge = () => {
    if (effectiveStatus === "COMPLETED" || allBookingsConfirmed) {
      return {
        label: "COMPLETED",
        desc: "All Day Bookings Confirmed",
        cls: "bg-emerald-50 text-emerald-800 border-emerald-300",
      };
    }
    if (effectiveStatus === "CONFIRMED" || allDestinationsConfigured) {
      return {
        label: "CONFIRMED",
        desc: "All Destinations Configured",
        cls: "bg-[#264624]/10 text-[#264624] border-[#264624]/30",
      };
    }
    return {
      label: "DRAFT",
      desc: `${totalDays - configuredDestinationsCount} Destinations Pending`,
      cls: "bg-amber-50 text-amber-800 border-amber-300",
    };
  };

  const statusBadge = getSafariStatusBadge();

  return (
    <div className="space-y-5 animate-in fade-in duration-200 font-sans">
      {/* 1. Executive Safari Booking Progress Tracker Banner */}
      <div className="rounded-2xl border border-slate-200/90 bg-white p-5 shadow-xs space-y-3.5">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-[#264624]/10 text-[#264624] flex items-center justify-center font-bold">
              <CalendarCheck size={22} weight="duotone" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h3 className="font-bold text-slate-900 text-sm sm:text-base font-serif-title">
                  Safari Route & Accommodation Tracker
                </h3>
                <span className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold border ${statusBadge.cls}`}>
                  <span>{statusBadge.label}</span>
                </span>
              </div>
              <p className="text-xs text-slate-500 mt-0.5">
                {statusBadge.desc} • {confirmedCount} of {totalDays} room nights confirmed
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2">
            {canUpdate && onRegenerateDays && (
              <button
                type="button"
                onClick={onRegenerateDays}
                disabled={actionLoading}
                title="Sync / Regenerate Itinerary Days from Start to End Date"
                className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-bold bg-slate-50 text-slate-700 border border-slate-200 hover:bg-[#264624]/10 hover:text-[#264624] hover:border-[#264624]/30 transition active:scale-95 disabled:opacity-50 shadow-2xs"
              >
                <ArrowsClockwise size={14} className={actionLoading ? "animate-spin" : ""} weight="bold" />
                <span>{actionLoading ? "Syncing..." : "Retry / Sync Days"}</span>
              </button>
            )}

            <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold bg-emerald-50 text-emerald-800 border border-emerald-200">
              <CheckCircle size={14} weight="bold" className="text-emerald-600" />
              <span>{confirmedCount}/{totalDays} Booked</span>
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
            <span className="font-bold text-[#264624]">{progressPercent}% Completed</span>
          </div>
          <div className="w-full h-2.5 rounded-full bg-slate-100 overflow-hidden">
            <div
              className="h-full rounded-full bg-gradient-to-r from-[#264624] via-[#7A5229] to-emerald-600 transition-all duration-300"
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

        const isBookingDeclined =
          booking &&
          (String(booking.status || "").toUpperCase() === "CANCELLED" ||
           String(booking.status || "").toUpperCase() === "DECLINED");

        const isManualReviewNeeded =
          booking &&
          booking.notes &&
          booking.notes.includes("[MANUAL REVIEW REQUIRED]");

        const isBookingPending =
          !isRequirementConfirmed &&
          !isBookingDeclined &&
          booking &&
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
                <span className="inline-flex items-center px-3 py-1 rounded-lg bg-[#264624] text-white text-xs font-bold shadow-2xs">
                  Day {day.dayNumber}
                </span>

                <div className="flex items-center gap-1.5 text-xs text-slate-500 font-medium">
                  <CalendarBlank size={15} className="text-slate-400" />
                  <span>{formatDisplayDate(day.date)}</span>
                </div>

                <div className="flex items-center gap-1.5 text-sm font-bold text-slate-900 font-serif-title">
                  <MapPin size={16} className={isDestinationConfigured ? "text-[#264624]" : "text-amber-500"} weight="fill" />
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
                      ? "bg-gradient-to-r from-[#264624] to-[#345c31] text-white hover:from-[#1b331a] hover:to-[#264624]"
                      : "border border-slate-200 bg-slate-50/70 text-slate-700 hover:bg-slate-100 hover:text-[#264624]"
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
                <div className="rounded-xl border border-[#264624]/20 bg-gradient-to-br from-[#264624]/5 via-white to-stone-50/60 p-4 shadow-2xs space-y-3">
                  <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 border-b border-[#264624]/15 pb-2.5">
                    <div className="flex items-center gap-2">
                      <div className="flex h-7 w-7 items-center justify-center rounded-lg bg-[#264624]/10 text-[#264624]">
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
                          className="inline-flex items-center gap-1 text-xs font-semibold text-[#264624] hover:text-[#1b331a] hover:underline"
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
                              className="inline-flex items-center px-1.5 py-0.5 rounded bg-[#7A5229]/10 border border-[#7A5229]/20 text-[10px] font-bold text-[#7A5229]"
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

                  {/* Lodge Booking Declined Alert Banner */}
                  {isBookingDeclined && !isRequirementConfirmed && (
                    <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 p-3 rounded-xl bg-rose-50 border border-rose-200 text-rose-800 text-xs animate-in fade-in">
                      <div className="flex items-center gap-2">
                        <XCircle size={18} weight="bold" className="text-rose-600 shrink-0" />
                        <div>
                          <p className="font-bold text-rose-900">Lodge Booking Declined</p>
                          <p className="text-[11px] text-rose-700">
                            {booking?.propertyName ? `Lodge '${booking.propertyName}'` : "Property"} was unavailable or declined the request. An alternative property allocation is required.
                          </p>
                        </div>
                      </div>
                      {!isSalesPerson && canBook && (
                        <button
                          type="button"
                          onClick={() => onBookLodge && onBookLodge(req, day)}
                          className="self-start sm:self-auto inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-rose-700 text-white text-xs font-bold hover:bg-rose-800 transition active:scale-95 shadow-2xs shrink-0"
                        >
                          <CalendarCheck size={14} weight="bold" />
                          <span>Re-allocate Alternative Lodge</span>
                        </button>
                      )}
                    </div>
                  )}

                  {/* Manual Review Needed Alert Banner */}
                  {isManualReviewNeeded && !isRequirementConfirmed && !isBookingDeclined && (
                    <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 p-3 rounded-xl bg-amber-50 border border-amber-200 text-amber-800 text-xs animate-in fade-in">
                      <div className="flex items-center gap-2">
                        <WarningCircle size={18} weight="bold" className="text-amber-600 shrink-0" />
                        <div>
                          <p className="font-bold text-amber-900">Action Needed: Verify Lodge Response</p>
                          <p className="text-[11px] text-amber-700">
                            Inbound email from lodge requires human review. Please check the response and confirm or decline manually.
                          </p>
                        </div>
                      </div>
                      {!isSalesPerson && canBook && (
                        <button
                          type="button"
                          onClick={() => onBookLodge && onBookLodge(req, day)}
                          className="self-start sm:self-auto inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-amber-700 text-white text-xs font-bold hover:bg-amber-800 transition active:scale-95 shadow-2xs shrink-0"
                        >
                          <span>Review & Confirm Manually</span>
                        </button>
                      )}
                    </div>
                  )}

                  {/* Booking Trigger / Status Bar */}
                  <div className="flex flex-wrap items-center justify-between gap-2 pt-2 border-t border-[#264624]/15">
                    <div className="text-[11px] text-slate-500">
                      Requirement Status:{" "}
                      <span className="font-semibold text-slate-800">
                        {isRequirementConfirmed
                          ? "CONFIRMED"
                          : isBookingDeclined
                          ? "DECLINED — NEEDS RE-BOOKING"
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
                        className={`inline-flex items-center gap-1.5 px-3.5 py-1.5 rounded-xl text-white text-xs font-bold transition active:scale-95 shadow-2xs ${
                          isBookingDeclined
                            ? "bg-rose-700 hover:bg-rose-800"
                            : "bg-[#264624] hover:bg-[#1b331a]"
                        }`}
                      >
                        <CalendarCheck size={14} weight="bold" />
                        <span>{isBookingDeclined ? "Re-allocate Alternative Lodge" : "Allocate & Book Lodge"}</span>
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
                      className="inline-flex items-center gap-1.5 self-start sm:self-auto rounded-xl bg-gradient-to-r from-[#264624] to-[#345c31] px-3.5 py-2 text-xs font-semibold text-white hover:from-[#1b331a] hover:to-[#264624] transition active:scale-95 shadow-2xs"
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
