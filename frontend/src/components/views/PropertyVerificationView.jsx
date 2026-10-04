import { ShieldCheck } from "@phosphor-icons/react";

const getStatusBadgeClass = (status) => {
  const s = String(status || "").toUpperCase();
  if (s === "VERIFIED" || s === "ACTIVE") {
    return "bg-emerald-50 text-emerald-700 border-emerald-200/90";
  }
  if (s === "PENDING") {
    return "bg-amber-50 text-amber-700 border-amber-200/90";
  }
  return "bg-rose-50 text-rose-700 border-rose-200/90";
};

const PropertyVerificationView = ({
  property,
  canVerify = false,
  loading = false,
  onVerify,
}) => {
  const isVerified = property?.verificationStatus === "VERIFIED";

  return (
    <div className="rounded-2xl border border-slate-200/90 bg-white p-5 sm:p-6 shadow-xs flex flex-col sm:flex-row sm:items-center justify-between gap-4 font-sans">
      <div className="flex items-start gap-4">
        <div
          className={`flex h-12 w-12 shrink-0 items-center justify-center rounded-2xl border ${
            isVerified
              ? "bg-emerald-50 text-emerald-600 border-emerald-200"
              : "bg-amber-50 text-amber-600 border-amber-200"
          }`}
        >
          <ShieldCheck size={26} weight="duotone" />
        </div>

        <div>
          <div className="flex flex-wrap items-center gap-2.5">
            <h4 className="text-base font-bold text-slate-900 font-serif-title">
              Verification Compliance
            </h4>
            <span
              className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold uppercase tracking-wider border ${getStatusBadgeClass(
                property?.verificationStatus
              )}`}
            >
              {property?.verificationStatus || "PENDING"}
            </span>
          </div>

          <p className="mt-1.5 text-xs text-slate-500 leading-relaxed max-w-xl">
            {isVerified
              ? `This property is verified and eligible for safari accommodation bookings. Verified by ${
                  property.verifiedBy || "Manager"
                } on ${property.verifiedAt || "creation"}.`
              : "This property is currently awaiting administrative or managerial verification before it is activated for bookings."}
          </p>
        </div>
      </div>

      {!isVerified && canVerify && (
        <button
          type="button"
          disabled={loading}
          onClick={() => onVerify(property.id)}
          className="inline-flex items-center gap-2 shrink-0 rounded-xl bg-emerald-700 px-5 py-2.5 text-xs font-semibold uppercase tracking-wider text-white hover:bg-emerald-800 transition active:scale-95 shadow-sm disabled:opacity-50"
        >
          <ShieldCheck size={16} weight="bold" />
          <span>{loading ? "Verifying..." : "Verify Property Now"}</span>
        </button>
      )}
    </div>
  );
};

export default PropertyVerificationView;
