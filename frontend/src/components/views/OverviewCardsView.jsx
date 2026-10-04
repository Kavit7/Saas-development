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

const OverviewCardsView = ({ fields = [], item = {} }) => {
  const nonDynamicFields = fields.filter((f) => !f.dynamicOptions);

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3.5 font-sans">
      {nonDynamicFields.map((f) => {
        const val = item[f.name];
        const displayVal =
          val !== null && val !== undefined && val !== "" ? String(val) : "—";
        const isStatusField = f.name === "status" || f.name === "verificationStatus";

        return (
          <div
            key={f.name}
            className="rounded-2xl border border-slate-200/80 bg-white p-4 shadow-2xs hover:border-slate-300 transition"
          >
            <span className="block text-[11px] font-bold uppercase tracking-wider text-slate-400">
              {f.label}
            </span>
            <div className="mt-1.5">
              {isStatusField && val ? (
                <span
                  className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold uppercase tracking-wider border shadow-2xs ${getStatusBadgeClass(
                    val
                  )}`}
                >
                  {val}
                </span>
              ) : (
                <span className="text-sm font-semibold text-slate-900 break-words font-serif-title">
                  {displayVal}
                </span>
              )}
            </div>
          </div>
        );
      })}
    </div>
  );
};

export default OverviewCardsView;
