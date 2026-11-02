import { useState, useMemo } from "react";
import {
  Compass,
  MagnifyingGlass,
  FunnelSimple,
  ArrowsDownUp,
  CaretRight,
  CaretLeft,
  CaretDown,
  Plus,
  Users,
  Clock,
  CheckCircle,
  WarningCircle,
  X,
  SquaresFour,
  Rows,
  PencilSimple,
  Trash,
  Eye,
  AirplaneTakeoff,
  ArrowRight
} from "@phosphor-icons/react";
import { useAuth } from "../../hooks/useAuth";

// Formats start and end dates into "08 Aug 26 → 14 Aug 26 · 7 Days"
const formatDateRange = (start, end) => {
  const parse = (val) => {
    if (!val) return null;
    if (Array.isArray(val) && val.length >= 3) {
      return new Date(Date.UTC(val[0], val[1] - 1, val[2]));
    }
    if (typeof val === "string") {
      const parts = val.substring(0, 10).split("-").map(Number);
      if (parts.length === 3) {
        return new Date(Date.UTC(parts[0], parts[1] - 1, parts[2]));
      }
    }
    const d = new Date(val);
    return isNaN(d.getTime()) ? null : d;
  };

  const d1 = parse(start);
  const d2 = parse(end);

  const formatShort = (d) => {
    if (!d || isNaN(d.getTime())) return "—";
    return d.toLocaleDateString("en-GB", {
      day: "2-digit",
      month: "short",
      year: "2-digit",
      timeZone: "UTC",
    });
  };

  const str1 = formatShort(d1);
  const str2 = formatShort(d2);

  let durationDays = "";
  if (d1 && d2 && !isNaN(d1.getTime()) && !isNaN(d2.getTime())) {
    const diffTime = d2.getTime() - d1.getTime();
    const diffDays = Math.round(diffTime / (1000 * 60 * 60 * 24)) + 1;
    if (diffDays > 0) {
      durationDays = `${diffDays} Day${diffDays > 1 ? "s" : ""}`;
    }
  }

  return {
    rangeStr:
      str1 !== "—" && str2 !== "—"
        ? `${str1} → ${str2}`
        : str1 !== "—"
        ? str1
        : "Dates pending",
    duration: durationDays,
  };
};

// Formats createdAt or fallback into top header timestamp "15 Jul 2026, 16:00"
const formatMetaDate = (createdVal, fallbackStart) => {
  if (!createdVal && !fallbackStart) return "Recent Safari";
  const val = createdVal || fallbackStart;
  try {
    if (Array.isArray(val) && val.length >= 3) {
      const d = new Date(
        Date.UTC(val[0], val[1] - 1, val[2], val[3] || 12, val[4] || 0)
      );
      return (
        d.toLocaleDateString("en-GB", {
          day: "numeric",
          month: "short",
          year: "numeric",
        }) +
        (createdVal
          ? `, ${d.toLocaleTimeString("en-GB", {
              hour: "2-digit",
              minute: "2-digit",
            })}`
          : "")
      );
    }
    const d = new Date(val);
    if (!isNaN(d.getTime())) {
      return (
        d.toLocaleDateString("en-GB", {
          day: "numeric",
          month: "short",
          year: "numeric",
        }) +
        (createdVal
          ? `, ${d.toLocaleTimeString("en-GB", {
              hour: "2-digit",
              minute: "2-digit",
            })}`
          : "")
      );
    }
  } catch {
    // ignore
  }
  return String(val).slice(0, 10);
};

