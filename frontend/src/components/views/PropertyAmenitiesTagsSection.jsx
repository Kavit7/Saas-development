import { useState } from "react";
import {
  Sparkle,
  Tag as TagIcon,
  Plus,
  X,
  Check,
  CheckCircle,
  Bookmarks,
} from "@phosphor-icons/react";

const getTagTypeBadgeClass = (type) => {
  const t = String(type || "").toUpperCase();
  switch (t) {
    case "PROPERTY":
      return "bg-emerald-50 text-emerald-700 border-emerald-200";
    case "VIBE":
      return "bg-purple-50 text-purple-700 border-purple-200";
    case "OCCASION":
      return "bg-rose-50 text-rose-700 border-rose-200";
    case "TRAVELER_TYPE":
      return "bg-blue-50 text-blue-700 border-blue-200";
    case "SPECIAL_NEED":
      return "bg-amber-50 text-amber-700 border-amber-200";
    default:
      return "bg-indigo-50 text-[#101B82] border-indigo-200";
  }
};

const PropertyAmenitiesTagsSection = ({
  property,
  allAmenities = [],
  allTags = [],
  canManage = false,
  onAddAmenities,
  onRemoveAmenity,
  onAddTags,
  onRemoveTag,
  loading = false,
}) => {
  const [showAddAmenityModal, setShowAddAmenityModal] = useState(false);
  const [showAddTagModal, setShowAddTagModal] = useState(false);
  const [selectedAmenityIds, setSelectedAmenityIds] = useState([]);
  const [selectedTagIds, setSelectedTagIds] = useState([]);
  const [submittingAction, setSubmittingAction] = useState(false);

  const assignedAmenities = property?.amenities || [];
  const assignedTags = property?.tags || [];

  const assignedAmenityIdSet = new Set(assignedAmenities.map((a) => String(a.id)));
  const assignedTagIdSet = new Set(assignedTags.map((t) => String(t.id)));

  // Filter out amenities that are already assigned
  const availableAmenities = allAmenities.filter(
    (a) => !assignedAmenityIdSet.has(String(a.value || a.id))
  );

  // Filter out tags that are already assigned
  const availableTags = allTags.filter(
    (t) => !assignedTagIdSet.has(String(t.value || t.id))
  );

  const handleToggleAmenity = (id) => {
    setSelectedAmenityIds((prev) =>
      prev.includes(id) ? prev.filter((item) => item !== id) : [...prev, id]
    );
  };

  const handleToggleTag = (id) => {
    setSelectedTagIds((prev) =>
      prev.includes(id) ? prev.filter((item) => item !== id) : [...prev, id]
    );
  };

  const submitAddAmenities = async () => {
    if (!selectedAmenityIds.length || !onAddAmenities) return;
    setSubmittingAction(true);
    try {
      await onAddAmenities(selectedAmenityIds);
      setSelectedAmenityIds([]);
      setShowAddAmenityModal(false);
    } finally {
      setSubmittingAction(false);
    }
  };

  const submitAddTags = async () => {
    if (!selectedTagIds.length || !onAddTags) return;
    setSubmittingAction(true);
    try {
      await onAddTags(selectedTagIds);
      setSelectedTagIds([]);
      setShowAddTagModal(false);
    } finally {
      setSubmittingAction(false);
    }
  };

  return (
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-5 font-sans">
      {/* 1. PROPERTY AMENITIES CARD */}
      <div className="rounded-2xl border border-slate-200/90 bg-white p-5 sm:p-6 shadow-xs flex flex-col justify-between">
        <div>
          <div className="flex items-center justify-between pb-4 border-b border-slate-100">
            <div className="flex items-center gap-2.5">
              <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-amber-50 text-amber-600 border border-amber-200/80">
                <Sparkle size={20} weight="fill" />
              </div>
              <div>
                <h3 className="text-sm sm:text-base font-bold text-slate-900 font-serif-title">
                  Property Amenities
                </h3>
                <p className="text-xs text-slate-500">
                  {assignedAmenities.length}{" "}
                  {assignedAmenities.length === 1 ? "amenity" : "amenities"} equipped
                </p>
              </div>
            </div>

            {canManage && (
              <button
                type="button"
                onClick={() => {
                  setSelectedAmenityIds([]);
                  setShowAddAmenityModal(true);
                }}
                disabled={loading || submittingAction}
                className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-semibold bg-[#101B82] text-white hover:bg-[#0c1566] transition active:scale-95 shadow-2xs disabled:opacity-50"
              >
                <Plus size={14} weight="bold" />
                <span>Assign Amenities</span>
              </button>
            )}
          </div>

          {/* Assigned Amenities List */}
          <div className="pt-4">
            {assignedAmenities.length === 0 ? (
              <div className="rounded-xl border border-dashed border-slate-200 p-6 text-center">
                <Sparkle size={24} className="mx-auto text-slate-300 mb-1.5" />
                <p className="text-xs text-slate-500 font-medium">
                  No amenities currently assigned to this property.
                </p>
                {canManage && (
                  <button
                    type="button"
                    onClick={() => {
                      setSelectedAmenityIds([]);
                      setShowAddAmenityModal(true);
                    }}
                    className="mt-2 text-xs font-semibold text-[#101B82] hover:underline"
                  >
                    + Assign amenities now
                  </button>
                )}
              </div>
            ) : (
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
                {assignedAmenities.map((amenity) => (
                  <div
                    key={amenity.id}
                    className="flex items-center justify-between gap-2 rounded-xl border border-slate-200/80 bg-slate-50/50 p-2.5 hover:bg-slate-50 transition"
                  >
                    <div className="flex items-center gap-2 min-w-0">
                      <CheckCircle
                        size={16}
                        weight="fill"
                        className="text-emerald-600 shrink-0"
                      />
                      <div className="min-w-0">
                        <span className="block text-xs font-bold text-slate-800 truncate">
                          {amenity.name}
                        </span>
                        {amenity.description && (
                          <span className="block text-[11px] text-slate-400 truncate">
                            {amenity.description}
                          </span>
                        )}
                      </div>
                    </div>

                    {canManage && onRemoveAmenity && (
                      <button
                        type="button"
                        onClick={() => onRemoveAmenity(amenity.id)}
                        disabled={loading || submittingAction}
                        title={`Remove ${amenity.name}`}
                        className="flex h-6 w-6 shrink-0 items-center justify-center rounded-lg text-slate-400 hover:bg-rose-50 hover:text-rose-600 transition"
                      >
                        <X size={13} weight="bold" />
                      </button>
                    )}
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* 2. PROPERTY TAGS & HIGHLIGHTS CARD */}
      <div className="rounded-2xl border border-slate-200/90 bg-white p-5 sm:p-6 shadow-xs flex flex-col justify-between">
        <div>
          <div className="flex items-center justify-between pb-4 border-b border-slate-100">
            <div className="flex items-center gap-2.5">
              <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-indigo-50 text-[#101B82] border border-indigo-200/80">
                <Bookmarks size={20} weight="fill" />
              </div>
              <div>
                <h3 className="text-sm sm:text-base font-bold text-slate-900 font-serif-title">
                  Tags & Safari Highlights
                </h3>
                <p className="text-xs text-slate-500">
                  {assignedTags.length}{" "}
                  {assignedTags.length === 1 ? "tag" : "tags"} categorizing this property
                </p>
              </div>
            </div>

            {canManage && (
              <button
                type="button"
                onClick={() => {
                  setSelectedTagIds([]);
                  setShowAddTagModal(true);
                }}
                disabled={loading || submittingAction}
                className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-semibold bg-[#101B82] text-white hover:bg-[#0d176f] transition active:scale-95 shadow-2xs disabled:opacity-50"
              >
                <Plus size={14} weight="bold" />
                <span>Assign Tags</span>
              </button>
            )}
          </div>

          {/* Assigned Tags List */}
          <div className="pt-4">
            {assignedTags.length === 0 ? (
              <div className="rounded-xl border border-dashed border-slate-200 p-6 text-center">
                <TagIcon size={24} className="mx-auto text-slate-300 mb-1.5" />
                <p className="text-xs text-slate-500 font-medium">
                  No tags or highlights assigned yet.
                </p>
                {canManage && (
                  <button
                    type="button"
                    onClick={() => {
                      setSelectedTagIds([]);
                      setShowAddTagModal(true);
                    }}
                    className="mt-2 text-xs font-semibold text-[#101B82] hover:underline"
                  >
                    + Assign tags now
                  </button>
                )}
              </div>
            ) : (
              <div className="flex flex-wrap gap-2">
                {assignedTags.map((tag) => (
                  <div
                    key={tag.id}
                    className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl border text-xs font-medium shadow-2xs ${getTagTypeBadgeClass(
                      tag.tagType
                    )}`}
                  >
                    <span className="font-semibold">{tag.name}</span>
                    {tag.tagType && (
                      <span className="text-[10px] uppercase font-bold opacity-70 border-l border-current/20 pl-1.5">
                        {tag.tagType.replace("_", " ")}
                      </span>
                    )}

                    {canManage && onRemoveTag && (
                      <button
                        type="button"
                        onClick={() => onRemoveTag(tag.id)}
                        disabled={loading || submittingAction}
                        title={`Remove ${tag.name}`}
                        className="ml-1 -mr-1 flex h-4 w-4 items-center justify-center rounded-full hover:bg-black/10 transition"
                      >
                        <X size={11} weight="bold" />
                      </button>
                    )}
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* MODAL: ASSIGN AMENITIES */}
      {showAddAmenityModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-xs p-4 animate-in fade-in duration-150">
          <div className="w-full max-w-lg rounded-2xl bg-white p-6 shadow-xl space-y-4">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <div className="flex items-center gap-2">
                <Sparkle size={20} className="text-amber-500" weight="fill" />
                <h4 className="text-base font-bold text-slate-900 font-serif-title">
                  Assign Amenities to {property?.name}
                </h4>
              </div>
              <button
                type="button"
                onClick={() => setShowAddAmenityModal(false)}
                className="text-slate-400 hover:text-slate-600 rounded-lg p-1"
              >
                <X size={18} />
              </button>
            </div>

            <p className="text-xs text-slate-500">
              Select one or more amenities to attach to this property:
            </p>

            <div className="max-h-64 overflow-y-auto space-y-1.5 pr-1">
              {availableAmenities.length === 0 ? (
                <div className="py-6 text-center text-xs text-slate-400 italic">
                  All available system amenities have already been assigned to this property.
                </div>
              ) : (
                availableAmenities.map((a) => {
                  const id = a.value || a.id;
                  const isChecked = selectedAmenityIds.includes(id);
                  return (
                    <button
                      key={id}
                      type="button"
                      onClick={() => handleToggleAmenity(id)}
                      className={`w-full flex items-center justify-between p-3 rounded-xl border text-left transition ${
                        isChecked
                          ? "border-[#101B82] bg-[#101B82]/5"
                          : "border-slate-200 hover:border-slate-300 bg-white"
                      }`}
                    >
                      <div>
                        <span className="block text-xs font-bold text-slate-800">
                          {a.label || a.name}
                        </span>
                        {a.description && (
                          <span className="block text-[11px] text-slate-400">
                            {a.description}
                          </span>
                        )}
                      </div>
                      <div
                        className={`flex h-5 w-5 items-center justify-center rounded border ${
                          isChecked
                            ? "bg-[#101B82] border-[#101B82] text-white"
                            : "border-slate-300 bg-white"
                        }`}
                      >
                        {isChecked && <Check size={12} weight="bold" />}
                      </div>
                    </button>
                  );
                })
              )}
            </div>

            <div className="flex items-center justify-end gap-2.5 pt-3 border-t border-slate-100">
              <button
                type="button"
                onClick={() => setShowAddAmenityModal(false)}
                className="px-4 py-2 text-xs font-semibold text-slate-600 hover:bg-slate-100 rounded-xl transition"
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={submitAddAmenities}
                disabled={submittingAction || selectedAmenityIds.length === 0}
                className="inline-flex items-center gap-1.5 px-4 py-2 rounded-xl text-xs font-semibold bg-[#101B82] text-white hover:bg-[#0c1566] transition disabled:opacity-50"
              >
                <span>{submittingAction ? "Assigning..." : `Assign (${selectedAmenityIds.length})`}</span>
              </button>
            </div>
          </div>
        </div>
      )}

      {/* MODAL: ASSIGN TAGS */}
      {showAddTagModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-xs p-4 animate-in fade-in duration-150">
          <div className="w-full max-w-lg rounded-2xl bg-white p-6 shadow-xl space-y-4">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <div className="flex items-center gap-2">
                <Bookmarks size={20} className="text-[#101B82]" weight="fill" />
                <h4 className="text-base font-bold text-slate-900 font-serif-title">
                  Assign Tags to {property?.name}
                </h4>
              </div>
              <button
                type="button"
                onClick={() => setShowAddTagModal(false)}
                className="text-slate-400 hover:text-slate-600 rounded-lg p-1"
              >
                <X size={18} />
              </button>
            </div>

            <p className="text-xs text-slate-500">
              Select one or more tags to classify and highlight this property:
            </p>

            <div className="max-h-64 overflow-y-auto space-y-1.5 pr-1">
              {availableTags.length === 0 ? (
                <div className="py-6 text-center text-xs text-slate-400 italic">
                  All available system tags have already been assigned to this property.
                </div>
              ) : (
                availableTags.map((t) => {
                  const id = t.value || t.id;
                  const isChecked = selectedTagIds.includes(id);
                  return (
                    <button
                      key={id}
                      type="button"
                      onClick={() => handleToggleTag(id)}
                      className={`w-full flex items-center justify-between p-3 rounded-xl border text-left transition ${
                        isChecked
                          ? "border-[#101B82] bg-[#101B82]/5"
                          : "border-slate-200 hover:border-slate-300 bg-white"
                      }`}
                    >
                      <div>
                        <span className="block text-xs font-bold text-slate-800">
                          {t.label || t.name}
                        </span>
                        {t.description && (
                          <span className="block text-[11px] text-slate-400">
                            {t.description}
                          </span>
                        )}
                      </div>
                      <div
                        className={`flex h-5 w-5 items-center justify-center rounded border ${
                          isChecked
                            ? "bg-[#101B82] border-[#101B82] text-white"
                            : "border-slate-300 bg-white"
                        }`}
                      >
                        {isChecked && <Check size={12} weight="bold" />}
                      </div>
                    </button>
                  );
                })
              )}
            </div>

            <div className="flex items-center justify-end gap-2.5 pt-3 border-t border-slate-100">
              <button
                type="button"
                onClick={() => setShowAddTagModal(false)}
                className="px-4 py-2 text-xs font-semibold text-slate-600 hover:bg-slate-100 rounded-xl transition"
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={submitAddTags}
                disabled={submittingAction || selectedTagIds.length === 0}
                className="inline-flex items-center gap-1.5 px-4 py-2 rounded-xl text-xs font-semibold bg-[#101B82] text-white hover:bg-[#0c1566] transition disabled:opacity-50"
              >
                <span>{submittingAction ? "Assigning..." : `Assign (${selectedTagIds.length})`}</span>
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default PropertyAmenitiesTagsSection;
