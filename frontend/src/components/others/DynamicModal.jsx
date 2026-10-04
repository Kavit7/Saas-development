import { useEffect } from "react";
import { X, CheckCircle, WarningCircle, Info, Warning } from "@phosphor-icons/react";

const alertStyles = {
  error: {
    icon: WarningCircle,
    color: "text-red-600 bg-red-50 border-red-200",
    button: "bg-red-600 hover:bg-red-700",
  },
  success: {
    icon: CheckCircle,
    color: "text-emerald-600 bg-emerald-50 border-emerald-200",
    button: "bg-emerald-600 hover:bg-emerald-700",
  },
  warning: {
    icon: Warning,
    color: "text-amber-600 bg-amber-50 border-amber-200",
    button: "bg-amber-600 hover:bg-amber-700",
  },
  info: {
    icon: Info,
    color: "text-[#101B82] bg-blue-50 border-blue-200",
    button: "bg-[#101B82] hover:bg-[#0d176f]",
  },
};

const DynamicModal = ({
  open,
  title,
  message,
  type = "info",
  closeLabel = "Close",
  onClose,
  children,
  maxWidth = "max-w-2xl",
}) => {
  useEffect(() => {
    if (!open) return undefined;

    const handleKeyDown = (event) => {
      if (event.key === "Escape") onClose?.();
    };

    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [open, onClose]);

  if (!open) return null;

  const isAlert = !children && message;
  const style = alertStyles[type] || alertStyles.info;
  const IconComponent = style.icon;

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center overflow-y-auto bg-slate-950/60 p-4 backdrop-blur-sm transition-all duration-200"
      onMouseDown={(e) => {
        if (e.target === e.currentTarget) onClose?.();
      }}
      role="presentation"
    >
      <div
        aria-modal="true"
        role="dialog"
        className={`relative w-full ${maxWidth} rounded-2xl bg-white shadow-2xl ring-1 ring-black/5 border border-slate-100 overflow-hidden my-8 transition-all`}
      >
        {/* Top Accent Gradient */}
        <div className="h-1 w-full bg-gradient-to-r from-[#101B82] via-indigo-600 to-blue-500" />

        {isAlert ? (
          /* Alert / Notification View */
          <div className="p-6 sm:p-8">
            <div className="flex items-start gap-4">
              <div
                className={`flex h-12 w-12 shrink-0 items-center justify-center rounded-2xl border shadow-xs ${style.color}`}
              >
                <IconComponent size={28} weight="fill" />
              </div>
              <div className="flex-1 pt-0.5">
                <h3 className="text-lg font-bold text-slate-900 tracking-tight">{title}</h3>
                <p className="mt-2 text-sm text-slate-600 leading-relaxed whitespace-pre-wrap">
                  {message}
                </p>
              </div>
            </div>
            <div className="mt-6 flex justify-end">
              <button
                type="button"
                onClick={onClose}
                autoFocus
                className={`rounded-xl px-5 py-2.5 text-sm font-semibold text-white shadow-sm hover:shadow transition active:scale-[0.98] ${style.button}`}
              >
                {closeLabel}
              </button>
            </div>
          </div>
        ) : (
          /* Form / Content View */
          <div className="flex flex-col max-h-[85vh]">
            {/* Modal Header */}
            <div className="flex items-center justify-between border-b border-slate-100 px-6 py-4 bg-white">
              <div className="space-y-0.5">
                <h3 className="text-lg font-bold text-slate-900 tracking-tight">
                  {title}
                </h3>
              </div>
              <button
                type="button"
                onClick={onClose}
                aria-label="Close"
                className="flex h-8 w-8 items-center justify-center rounded-full text-slate-400 transition hover:bg-slate-100 hover:text-slate-700"
              >
                <X size={18} weight="bold" />
              </button>
            </div>

            {/* Modal Body */}
            <div className="flex-1 overflow-y-auto px-6 py-5">
              {children}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default DynamicModal;