// Precise, clear safari status config reflecting real expedition lifecycle
const getStatusConfig = (status) => {
  const s = String(status || "").toUpperCase();
  switch (s) {
    case "CONFIRMED":
      return {
        label: "Confirmed",
        pillClass:
          "bg-emerald-50 text-emerald-700 border border-emerald-200/90 font-medium",
        dotColor: "bg-emerald-500",
        operationalText: "Confirmed (Lodging in progress)",
        highlightRow: "",
      };
    case "DRAFT":
      return {
        label: "Draft",
        pillClass:
          "bg-amber-50 text-amber-700 border border-amber-200/90 font-medium",
        dotColor: "bg-amber-500",
        operationalText: "Itinerary in Draft",
        highlightRow: "bg-amber-50/70 text-amber-900 px-1.5 py-0.5 -mx-1.5 rounded",
      };
    case "COMPLETED":
      return {
        label: "Completed",
        pillClass:
          "bg-blue-50 text-blue-700 border border-blue-200/90 font-medium",
        dotColor: "bg-blue-500",
        operationalText: "All Days & Lodges Booked",
        highlightRow: "",
      };
    case "CANCELLED":
      return {
        label: "Cancelled",
        pillClass:
          "bg-rose-50 text-rose-700 border border-rose-200/90 font-medium",
        dotColor: "bg-rose-500",
        operationalText: "Expedition Cancelled",
        highlightRow: "bg-rose-50/70 text-rose-900 px-1.5 py-0.5 -mx-1.5 rounded",
      };
    default:
      return {
        label: status || "Operational",
        pillClass:
          "bg-slate-100 text-slate-700 border border-slate-200 font-medium",
        dotColor: "bg-slate-400",
        operationalText: status || "Active Expedition",
        highlightRow: "",
      };
  }
};

