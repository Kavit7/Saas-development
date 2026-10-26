import { useState, useMemo } from "react";
import {
  X,
  MagnifyingGlass,
  Tag as TagIcon,
  Buildings,
  MapPin,
  CheckCircle,
  XCircle,
  Check,
  Funnel,
  Bed,
  EnvelopeSimple,
  Phone
} from "@phosphor-icons/react";

/**
 * PropertyPickerModal Component
 * Wide, dedicated property browser with multi-filter support:
 * - Search by name, location, and description
 * - Clickable tag filter pills
 * - Category and price tier filtering
 * - Strict disabled state & warning for previously declined lodges
 */
const PropertyPickerModal = ({
  open,
  onClose,
  properties = [],
  selectedPropertyId,
  onSelectProperty,
  declinedPropertyIds = [],
  requirement = null,
}) => {
  const [searchQuery, setSearchQuery] = useState("");
  const [selectedTag, setSelectedTag] = useState(null);
  const [selectedCategory, setSelectedCategory] = useState("");
  const [selectedPriceTier, setSelectedPriceTier] = useState("");
  const [hideDeclined, setHideDeclined] = useState(false);

  // Extract all unique tags across all available properties
  const availableTags = useMemo(() => {
    const map = new Map();
    (properties || []).forEach((prop) => {
      (prop.tags || []).forEach((t) => {
        const key = t.id || t.name;
        if (key && !map.has(key)) {
          map.set(key, { id: t.id, name: t.name });
        }
      });
    });
    return Array.from(map.values()).sort((a, b) => a.name.localeCompare(b.name));
  }, [properties]);

  // Extract all unique categories and price tiers for filter dropdowns
  const availableCategories = useMemo(() => {
    const set = new Set();
    (properties || []).forEach((p) => {
      if (p.categoryName) set.add(p.categoryName);
    });
    return Array.from(set).sort();
  }, [properties]);

  const availablePriceTiers = useMemo(() => {
    const set = new Set();
    (properties || []).forEach((p) => {
      if (p.priceTierName) set.add(p.priceTierName);
    });
    return Array.from(set).sort();
  }, [properties]);

  // Filter properties based on active criteria
  const filteredProperties = useMemo(() => {
    return (properties || []).filter((prop) => {
      const isDeclined = declinedPropertyIds.some(
        (dId) => String(dId) === String(prop.id)
      );

      if (hideDeclined && isDeclined) return false;

      // Text search
      if (searchQuery.trim()) {
        const q = searchQuery.toLowerCase();
        const matchesName = prop.name?.toLowerCase().includes(q);
        const matchesLocation = [prop.location, prop.region, prop.country]
          .filter(Boolean)
          .join(" ")
          .toLowerCase()
          .includes(q);
        const matchesDesc = prop.description?.toLowerCase().includes(q);
        if (!matchesName && !matchesLocation && !matchesDesc) return false;
      }

      // Category filter
      if (selectedCategory && prop.categoryName !== selectedCategory) {
        return false;
      }

      // Price Tier filter
      if (selectedPriceTier && prop.priceTierName !== selectedPriceTier) {
        return false;
      }

      // Tag filter
      if (selectedTag) {
        const hasTag = (prop.tags || []).some(
          (t) => (t.id && String(t.id) === String(selectedTag)) || t.name === selectedTag
        );
        if (!hasTag) return false;
      }

      return true;
    });
  }, [
    properties,
    searchQuery,
    selectedCategory,
    selectedPriceTier,
    selectedTag,
    hideDeclined,
    declinedPropertyIds,
  ]);

  if (!open) return null;

  return (
    <div className="fixed inset-0 z-60 flex items-center justify-center p-3 sm:p-6 bg-black/60 backdrop-blur-xs animate-in fade-in duration-200">
      <div className="flex flex-col w-full max-w-6xl max-h-[92vh] rounded-3xl bg-white shadow-2xl border border-slate-200 overflow-hidden font-sans animate-in zoom-in-95 duration-150">
        
        {/* Top Header */}
        <div className="flex items-center justify-between border-b border-slate-100 px-6 py-4.5 bg-slate-50/80 shrink-0">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-[#264624]/10 text-[#264624] flex items-center justify-center font-bold shadow-2xs">
              <Buildings size={22} weight="duotone" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h2 className="text-base sm:text-lg font-bold text-slate-900 font-serif-title">
                  Select Partner Lodge / Property
                </h2>
                <span className="px-2 py-0.5 rounded-full bg-[#264624]/10 text-[#264624] font-bold text-[11px]">
                  {filteredProperties.length} Available
                </span>
              </div>
              <p className="text-xs text-slate-500 mt-0.5">
                Browse lodges, filter by tag, category, and tier. Lodges that declined this requirement are disabled.
              </p>
            </div>
          </div>

          <button
            type="button"
            onClick={onClose}
            className="rounded-xl p-2 text-slate-400 hover:text-slate-700 hover:bg-slate-200/60 transition"
          >
            <X size={20} weight="bold" />
          </button>
        </div>

        {/* Filter Controls Bar */}
        <div className="p-4 sm:p-5 border-b border-slate-100 bg-white space-y-3 shrink-0">
          <div className="grid grid-cols-1 sm:grid-cols-12 gap-3 items-center">
            {/* Search Box */}
            <div className="sm:col-span-5 relative">
              <MagnifyingGlass
                size={18}
                className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400"
              />
              <input
                type="text"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                placeholder="Search by lodge name, location, or circuit..."
                className="w-full pl-10 pr-4 py-2.5 rounded-xl border border-slate-200 text-xs text-slate-800 placeholder-slate-400 bg-slate-50/50 focus:bg-white focus:border-[#264624] focus:ring-1 focus:ring-[#264624] outline-hidden transition shadow-2xs"
              />
              {searchQuery && (
                <button
                  type="button"
                  onClick={() => setSearchQuery("")}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
                >
                  <X size={14} />
                </button>
              )}
            </div>

            {/* Category Filter */}
            <div className="sm:col-span-3">
              <select
                value={selectedCategory}
                onChange={(e) => setSelectedCategory(e.target.value)}
                className="w-full px-3 py-2.5 rounded-xl border border-slate-200 text-xs text-slate-700 bg-slate-50/50 focus:bg-white focus:border-[#264624] focus:ring-1 focus:ring-[#264624] outline-hidden transition shadow-2xs"
              >
                <option value="">All Categories ({availableCategories.length})</option>
                {availableCategories.map((c) => (
                  <option key={c} value={c}>
                    {c}
                  </option>
                ))}
              </select>
            </div>

            {/* Price Tier Filter */}
            <div className="sm:col-span-2">
              <select
                value={selectedPriceTier}
                onChange={(e) => setSelectedPriceTier(e.target.value)}
                className="w-full px-3 py-2.5 rounded-xl border border-slate-200 text-xs text-slate-700 bg-slate-50/50 focus:bg-white focus:border-[#264624] focus:ring-1 focus:ring-[#264624] outline-hidden transition shadow-2xs"
              >
                <option value="">All Price Tiers</option>
                {availablePriceTiers.map((t) => (
                  <option key={t} value={t}>
                    {t}
                  </option>
                ))}
              </select>
            </div>

            {/* Reset / Toggle Controls */}
            <div className="sm:col-span-2 flex items-center justify-end gap-2">
              {(searchQuery || selectedTag || selectedCategory || selectedPriceTier) && (
                <button
                  type="button"
                  onClick={() => {
                    setSearchQuery("");
                    setSelectedTag(null);
                    setSelectedCategory("");
                    setSelectedPriceTier("");
                  }}
                  className="px-3 py-2 text-xs font-semibold text-rose-600 hover:bg-rose-50 rounded-xl transition"
                >
                  Clear Filters
                </button>
              )}
            </div>
          </div>

          {/* Tag Filter Pills */}
          {availableTags.length > 0 && (
            <div className="flex items-center gap-1.5 overflow-x-auto pb-1 pt-0.5 scrollbar-thin">
              <span className="text-[11px] font-bold uppercase tracking-wider text-slate-400 shrink-0 flex items-center gap-1 mr-1">
                <TagIcon size={13} weight="bold" />
                Tags:
              </span>

              <button
                type="button"
                onClick={() => setSelectedTag(null)}
                className={`px-2.5 py-1 rounded-lg text-xs font-semibold shrink-0 transition active:scale-95 ${
                  selectedTag === null
                    ? "bg-[#264624] text-white shadow-2xs"
                    : "bg-slate-100 text-slate-600 hover:bg-slate-200"
                }`}
              >
                All
              </button>

              {availableTags.map((tag) => {
                const isActive = selectedTag === tag.id || selectedTag === tag.name;
                return (
                  <button
                    key={tag.id || tag.name}
                    type="button"
                    onClick={() => setSelectedTag(isActive ? null : tag.id || tag.name)}
                    className={`px-2.5 py-1 rounded-lg text-xs font-semibold shrink-0 transition active:scale-95 flex items-center gap-1 ${
                      isActive
                        ? "bg-[#264624] text-white shadow-2xs"
                        : "bg-slate-100 text-slate-600 hover:bg-slate-200"
                    }`}
                  >
                    <span>#{tag.name}</span>
                  </button>
                );
              })}
            </div>
          )}
        </div>

        {/* Property Grid Body */}
        <div className="flex-1 overflow-y-auto p-4 sm:p-6 bg-slate-50/50">
          {filteredProperties.length === 0 ? (
            <div className="py-16 text-center space-y-3">
              <Buildings size={44} className="mx-auto text-slate-300" />
              <div>
                <p className="font-bold text-slate-800 text-sm">No partner lodges found</p>
                <p className="text-xs text-slate-500 mt-1">
                  Try clearing your search query or tag filters to see more results.
                </p>
              </div>
              <button
                type="button"
                onClick={() => {
                  setSearchQuery("");
                  setSelectedTag(null);
                  setSelectedCategory("");
                  setSelectedPriceTier("");
                }}
                className="px-4 py-2 text-xs font-semibold text-[#264624] bg-white border border-slate-200 rounded-xl hover:bg-slate-50 transition"
              >
                Reset All Filters
              </button>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {filteredProperties.map((prop) => {
                const isSelected = String(selectedPropertyId) === String(prop.id);
                const isDeclined = declinedPropertyIds.some(
                  (dId) => String(dId) === String(prop.id)
                );

                return (
                  <div
                    key={prop.id}
                    className={`flex flex-col justify-between rounded-2xl p-4.5 border transition-all duration-200 ${
                      isDeclined
                        ? "border-rose-200 bg-rose-50/40 opacity-85 shadow-2xs"
                        : isSelected
                        ? "border-[#264624] bg-white ring-2 ring-[#264624]/20 shadow-md"
                        : "border-slate-200 bg-white hover:border-slate-300 hover:shadow-sm"
                    }`}
                  >
                    <div>
                      {/* Declined Warning Banner */}
                      {isDeclined && (
                        <div className="mb-3 px-3 py-1.5 rounded-xl bg-rose-100 border border-rose-200/80 text-rose-800 text-[11px] font-bold flex items-center gap-1.5">
                          <XCircle size={15} weight="bold" className="text-rose-600 shrink-0" />
                          <span>Previously Declined for this Stay</span>
                        </div>
                      )}

                      {/* Header with Title and Verification */}
                      <div className="flex items-start justify-between gap-2">
                        <div>
                          <h4 className="font-bold text-slate-900 text-sm leading-snug">
                            {prop.name}
                          </h4>
                          <p className="text-xs text-slate-500 flex items-center gap-1 mt-1">
                            <MapPin size={13} className="text-[#264624] shrink-0" weight="fill" />
                            <span>
                              {[prop.location, prop.region, prop.country]
                                .filter(Boolean)
                                .join(", ") || "Location not specified"}
                            </span>
                          </p>
                        </div>

                        {prop.verificationStatus && (
                          <span
                            className={`rounded-full px-2 py-0.5 text-[10px] font-bold shrink-0 ${
                              prop.verificationStatus === "VERIFIED"
                                ? "bg-emerald-100 text-emerald-800"
                                : "bg-slate-100 text-slate-600"
                            }`}
                          >
                            {prop.verificationStatus}
                          </span>
                        )}
                      </div>

                      {/* Category & Tier Badges */}
                      <div className="mt-3 flex flex-wrap gap-1.5 text-[11px]">
                        {prop.categoryName && (
                          <span className="px-2 py-0.5 rounded-md bg-slate-100 text-slate-700 font-medium">
                            {prop.categoryName}
                          </span>
                        )}
                        {prop.priceTierName && (
                          <span className="px-2 py-0.5 rounded-md bg-amber-50 text-amber-800 border border-amber-200/60 font-semibold">
                            {prop.priceTierName}
                          </span>
                        )}
                      </div>

                      {/* Tags List */}
                      {prop.tags?.length > 0 && (
                        <div className="mt-2.5 flex flex-wrap gap-1">
                          {prop.tags.slice(0, 4).map((t, idx) => (
                            <span
                              key={idx}
                              className="px-2 py-0.5 rounded-md bg-[#264624]/10 text-[#264624] text-[10px] font-bold"
                            >
                              #{t.name}
                            </span>
                          ))}
                          {prop.tags.length > 4 && (
                            <span className="px-1.5 py-0.5 text-[10px] text-slate-400 font-medium">
                              +{prop.tags.length - 4} more
                            </span>
                          )}
                        </div>
                      )}

                      {/* Contact details */}
                      {(prop.contactEmail || prop.contactPhone) && (
                        <div className="mt-3 pt-2.5 border-t border-slate-100 space-y-1 text-[11px] text-slate-500">
                          {prop.contactEmail && (
                            <p className="flex items-center gap-1.5 truncate">
                              <EnvelopeSimple size={13} className="shrink-0 text-slate-400" />
                              <span className="truncate">{prop.contactEmail}</span>
                            </p>
                          )}
                          {prop.contactPhone && (
                            <p className="flex items-center gap-1.5">
                              <Phone size={13} className="shrink-0 text-slate-400" />
                              <span>{prop.contactPhone}</span>
                            </p>
                          )}
                        </div>
                      )}
                    </div>

                    {/* Action Button */}
                    <div className="mt-4 pt-3 border-t border-slate-100">
                      {isDeclined ? (
                        <button
                          type="button"
                          disabled
                          className="w-full py-2 px-3 rounded-xl bg-slate-100 text-slate-400 font-bold text-xs cursor-not-allowed flex items-center justify-center gap-1.5"
                        >
                          <XCircle size={15} weight="bold" />
                          <span>Unavailable (Declined)</span>
                        </button>
                      ) : isSelected ? (
                        <button
                          type="button"
                          onClick={() => {
                            onSelectProperty(prop);
                            onClose();
                          }}
                          className="w-full py-2 px-3 rounded-xl bg-emerald-700 text-white font-bold text-xs flex items-center justify-center gap-1.5 shadow-xs"
                        >
                          <Check size={16} weight="bold" />
                          <span>Selected (Current Choice)</span>
                        </button>
                      ) : (
                        <button
                          type="button"
                          onClick={() => {
                            onSelectProperty(prop);
                            onClose();
                          }}
                          className="w-full py-2 px-3 rounded-xl bg-[#264624] text-white font-bold text-xs hover:bg-[#1b331a] transition active:scale-95 shadow-xs flex items-center justify-center gap-1.5"
                        >
                          <Buildings size={15} weight="bold" />
                          <span>Select This Lodge</span>
                        </button>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>

        {/* Footer info */}
        <div className="flex items-center justify-between border-t border-slate-100 px-6 py-3 bg-white text-xs text-slate-500 shrink-0">
          <div>
            {requirement?.destination && (
              <span>
                Target Circuit: <strong className="text-slate-800">{requirement.destination}</strong>
              </span>
            )}
            {requirement?.numberOfNights && (
              <span> • Stay: <strong className="text-slate-800">{requirement.numberOfNights} Night(s)</strong></span>
            )}
          </div>
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-2 font-semibold text-slate-600 hover:bg-slate-100 rounded-xl transition"
          >
            Close
          </button>
        </div>

      </div>
    </div>
  );
};

export default PropertyPickerModal;
