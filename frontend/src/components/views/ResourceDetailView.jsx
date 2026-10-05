import {
  ArrowLeft,
  ArrowsClockwise,
  PencilSimple,
  Compass,
  Users,
  Buildings,
  Info,
  CalendarBlank,
  Bed,
  Airplane,
  ShieldCheck,
} from "@phosphor-icons/react";
import SafariItineraryView from "./SafariItineraryView";
import ClientSubdataView from "./ClientSubdataView";
import PropertyVerificationView from "./PropertyVerificationView";
import BookingActionBanner from "./BookingActionBanner";
import OverviewCardsView from "./OverviewCardsView";

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

const getItemPrimaryTitle = (item, res) => {
  if (!item) return "Record Details";
  if (res === "accommodation-bookings") {
    return item.referenceNumber || item.propertyName || `Booking #${item.id}`;
  }
  if (res === "clients") {
    const fullName = `${item.firstName || ""} ${item.lastName || ""}`.trim();
    return fullName || item.email || `Client #${item.id}`;
  }
  if (res === "safaris") {
    return item.referenceNumber || item.title || `Safari #${item.id}`;
  }
  if (res === "properties") {
    return item.name || `Property #${item.id}`;
  }
  if (res === "users") {
    const fullName = `${item.firstName || ""} ${item.lastName || ""}`.trim();
    return fullName || item.email || `User #${item.id}`;
  }
  return item.name || item.title || `Record #${item.id}`;
};

const getItemSubtitle = (item, res) => {
  if (!item) return "";
  if (res === "accommodation-bookings") {
    const parts = [];
    if (item.propertyName) parts.push(item.propertyName);
    if (item.destination) parts.push(item.destination);
    if (item.checkIn) parts.push(`${item.checkIn} → ${item.checkOut || ""}`);
    if (item.safariReference) parts.push(`Safari: ${item.safariReference}`);
    return parts.join(" • ");
  }
  if (res === "safaris") {
    const parts = [];
    if (item.clientName) parts.push(`Client: ${item.clientName}`);
    if (item.salesPersonName) parts.push(`Sales: ${item.salesPersonName}`);
    if (item.startDate && item.endDate) parts.push(`${item.startDate} → ${item.endDate}`);
    return parts.join(" • ");
  }
  if (res === "clients") {
    const parts = [];
    if (item.email) parts.push(item.email);
    if (item.phone) parts.push(item.phone);
    if (item.nationality) parts.push(item.nationality);
    return parts.join(" • ");
  }
  if (res === "properties") {
    const parts = [];
    if (item.categoryName || item.categoryId) parts.push(item.categoryName || "Property");
    if (item.location) parts.push(item.location);
    if (item.contactEmail) parts.push(item.contactEmail);
    return parts.join(" • ");
  }
  if (res === "users") {
    const parts = [];
    if (item.role_name) parts.push(item.role_name);
    if (item.companyName) parts.push(item.companyName);
    if (item.email) parts.push(item.email);
    return parts.join(" • ");
  }
  return "";
};