const SafariCardsView = ({
  items = [],
  totalElements = 0,
  totalPages = 1,
  page = 0,
  pageSize = 8,
  onPageChange = () => {},
  onPageSizeChange = () => {},
  loading = false,
  searchTerm = "",
  onSearchChange = () => {},
  statusFilter = "",
  onStatusFilterChange = () => {},
  sortField = "createdAt",
  sortDirection = "desc",
  onSortChange = () => {},
  canCreate = false,
  onCreate = () => {},
  actions = [],
  permissions = {},
  onAction = () => {},
}) => {
  const { can } = useAuth();
  const [viewMode, setViewMode] = useState("grid"); // 'grid' | 'list'
  const [showFilterPanel, setShowFilterPanel] = useState(false);

  // Compute status counts for the top tabs
  const statusCounts = useMemo(() => {
    const counts = {
      ALL: totalElements || items.length,
      CONFIRMED: 0,
      DRAFT: 0,
      COMPLETED: 0,
      CANCELLED: 0,
    };

    items.forEach((item) => {
      const s = String(item.status || "").toUpperCase();
      if (counts[s] !== undefined) {
        counts[s] += 1;
      }
    });

    if (statusFilter && counts[statusFilter.toUpperCase()] !== undefined) {
      counts[statusFilter.toUpperCase()] = totalElements;
    }

    return counts;
  }, [items, totalElements, statusFilter]);

  // Meaningful status tabs matching backend filters and safari lifecycle
  const tabs = [
    { key: "", label: "ALL", count: statusCounts.ALL, icon: SquaresFour },
    { key: "CONFIRMED", label: "CONFIRMED", count: statusCounts.CONFIRMED, icon: CheckCircle },
    { key: "DRAFT", label: "DRAFT", count: statusCounts.DRAFT, icon: Clock },
    { key: "COMPLETED", label: "COMPLETED", count: statusCounts.COMPLETED, icon: AirplaneTakeoff },
    { key: "CANCELLED", label: "CANCELLED", count: statusCounts.CANCELLED, icon: WarningCircle },
  ];

  // Sorting options
  const sortOptions = [
    { label: "Newest First", field: "createdAt", direction: "desc" },
    { label: "Oldest First", field: "createdAt", direction: "asc" },
    { label: "Start Date (Earliest)", field: "startDate", direction: "asc" },
    { label: "Start Date (Latest)", field: "startDate", direction: "desc" },
    { label: "Reference (A-Z)", field: "referenceNumber", direction: "asc" },
    { label: "Client Name (A-Z)", field: "clientName", direction: "asc" },
  ];

  const currentSortKey = `${sortField}_${sortDirection.toLowerCase()}`;

  const handleSortSelect = (e) => {
    const selected = sortOptions.find(
      (opt) => `${opt.field}_${opt.direction.toLowerCase()}` === e.target.value
    );
    if (selected) {
      onSortChange(selected.field, selected.direction);
    }
  };

  // Safe action check
  const canView = actions.some((a) => a.key === "view") || can("view", permissions);
  const canUpdate = can("update", permissions);
  const canDelete = can("delete", permissions);

  // Pagination calculation
  const startRecord = items.length === 0 ? 0 : page * pageSize + 1;
  const endRecord = items.length === 0 ? 0 : Math.min((page + 1) * pageSize, totalElements);

  return (
    <div className="space-y-3 font-sans">
      {/* 1. Top Search & Controls Bar (Sitting directly on the page) */}
      <div className="flex flex-col gap-2.5 sm:flex-row sm:items-center sm:justify-between">
        {/* Search Input Bar */}
        <div className="relative flex-1">
          <span className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3 text-slate-400">
            <MagnifyingGlass size={15} />
          </span>
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => onSearchChange(e.target.value)}
            placeholder="Search safaris..."
            className="w-full h-9 rounded-md border border-slate-200/90 bg-white py-1.5 pl-8.5 pr-8 text-xs text-slate-800 placeholder:text-slate-400 focus:border-[#35634B] focus:outline-none focus:ring-2 focus:ring-[#35634B]/15 shadow-xs transition"
          />
          {searchTerm && (
            <button
              type="button"
              onClick={() => onSearchChange("")}
              className="absolute inset-y-0 right-0 flex items-center pr-2.5 text-slate-400 hover:text-slate-600 transition"
              title="Clear search"
            >
              <X size={13} />
            </button>
          )}
        </div>

        {/* Action Buttons: Filter, Sort, Create */}
        <div className="flex flex-wrap items-center gap-2">
          {/* Filter Toggle Button */}
          <button
            type="button"
            onClick={() => setShowFilterPanel((prev) => !prev)}
            className={`inline-flex h-9 items-center gap-1.5 rounded-md border px-3 text-xs font-medium shadow-xs transition ${
              showFilterPanel || statusFilter
                ? "border-[#35634B] bg-[#35634B]/5 text-[#35634B]"
                : "border-slate-200/90 bg-white text-slate-700 hover:bg-slate-50"
            }`}
          >
            <FunnelSimple size={14} />
            <span>Filter</span>
            {statusFilter && (
              <span className="ml-0.5 rounded-full bg-[#35634B] px-1.5 py-0.2 text-[9px] text-white">
                1
              </span>
            )}
          </button>

          {/* Sort Dropdown */}
          <div className="relative inline-flex h-9 items-center rounded-md border border-slate-200/90 bg-white px-2.5 text-xs font-medium text-slate-700 shadow-xs hover:bg-slate-50 transition focus-within:border-[#35634B] focus-within:ring-2 focus-within:ring-[#35634B]/15">
            <ArrowsDownUp size={13} className="mr-1 text-slate-400 shrink-0" />
            <span className="text-slate-400 mr-1 hidden sm:inline text-[11px]">Sort:</span>
            <select
              value={currentSortKey}
              onChange={handleSortSelect}
              aria-label="Sort safaris"
              className="cursor-pointer appearance-none bg-transparent pr-4 text-xs font-medium text-slate-800 outline-none"
            >
              {sortOptions.map((opt) => (
                <option
                  key={`${opt.field}_${opt.direction}`}
                  value={`${opt.field}_${opt.direction.toLowerCase()}`}
                >
                  {opt.label}
                </option>
              ))}
            </select>
            <CaretDown size={11} className="pointer-events-none absolute right-1.5 text-slate-400" />
          </div>

          {/* Create Safari Button */}
          {canCreate && (
            <button
              type="button"
              onClick={onCreate}
              className="inline-flex h-9 items-center gap-2 rounded-xl bg-gradient-to-r from-[#264624] via-[#2D532B] to-[#35634B] px-3.5 text-xs font-semibold text-white shadow-xs hover:shadow-md hover:shadow-[#264624]/20 transition-all active:scale-[0.98] cursor-pointer border border-white/10"
            >
              <Plus size={13} weight="bold" />
              <span>New Safari</span>
            </button>
          )}
        </div>
      </div>

      {/* Collapsible Filter Panel */}
      {showFilterPanel && (
        <div className="rounded-md border border-slate-200/90 bg-white p-3 shadow-xs flex flex-wrap items-center justify-between gap-2.5 text-xs">
          <div className="flex flex-wrap items-center gap-1.5">
            <span className="font-medium text-slate-600 text-[11px]">Filter by Status:</span>
            {tabs.map((tab) => (
              <button
                key={tab.key}
                type="button"
                onClick={() => onStatusFilterChange(tab.key)}
                className={`rounded-md px-2 py-0.5 text-[10px] font-medium transition ${
                  statusFilter === tab.key
                    ? "bg-[#35634B] text-white"
                    : "bg-slate-100 text-slate-600 hover:bg-slate-200"
                }`}
              >
                {tab.label}
              </button>
            ))}
          </div>

          {(statusFilter || searchTerm) && (
            <button
              type="button"
              onClick={() => {
                onStatusFilterChange("");
                onSearchChange("");
              }}
              className="text-[10px] text-rose-600 hover:text-rose-700 underline font-medium"
            >
              Reset filters
            </button>
          )}
        </div>
      )}

      {/* 2. Status Tabs Bar OUTSIDE background with reduced font size */}
      <div className="border-b border-slate-200/70 pt-0.5">
        <div className="flex items-center gap-4.5 overflow-x-auto no-scrollbar">
          {tabs.map((tab) => {
            const isActive = statusFilter === tab.key;
            const TabIcon = tab.icon;
            return (
              <button
                key={tab.key}
                type="button"
                onClick={() => onStatusFilterChange(tab.key)}
                className={`group relative flex items-center gap-1.5 pb-2 text-[11px] font-medium tracking-wide transition whitespace-nowrap ${
                  isActive
                    ? "text-slate-900 border-b-2 border-[#10b981] font-semibold -mb-px"
                    : "text-slate-500 hover:text-slate-800 border-b-2 border-transparent -mb-px"
                }`}
              >
                {TabIcon && (
                  <TabIcon
                    size={12}
                    className={isActive ? "text-[#10b981]" : "text-slate-400 group-hover:text-slate-600"}
                    weight={isActive ? "bold" : "regular"}
                  />
                )}
                <span>{tab.label}</span>
                <span
                  className={`rounded px-1.5 py-0.2 text-[9px] font-semibold transition ${
                    isActive
                      ? "bg-emerald-100 text-emerald-800"
                      : "bg-slate-200/70 text-slate-500 group-hover:bg-slate-300/80"
                  }`}
                >
                  {tab.count}
                </span>
              </button>
            );
          })}
        </div>
      </div>

      {/* Results Sub-bar: "11 - 18 of 64 results | Results per page: 8 | View: [::] [=]" */}
      <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between px-1">
        <p className="text-[11px] text-slate-500">
          Showing <span className="font-semibold text-slate-700">{startRecord}</span> -{" "}
          <span className="font-semibold text-slate-700">{endRecord}</span> of{" "}
          <span className="font-semibold text-slate-700">{totalElements}</span> results
        </p>

        <div className="flex items-center gap-3 text-[11px] text-slate-600">
          <div className="flex items-center gap-1.5">
            <span className="text-slate-500">Results per page:</span>
            <select
              value={pageSize}
              onChange={(e) => onPageSizeChange(Number(e.target.value))}
              aria-label="Results per page"
              className="rounded-lg border border-slate-200 bg-white px-2 py-0.5 text-[11px] font-semibold text-slate-700 outline-none cursor-pointer hover:border-slate-300"
            >
              <option value={8}>8</option>
              <option value={12}>12</option>
              <option value={24}>24</option>
              <option value={48}>48</option>
            </select>
          </div>

          {/* View switcher: Grid vs List */}
          <div className="flex items-center gap-1">
            <span className="text-slate-500 mr-0.5">View:</span>
            <button
              type="button"
              onClick={() => setViewMode("grid")}
              title="Grid card view"
              className={`p-1 rounded-md transition ${
                viewMode === "grid"
                  ? "bg-[#10b981]/15 text-[#059669]"
                  : "text-slate-400 hover:text-slate-600 hover:bg-slate-100"
              }`}
            >
              <SquaresFour size={16} weight={viewMode === "grid" ? "fill" : "regular"} />
            </button>
            <button
              type="button"
              onClick={() => setViewMode("list")}
              title="Expanded list view"
              className={`p-1 rounded-md transition ${
                viewMode === "list"
                  ? "bg-[#10b981]/15 text-[#059669]"
                  : "text-slate-400 hover:text-slate-600 hover:bg-slate-100"
              }`}
            >
              <Rows size={16} weight={viewMode === "list" ? "fill" : "regular"} />
            </button>
          </div>
        </div>
      </div>

      {/* Main Safari Cards Container */}
      {loading ? (
        // Skeletons matching exact compact card structure
        <div
          className={`grid gap-3.5 ${
            viewMode === "grid" ? "grid-cols-1 md:grid-cols-2" : "grid-cols-1"
          }`}
        >
          {Array.from({ length: 4 }).map((_, idx) => (
            <div
              key={idx}
              className="rounded-md border border-slate-200/80 bg-white p-4 shadow-sm animate-pulse space-y-3"
            >
              <div className="flex items-center justify-between pb-2 border-b border-slate-100">
                <div className="h-2.5 w-24 bg-slate-200 rounded"></div>
                <div className="h-2.5 w-32 bg-slate-200 rounded"></div>
              </div>
              <div className="flex items-center justify-between">
                <div className="space-y-1">
                  <div className="h-4 w-20 bg-slate-200 rounded"></div>
                  <div className="h-2.5 w-36 bg-slate-100 rounded"></div>
                </div>
                <div className="h-5 w-16 bg-slate-200 rounded-md"></div>
              </div>
              <div className="space-y-1.5 pt-1.5">
                <div className="h-3 bg-slate-100 rounded w-full"></div>
                <div className="h-3 bg-slate-100 rounded w-full"></div>
                <div className="h-3 bg-slate-100 rounded w-full"></div>
                <div className="h-3 bg-slate-100 rounded w-3/4"></div>
              </div>
            </div>
          ))}
        </div>
      ) : items.length === 0 ? (
        // Clean Empty State
        <div className="rounded-md border border-slate-200/80 bg-white p-10 text-center shadow-sm">
          <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-md bg-[#35634B]/10 text-[#35634B]">
            <Compass size={24} />
          </div>
          <h3 className="mt-3 text-sm font-semibold text-slate-800">
            No safaris found
          </h3>
          <p className="mt-1 text-[11px] text-slate-500 max-w-sm mx-auto">
            {searchTerm || statusFilter
              ? "We couldn't find any safaris matching your active search terms or status filters."
              : "No safari itineraries exist in your company yet. Create your first safari quotation to begin."}
          </p>
          <div className="mt-4 flex items-center justify-center gap-2.5">
            {(searchTerm || statusFilter) && (
              <button
                type="button"
                onClick={() => {
                  onSearchChange("");
                  onStatusFilterChange("");
                }}
                className="rounded-md border border-slate-200 px-3.5 py-1.5 text-xs font-medium text-slate-700 hover:bg-slate-50 transition"
              >
                Clear all filters
              </button>
            )}
            {canCreate && (
              <button
                type="button"
                onClick={onCreate}
                className="inline-flex items-center gap-2 rounded-xl bg-gradient-to-r from-[#264624] via-[#2D532B] to-[#35634B] px-4 py-2 text-xs font-semibold text-white shadow-xs hover:shadow-md hover:shadow-[#264624]/20 transition-all active:scale-[0.98] cursor-pointer border border-white/10"
              >
                <Plus size={14} weight="bold" />
                <span>New Safari</span>
              </button>
            )}
          </div>
        </div>
      ) : (
        // Safari Cards Grid (2-columns on md/lg, responsive, compact matching safari_card_example.jpg)
        <div
          className={`grid gap-3.5 ${
            viewMode === "grid" ? "grid-cols-1 md:grid-cols-2" : "grid-cols-1"
          }`}
        >
          {items.map((item) => {
            const statusConfig = getStatusConfig(item.status);
            const dateRange = formatDateRange(item.startDate, item.endDate);
            const metaDate = formatMetaDate(item.createdAt, item.startDate);
            const locationTag =
              item.notes?.split("\n")[0]?.slice(0, 32) ||
              "Northern Circuit, Tanzania";

            return (
              <div
                key={item.id || item.referenceNumber}
                onClick={() => onAction("view", item)}
                tabIndex={0}
                role="button"
                onKeyDown={(e) => {
                  if (e.key === "Enter" || e.key === " ") {
                    e.preventDefault();
                    onAction("view", item);
                  }
                }}
                className="group relative flex flex-col justify-between rounded-md border border-slate-200/80 bg-white p-4 shadow-sm transition-all duration-200 hover:border-slate-300 hover:shadow-md cursor-pointer outline-none focus-visible:ring-2 focus-visible:ring-[#35634B]"
              >
                {/* 1. Header Row (Created date on left, Location on right) */}
                <div>
                  <div className="flex items-center justify-between text-[11px] text-slate-400 font-normal">
                    <span>{metaDate}</span>
                    <span className="truncate max-w-[180px]" title={locationTag}>
                      {locationTag}
                    </span>
                  </div>

                  {/* Thin divider */}
                  <div className="my-2 border-b border-slate-100" />

                  {/* 2. Identifier & Status Row */}
                  <div className="flex items-start justify-between gap-2.5">
                    <div className="flex items-start gap-2 min-w-0">
                      {/* Compact Icon */}
                      <div className="mt-0.5 flex h-6 w-6 shrink-0 items-center justify-center rounded-md bg-[#35634B]/10 text-[#35634B]">
                        <Compass size={14} weight="duotone" />
                      </div>

                      {/* Reference code and dates */}
                      <div className="min-w-0">
                        <span
                          className="font-semibold text-[13px] text-slate-900 tracking-tight group-hover:text-[#35634B] transition truncate block"
                          title="View Safari Itinerary"
                        >
                          {item.referenceNumber || `SAF-${String(item.id).substring(0, 8)}`}
                        </span>
                        <p className="mt-0.5 text-[11px] text-slate-500 font-normal flex items-center gap-1.5 flex-wrap">
                          <span>{dateRange.rangeStr}</span>
                          {dateRange.duration && (
                            <span className="text-slate-400">· {dateRange.duration}</span>
                          )}
                        </p>
                      </div>
                    </div>

                    {/* Status Pill Badge & Caret right */}
                    <div className="flex items-center gap-1 shrink-0">
                      <span
                        className={`inline-flex items-center rounded-md px-2.5 py-0.5 text-[11px] ${statusConfig.pillClass}`}
                      >
                        {statusConfig.label}
                      </span>

                      {canView && (
                        <span
                          className="rounded-md p-0.5 text-slate-400 group-hover:text-slate-800 transition"
                          title="Open Safari Details"
                        >
                          <CaretRight size={14} weight="bold" />
                        </span>
                      )}
                    </div>
                  </div>

                  {/* 3. Card Body: Compact Key-Value Rows matching safari_card_example.jpg */}
                  <div className="mt-2.5 space-y-1 text-[11px]">
                    {/* Row 1: Customer */}
                    <div className="flex items-center justify-between py-1 border-b border-slate-50">
                      <span className="text-slate-400 font-normal">Customer</span>
                      <span className="text-[12px] font-medium text-slate-800 text-right truncate max-w-[220px]">
                        {item.clientName || "—"}
                      </span>
                    </div>

                    {/* Row 2: Travelers / Passengers */}
                    <div className="flex items-center justify-between py-1 border-b border-slate-50">
                      <span className="text-slate-400 font-normal">Travelers</span>
                      <span className="text-[12px] font-medium text-slate-700 flex items-center gap-1">
                        <Users size={13} className="text-slate-400" />
                        <span>
                          {item.numberOfPassengers
                            ? `${item.numberOfPassengers} Passenger${
                                item.numberOfPassengers > 1 ? "s" : ""
                              }`
                            : "1 Passenger"}
                        </span>
                      </span>
                    </div>

                    {/* Row 3: Sales Person */}
                    <div className="flex items-center justify-between py-1 border-b border-slate-50">
                      <span className="text-slate-400 font-normal">Sales Person</span>
                      <span className="text-[12px] font-medium text-slate-700 text-right truncate max-w-[200px]">
                        {item.salesPersonName || "Unassigned"}
                      </span>
                    </div>

                    {/* Row 4: Operational Status with Colored Dot */}
                    <div
                      className={`flex items-center justify-between py-1 transition ${statusConfig.highlightRow}`}
                    >
                      <span className="text-slate-400 font-normal">Safari status</span>
                      <span className="text-[11px] font-medium text-slate-700 flex items-center gap-1.5">
                        <span
                          className={`inline-block h-1.5 w-1.5 rounded-full ${statusConfig.dotColor}`}
                        />
                        <span>{statusConfig.operationalText}</span>
                      </span>
                    </div>
                  </div>
                </div>

                {/* 4. Action Buttons Footer: Preserves ALL capabilities */}
                <div
                  className="mt-3 pt-2.5 border-t border-slate-100 flex items-center justify-end gap-1.5"
                  onClick={(e) => e.stopPropagation()}
                >
                  {canView && (
                    <button
                      type="button"
                      onClick={() => onAction("view", item)}
                      className="inline-flex h-7.5 items-center justify-center gap-1 rounded-md border border-slate-200 bg-white px-2.5 text-[11px] font-medium text-slate-700 shadow-xs transition hover:border-[#35634B] hover:text-[#35634B] hover:bg-slate-50 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#35634B]"
                    >
                      <Eye size={13} />
                      <span>View</span>
                    </button>
                  )}

                  {canUpdate && (
                    <button
                      type="button"
                      onClick={() => onAction("update", item)}
                      className="inline-flex h-7.5 items-center justify-center gap-1 rounded-md border border-slate-200 bg-white px-2.5 text-[11px] font-medium text-slate-700 shadow-xs transition hover:border-slate-300 hover:bg-slate-50"
                      title="Edit"
                    >
                      <PencilSimple size={13} />
                      <span>Edit</span>
                    </button>
                  )}

                  {canDelete && (
                    <button
                      type="button"
                      onClick={() => onAction("delete", item)}
                      className="inline-flex h-7.5 w-7.5 items-center justify-center rounded-md border border-slate-200 bg-white text-slate-400 shadow-xs transition hover:border-rose-300 hover:bg-rose-50 hover:text-rose-600"
                      title="Delete"
                    >
                      <Trash size={13} />
                    </button>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Pagination Footer matching `< [ 2 ] out of 7 >` in safari_card_example.jpg */}
      <div className="flex flex-col gap-2 rounded-md border border-slate-200/80 bg-white p-3 shadow-sm sm:flex-row sm:items-center sm:justify-between">
        <div className="flex items-center gap-2 text-[11px] text-slate-500 font-sans">
          <span>
            Showing records {startRecord} - {endRecord} of {totalElements}
          </span>
        </div>

        <div className="flex items-center gap-2 text-xs font-medium text-slate-700">
          {/* Previous Page Button */}
          <button
            type="button"
            onClick={() => onPageChange(Math.max(page - 1, 0))}
            disabled={page === 0 || loading}
            aria-label="Previous Page"
            className="flex h-7 w-7 items-center justify-center rounded-md border border-slate-200 text-slate-600 hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-40 transition"
          >
            <CaretLeft size={13} weight="bold" />
          </button>

          {/* Current Page Box & Total Pages */}
          <span className="flex items-center gap-1.5 px-1">
            <span className="rounded-md border border-slate-200 bg-[#FAFBF9] px-2 py-0.5 text-[11px] font-bold text-slate-800">
              {page + 1}
            </span>
            <span className="text-[11px] text-slate-500">out of {Math.max(totalPages, 1)}</span>
          </span>

          {/* Next Page Button */}
          <button
            type="button"
            onClick={() => onPageChange(page + 1)}
            disabled={page >= totalPages - 1 || loading}
            aria-label="Next Page"
            className="flex h-7 w-7 items-center justify-center rounded-md border border-slate-200 text-slate-600 hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-40 transition"
          >
            <CaretRight size={13} weight="bold" />
          </button>
        </div>
      </div>
    </div>
  );
};

export default SafariCardsView;
