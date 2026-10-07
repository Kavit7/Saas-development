import { ArrowLeft, Users, Airplane, Compass } from "@phosphor-icons/react";
import DynamicForm from "./DynamicForm";

const SubFormView = ({
  type, // "guest" | "flight" | "itineraryDay"
  title,
  parentName = "Record",
  initialValues = {},
  onSubmit,
  onCancel,
  loading = false,
}) => {
  const getFields = () => {
    if (type === "guest") {
      return [
        { name: "firstName", label: "First Name", required: true },
        { name: "lastName", label: "Last Name", required: true },
        {
          name: "gender",
          label: "Gender",
          type: "select",
          required: true,
          options: [
            { label: "Male", value: "MALE" },
            { label: "Female", value: "FEMALE" },
          ],
        },
        { name: "nationality", label: "Nationality" },
        { name: "passportNumber", label: "Passport Number" },
        { name: "passportExpiry", label: "Passport Expiry Date", type: "date" },
        { name: "dateOfBirth", label: "Date of Birth", type: "date" },
      ];
    }

    if (type === "flight") {
      return [
        {
          name: "flightType",
          label: "Flight Type",
          type: "select",
          required: true,
          options: [
            { label: "Arrival", value: "ARRIVAL" },
            { label: "Departure", value: "DEPARTURE" },
            { label: "Internal", value: "INTERNAL" },
          ],
        },
        { name: "airline", label: "Airline", required: true },
        { name: "flightNumber", label: "Flight Number", required: true },
        { name: "airport", label: "Airport", required: true },
        { name: "arrivalDatetime", label: "Arrival Date & Time", type: "datetime-local" },
        { name: "departureDatetime", label: "Departure Date & Time", type: "datetime-local" },
      ];
    }

    if (type === "itineraryDay") {
      return [
        { name: "date", label: "Day Schedule Date", type: "date", required: true },
        { name: "destination", label: "Destination / National Park Circuit", required: true, fullWidth: true, placeholder: "e.g., Serengeti National Park (Central / Seronera)" },
        { name: "notes", label: "Scheduled Activities, Game Drives & Logistics", type: "textarea", fullWidth: true, placeholder: "e.g., Early morning game drive, picnic lunch, afternoon migration viewing, check-in at lodge" },
      ];
    }

    return [];
  };

  const getIcon = () => {
    if (type === "guest") return <Users size={24} weight="duotone" />;
    if (type === "flight") return <Airplane size={24} weight="duotone" />;
    return <Compass size={24} weight="duotone" />;
  };

  const toIsoStringOrNull = (val) => {
    if (!val) return null;
    if (typeof val === "string") {
      const trimmed = val.trim();
      if (!trimmed) return null;
      const d = new Date(trimmed);
      return isNaN(d.getTime()) ? null : d.toISOString();
    }
    if (val instanceof Date) {
      return isNaN(val.getTime()) ? null : val.toISOString();
    }
    return null;
  };

  const handleFormSubmit = (data) => {
    if (type === "flight") {
      const formatted = {
        ...data,
        arrivalDatetime: toIsoStringOrNull(data.arrivalDatetime),
        departureDatetime: toIsoStringOrNull(data.departureDatetime),
      };
      onSubmit?.(formatted);
    } else {
      onSubmit?.(data);
    }
  };

  return (
    <div className="space-y-5 animate-in fade-in duration-200 font-sans">
      {/* Top Navigation Bar */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 rounded-2xl border border-slate-200/90 bg-white p-4 shadow-xs">
        <div className="flex flex-wrap items-center gap-3">
          <button
            type="button"
            onClick={onCancel}
            className="inline-flex items-center gap-2 rounded-xl border border-slate-200 bg-slate-50/80 px-3.5 py-2 text-xs font-semibold text-slate-700 hover:border-slate-300 hover:bg-slate-100 hover:text-[#264624] transition active:scale-95 group shadow-2xs"
          >
            <ArrowLeft size={16} weight="bold" className="transition-transform group-hover:-translate-x-0.5" />
            <span>Cancel & Back</span>
          </button>
          <div className="h-5 w-px bg-slate-200 hidden sm:block" />
          <div className="flex items-center gap-1.5 text-xs text-slate-500 font-medium">
            <span className="text-slate-400">{parentName}</span>
            <span className="text-slate-300">/</span>
            <span className="text-slate-900 font-semibold">{title}</span>
          </div>
        </div>
      </div>

      {/* Main Card */}
      <div className="relative overflow-hidden rounded-2xl border border-slate-200/90 bg-white p-6 sm:p-8 shadow-xs max-w-3xl">
        <div className="absolute top-0 inset-x-0 h-1.5 bg-gradient-to-r from-[#264624] via-[#7A5229] to-[#B8860B]" />

        <div className="mb-6 border-b border-slate-100 pb-5">
          <div className="flex items-center gap-3">
            <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-[#264624]/10 border border-[#264624]/20 text-[#264624]">
              {getIcon()}
            </div>
            <div>
              <h2 className="text-xl sm:text-2xl font-bold tracking-tight text-slate-900 font-serif-title">
                {title}
              </h2>
              <p className="text-xs sm:text-sm text-slate-500 mt-0.5">
                {type === "guest"
                  ? "Record guest passport and identification details."
                  : type === "flight"
                  ? "Configure arrival, departure, or internal flight schedules."
                  : "Update itinerary day destination and scheduled activities."}
              </p>
            </div>
          </div>
        </div>

        <DynamicForm
          fields={getFields()}
          initialValues={initialValues}
          onSubmit={handleFormSubmit}
          onCancel={onCancel}
          submitLabel={
            type === "guest"
              ? "Save Guest"
              : type === "flight"
              ? "Save Flight Schedule"
              : "Save Day Destination"
          }
          loading={loading}
        />
      </div>
    </div>
  );
};

export default SubFormView;