const ResourceDetailView = ({
  title,
  resource,
  item,
  subData = { guests: [], flights: [], days: [], requirements: [] },
  activeTab = "overview",
  setActiveTab,
  fields = [],
  permissions = {},
  user,
  can,
  actionLoading = false,
  onBack,
  onRefresh,
  onEdit,
  // Safari actions
  onEditDay,
  onAddRequirement,
  onEditRequirement,
  onDeleteRequirement,
  onBookLodge,
  // Client actions
  onAddGuest,
  onAddFlight,
  // Property actions
  onVerifyProperty,
  // Booking actions
  onSendBooking,
  onConfirmBookingSuccess,
  onDeclineBooking,
}) => {
  const canUpdate = can("update", permissions);
  const canCreate = can("create", permissions);
  const canDelete = can("delete", permissions);

  const normalizedRole = String(user?.role_name || user?.role || "").toUpperCase().replace(/^ROLE_/, "").trim();
  const isSalesPerson = ["SALES_PERSON", "SALE", "SALES", "SALESPERSON"].includes(normalizedRole);

  const canVerify =
    can("verify", permissions) ||
    normalizedRole === "SUPER_ADMIN" ||
    normalizedRole === "ADMIN" ||
    normalizedRole === "RESERVATION_MANAGER";

  const canBookLodge =
    !isSalesPerson &&
    (normalizedRole === "ADMIN" ||
      normalizedRole === "SUPER_ADMIN" ||
      normalizedRole === "PLATFORM_ADMIN" ||
      normalizedRole === "RESERVATION_MANAGER" ||
      normalizedRole === "RM" ||
      normalizedRole === "RESERVATION" ||
      normalizedRole === "RESERVATIONS");

  return (
    <div className="space-y-5 animate-in fade-in duration-200 font-sans">
      {/* Top Bar: Back, Breadcrumbs, Quick Actions */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 rounded-2xl border border-slate-200/90 bg-white p-4 shadow-xs">
        <div className="flex flex-wrap items-center gap-3">
          <button
            type="button"
            onClick={onBack}
            className="inline-flex items-center gap-2 rounded-xl border border-slate-200 bg-slate-50/80 px-3.5 py-2 text-xs font-semibold text-slate-700 hover:border-slate-300 hover:bg-slate-100 hover:text-[#101B82] transition active:scale-95 group shadow-2xs"
          >
            <ArrowLeft size={16} weight="bold" className="transition-transform group-hover:-translate-x-0.5" />
            <span>Back to {title}</span>
          </button>
          <div className="h-5 w-px bg-slate-200 hidden sm:block" />
          <div className="flex items-center gap-1.5 text-xs text-slate-500 font-medium">
            <span className="text-slate-400">{title}</span>
            <span className="text-slate-300">/</span>
            <span className="text-slate-900 font-semibold max-w-[240px] truncate font-serif-title">
              {getItemPrimaryTitle(item, resource)}
            </span>
          </div>
        </div>

        <div className="flex items-center gap-2 self-end sm:self-auto">
          <button
            type="button"
            onClick={onRefresh}
            disabled={actionLoading}
            title="Refresh record details"
            className="inline-flex items-center justify-center rounded-xl border border-slate-200 bg-slate-50/70 p-2 text-slate-600 hover:bg-slate-100 hover:text-[#101B82] transition shadow-2xs"
          >
            <ArrowsClockwise size={17} weight="bold" className={actionLoading ? "animate-spin" : ""} />
          </button>
          {canUpdate && (
            <button
              type="button"
              onClick={onEdit}
              className="inline-flex items-center gap-1.5 rounded-xl bg-slate-900 px-3.5 py-2 text-xs font-semibold text-white hover:bg-slate-800 transition active:scale-95 shadow-2xs"
            >
              <PencilSimple size={15} weight="bold" />
              <span>Edit Details</span>
            </button>
          )}
        </div>
      </div>

      {/* Hero Header Card */}
      <div className="relative overflow-hidden rounded-2xl border border-slate-200/90 bg-white p-6 shadow-xs">
        <div className="absolute top-0 inset-x-0 h-1.5 bg-gradient-to-r from-[#101B82] via-indigo-600 to-blue-500" />

        <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-6 pt-1">
          <div className="flex items-start gap-4">
            <div className="flex h-14 w-14 shrink-0 items-center justify-center rounded-2xl bg-gradient-to-br from-[#101B82]/10 via-indigo-50 to-blue-50/50 border border-indigo-100 text-[#101B82] shadow-2xs">
              {resource === "safaris" ? (
                <Compass size={32} weight="duotone" />
              ) : resource === "clients" ? (
                <Users size={32} weight="duotone" />
              ) : resource === "properties" ? (
                <Buildings size={32} weight="duotone" />
              ) : (
                <Info size={32} weight="duotone" />
              )}
            </div>

            <div className="space-y-1">
              <div className="flex flex-wrap items-center gap-2.5">
                <h1 className="text-xl sm:text-2xl font-bold tracking-tight text-slate-900 font-serif-title">
                  {getItemPrimaryTitle(item, resource)}
                </h1>
                {(item.status || item.verificationStatus) && (
                  <span
                    className={`inline-flex items-center px-3 py-1 rounded-full text-xs font-semibold uppercase tracking-wider border shadow-2xs ${getStatusBadgeClass(
                      item.status || item.verificationStatus
                    )}`}
                  >
                    {item.status || item.verificationStatus}
                  </span>
                )}
              </div>

              {getItemSubtitle(item, resource) && (
                <p className="text-sm text-slate-500 font-medium">
                  {getItemSubtitle(item, resource)}
                </p>
              )}
            </div>
          </div>

          {/* Metric / Stat Chips */}
          <div className="flex flex-wrap items-center gap-2.5">
            {resource === "safaris" && (
              <>
                <div className="flex items-center gap-2 rounded-xl bg-slate-50/80 border border-slate-200/80 px-3.5 py-2 text-xs">
                  <CalendarBlank size={16} className="text-[#101B82]" />
                  <div>
                    <span className="block text-[10px] uppercase font-bold text-slate-400">Duration</span>
                    <span className="font-semibold text-slate-800 font-serif-title">
                      {subData?.days?.length || 0} Itinerary Days
                    </span>
                  </div>
                </div>

                <div className="flex items-center gap-2 rounded-xl bg-slate-50/80 border border-slate-200/80 px-3.5 py-2 text-xs">
                  <Users size={16} className="text-[#101B82]" />
                  <div>
                    <span className="block text-[10px] uppercase font-bold text-slate-400">Passengers</span>
                    <span className="font-semibold text-slate-800 font-serif-title">
                      {item.numberOfPassengers || "—"} Pax
                    </span>
                  </div>
                </div>

                <div className="flex items-center gap-2 rounded-xl bg-slate-50/80 border border-slate-200/80 px-3.5 py-2 text-xs">
                  <Bed size={16} className="text-[#101B82]" />
                  <div>
                    <span className="block text-[10px] uppercase font-bold text-slate-400">Accommodations</span>
                    <span className="font-semibold text-slate-800 font-serif-title">
                      {subData?.requirements?.length || 0} / {subData?.days?.length || 0} Configured
                    </span>
                  </div>
                </div>
              </>
            )}

            {resource === "clients" && (
              <>
                <div className="flex items-center gap-2 rounded-xl bg-slate-50/80 border border-slate-200/80 px-3.5 py-2 text-xs">
                  <Users size={16} className="text-[#101B82]" />
                  <div>
                    <span className="block text-[10px] uppercase font-bold text-slate-400">Guests</span>
                    <span className="font-semibold text-slate-800 font-serif-title">
                      {subData?.guests?.length || 0} Registered
                    </span>
                  </div>
                </div>

                <div className="flex items-center gap-2 rounded-xl bg-slate-50/80 border border-slate-200/80 px-3.5 py-2 text-xs">
                  <Airplane size={16} className="text-[#101B82]" />
                  <div>
                    <span className="block text-[10px] uppercase font-bold text-slate-400">Flights</span>
                    <span className="font-semibold text-slate-800 font-serif-title">
                      {subData?.flights?.length || 0} Scheduled
                    </span>
                  </div>
                </div>
              </>
            )}

            {resource === "properties" && (
              <div className="flex items-center gap-2 rounded-xl bg-slate-50/80 border border-slate-200/80 px-3.5 py-2 text-xs">
                <ShieldCheck
                  size={18}
                  className={item.verificationStatus === "VERIFIED" ? "text-emerald-600" : "text-amber-500"}
                />
                <div>
                  <span className="block text-[10px] uppercase font-bold text-slate-400">Compliance</span>
                  <span className="font-semibold text-slate-800 font-serif-title">
                    {item.verificationStatus || "PENDING"}
                  </span>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Tab Navigation */}
      <div className="flex items-center gap-2 border-b border-slate-200/80 pb-px">
        {resource === "safaris" && (
          <>
            <button
              type="button"
              onClick={() => setActiveTab("itinerary")}
              className={`inline-flex items-center gap-2 px-4 py-2.5 text-sm font-semibold border-b-2 transition-all duration-150 ${
                activeTab === "itinerary"
                  ? "border-[#101B82] text-[#101B82]"
                  : "border-transparent text-slate-500 hover:text-slate-900"
              }`}
            >
              <Compass size={18} />
              <span>Itinerary Schedule & Accommodations</span>
              <span
                className={`ml-1 px-2 py-0.5 rounded-full text-xs font-bold ${
                  activeTab === "itinerary"
                    ? "bg-[#101B82]/10 text-[#101B82]"
                    : "bg-slate-100 text-slate-600"
                }`}
              >
                {subData?.days?.length || 0}
              </span>
            </button>

            <button
              type="button"
              onClick={() => setActiveTab("overview")}
              className={`inline-flex items-center gap-2 px-4 py-2.5 text-sm font-semibold border-b-2 transition-all duration-150 ${
                activeTab === "overview"
                  ? "border-[#101B82] text-[#101B82]"
                  : "border-transparent text-slate-500 hover:text-slate-900"
              }`}
            >
              <Info size={18} />
              <span>Safari Specifications</span>
            </button>
          </>
        )}

        {resource === "clients" && (
          <>
            <button
              type="button"
              onClick={() => setActiveTab("guests")}
              className={`inline-flex items-center gap-2 px-4 py-2.5 text-sm font-semibold border-b-2 transition-all duration-150 ${
                activeTab === "guests"
                  ? "border-[#101B82] text-[#101B82]"
                  : "border-transparent text-slate-500 hover:text-slate-900"
              }`}
            >
              <Users size={18} />
              <span>Registered Guests</span>
              <span
                className={`ml-1 px-2 py-0.5 rounded-full text-xs font-bold ${
                  activeTab === "guests"
                    ? "bg-[#101B82]/10 text-[#101B82]"
                    : "bg-slate-100 text-slate-600"
                }`}
              >
                {subData?.guests?.length || 0}
              </span>
            </button>

            <button
              type="button"
              onClick={() => setActiveTab("flights")}
              className={`inline-flex items-center gap-2 px-4 py-2.5 text-sm font-semibold border-b-2 transition-all duration-150 ${
                activeTab === "flights"
                  ? "border-[#101B82] text-[#101B82]"
                  : "border-transparent text-slate-500 hover:text-slate-900"
              }`}
            >
              <Airplane size={18} />
              <span>Flight Schedules</span>
              <span
                className={`ml-1 px-2 py-0.5 rounded-full text-xs font-bold ${
                  activeTab === "flights"
                    ? "bg-[#101B82]/10 text-[#101B82]"
                    : "bg-slate-100 text-slate-600"
                }`}
              >
                {subData?.flights?.length || 0}
              </span>
            </button>

            <button
              type="button"
              onClick={() => setActiveTab("overview")}
              className={`inline-flex items-center gap-2 px-4 py-2.5 text-sm font-semibold border-b-2 transition-all duration-150 ${
                activeTab === "overview"
                  ? "border-[#101B82] text-[#101B82]"
                  : "border-transparent text-slate-500 hover:text-slate-900"
              }`}
            >
              <Info size={18} />
              <span>Client Profile</span>
            </button>
          </>
        )}

        {resource !== "safaris" && resource !== "clients" && (
          <button
            type="button"
            className="inline-flex items-center gap-2 px-4 py-2.5 text-sm font-semibold border-b-2 border-[#101B82] text-[#101B82]"
          >
            <Info size={18} />
            <span>General Overview</span>
          </button>
        )}
      </div>

      {/* Tab Content Display */}
      {resource === "safaris" && activeTab === "itinerary" && (
        <SafariItineraryView
          days={subData?.days || []}
          requirements={subData?.requirements || []}
          bookings={subData?.bookings || []}
          canUpdate={canUpdate}
          canCreate={canCreate}
          canDelete={canDelete}
          canBook={canBookLodge}
          onEditDay={onEditDay}
          onAddRequirement={onAddRequirement}
          onEditRequirement={onEditRequirement}
          onDeleteRequirement={onDeleteRequirement}
          onBookLodge={onBookLodge}
        />
      )}

      {(resource === "clients" || resource === "safaris") && (activeTab === "guests" || activeTab === "flights") && (
        <ClientSubdataView
          activeTab={activeTab}
          client={resource === "safaris" ? (item.client || item) : item}
          guests={subData?.guests || []}
          flights={subData?.flights || []}
          canCreate={canCreate}
          canManage={!isSalesPerson || canUpdate || canCreate}
          onAddGuest={onAddGuest}
          onAddFlight={onAddFlight}
        />
      )}

      {(activeTab === "overview" ||
        (resource !== "safaris" && resource !== "clients")) && (
        <div className="space-y-5">
          {resource === "properties" && (
            <PropertyVerificationView
              property={item}
              canVerify={canVerify}
              loading={actionLoading}
              onVerify={onVerifyProperty}
            />
          )}

          {resource === "accommodation-bookings" && (
            <BookingActionBanner
              booking={item}
              canManage={!isSalesPerson && (canUpdate || canCreate || normalizedRole === "ADMIN" || normalizedRole === "RESERVATION_MANAGER" || normalizedRole === "SUPER_ADMIN")}
              loading={actionLoading}
              onSend={onSendBooking}
              onConfirmSuccess={onConfirmBookingSuccess}
              onDecline={onDeclineBooking}
            />
          )}

          <OverviewCardsView fields={fields} item={item} />
        </div>
      )}
    </div>
  );
};

export default ResourceDetailView;
