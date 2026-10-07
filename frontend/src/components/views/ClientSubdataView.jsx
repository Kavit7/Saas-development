import { useState } from "react";
import { Users, Airplane, Plus, FirstAid } from "@phosphor-icons/react";
import GuestRequirementsModal from "../guests/GuestRequirementsModal";

const ClientSubdataView = ({
  activeTab, // "guests" | "flights"
  client,
  guests = [],
  flights = [],
  canCreate = false,
  canManage = true,
  onAddGuest,
  onAddFlight,
}) => {
  const [selectedGuestForReqs, setSelectedGuestForReqs] = useState(null);
  const [reqsModalOpen, setReqsModalOpen] = useState(false);

  if (activeTab === "guests") {
    return (
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <h3 className="text-sm font-bold text-slate-800 font-serif-title">
            Registered Guests ({guests.length})
          </h3>
          {canCreate && (
            <button
              type="button"
              onClick={onAddGuest}
              className="inline-flex items-center gap-1.5 rounded-xl bg-[#264624] px-3.5 py-2 text-xs font-semibold text-white hover:bg-[#1b331a] transition shadow-2xs"
            >
              <Plus size={14} weight="bold" />
              <span>Add Guest</span>
            </button>
          )}
        </div>

        {guests.length === 0 ? (
          <div className="rounded-2xl border border-slate-200/90 bg-white p-12 text-center shadow-xs">
            <Users size={36} className="mx-auto text-slate-300 mb-2" />
            <p className="text-sm font-semibold text-slate-700 font-serif-title">
              No guests registered for this client yet.
            </p>
            <p className="text-xs text-slate-400 mt-1">
              Click the button above to add the first guest record.
            </p>
          </div>
        ) : (
          <div className="overflow-hidden rounded-2xl border border-slate-200/90 bg-white shadow-xs">
            <table className="w-full text-left text-xs text-slate-700">
              <thead className="bg-slate-50/80 border-b border-slate-200/80">
                <tr>
                  <th className="px-4 py-3 font-semibold uppercase tracking-wider text-slate-500">Name</th>
                  <th className="px-4 py-3 font-semibold uppercase tracking-wider text-slate-500">Gender</th>
                  <th className="px-4 py-3 font-semibold uppercase tracking-wider text-slate-500">Nationality</th>
                  <th className="px-4 py-3 font-semibold uppercase tracking-wider text-slate-500">Passport Number</th>
                  <th className="px-4 py-3 font-semibold uppercase tracking-wider text-slate-500">Passport Expiry</th>
                  <th className="px-4 py-3 font-semibold uppercase tracking-wider text-slate-500">Date of Birth</th>
                  <th className="px-4 py-3 font-semibold uppercase tracking-wider text-slate-500 text-right">Special Requirements</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {guests.map((g) => (
                  <tr key={g.id} className="hover:bg-slate-50/60 transition-colors">
                    <td className="px-4 py-3 font-semibold text-slate-900">
                      {g.firstName} {g.lastName}
                    </td>
                    <td className="px-4 py-3">{g.gender || "—"}</td>
                    <td className="px-4 py-3">{g.nationality || "—"}</td>
                    <td className="px-4 py-3 font-mono">{g.passportNumber || "—"}</td>
                    <td className="px-4 py-3">{g.passportExpiry || "—"}</td>
                    <td className="px-4 py-3">{g.dateOfBirth || "—"}</td>
                    <td className="px-4 py-3 text-right">
                      <button
                        type="button"
                        onClick={() => {
                          setSelectedGuestForReqs(g);
                          setReqsModalOpen(true);
                        }}
                        className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl border border-[#264624]/20 bg-[#264624]/10 text-[#264624] text-xs font-bold hover:bg-[#264624]/15 transition active:scale-95 shadow-2xs"
                      >
                        <FirstAid size={14} weight="duotone" />
                        <span>Requirements</span>
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        <GuestRequirementsModal
          open={reqsModalOpen}
          onClose={() => {
            setReqsModalOpen(false);
            setSelectedGuestForReqs(null);
          }}
          guest={selectedGuestForReqs}
          canManage={canManage}
        />
      </div>
    );
  }

  const formatDateTime = (val) => {
    if (!val) return "—";
    try {
      const d = new Date(val);
      if (isNaN(d.getTime())) return String(val).slice(0, 16).replace("T", " ");
      return d.toLocaleString([], {
        year: "numeric",
        month: "short",
        day: "numeric",
        hour: "2-digit",
        minute: "2-digit",
      });
    } catch {
      return String(val).slice(0, 16).replace("T", " ");
    }
  };

  // Flights view
  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h3 className="text-sm font-bold text-slate-800 font-serif-title">
          Flight Schedules ({flights.length})
        </h3>
        {canCreate && (
          <button
            type="button"
            onClick={onAddFlight}
            className="inline-flex items-center gap-1.5 rounded-xl bg-[#264624] px-3.5 py-2 text-xs font-semibold text-white hover:bg-[#1b331a] transition shadow-2xs"
          >
            <Plus size={14} weight="bold" />
            <span>Add Flight</span>
          </button>
        )}
      </div>

      {flights.length === 0 ? (
        <div className="rounded-2xl border border-slate-200/90 bg-white p-12 text-center shadow-xs">
          <Airplane size={36} className="mx-auto text-slate-300 mb-2" />
          <p className="text-sm font-semibold text-slate-700 font-serif-title">
            No flight records registered for this client.
          </p>
          <p className="text-xs text-slate-400 mt-1">
            Click the button above to register an arrival, departure, or internal flight.
          </p>
        </div>
      ) : (
        <div className="overflow-hidden rounded-2xl border border-slate-200/90 bg-white shadow-xs">
          <table className="w-full text-left text-xs text-slate-700">
            <thead className="bg-slate-50/80 border-b border-slate-200/80">
              <tr>
                <th className="px-4 py-3 font-semibold uppercase tracking-wider text-slate-500">Type</th>
                <th className="px-4 py-3 font-semibold uppercase tracking-wider text-slate-500">Airline</th>
                <th className="px-4 py-3 font-semibold uppercase tracking-wider text-slate-500">Flight No</th>
                <th className="px-4 py-3 font-semibold uppercase tracking-wider text-slate-500">Airport</th>
                <th className="px-4 py-3 font-semibold uppercase tracking-wider text-slate-500">Arrival</th>
                <th className="px-4 py-3 font-semibold uppercase tracking-wider text-slate-500">Departure</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {flights.map((f) => (
                <tr key={f.id} className="hover:bg-slate-50/60 transition-colors">
                  <td className="px-4 py-3">
                    <span
                      className={`inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold uppercase tracking-wider border ${
                        f.flightType === "ARRIVAL"
                          ? "bg-emerald-50 text-emerald-700 border-emerald-200"
                          : f.flightType === "DEPARTURE"
                          ? "bg-blue-50 text-blue-700 border-blue-200"
                          : "bg-[#264624]/10 text-[#264624] border-[#264624]/20"
                      }`}
                    >
                      {f.flightType}
                    </span>
                  </td>
                  <td className="px-4 py-3 font-semibold text-slate-900">{f.airline}</td>
                  <td className="px-4 py-3 font-mono font-medium">{f.flightNumber}</td>
                  <td className="px-4 py-3">{f.airport}</td>
                  <td className="px-4 py-3">{formatDateTime(f.arrivalDatetime)}</td>
                  <td className="px-4 py-3">{formatDateTime(f.departureDatetime)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};

export default ClientSubdataView;
