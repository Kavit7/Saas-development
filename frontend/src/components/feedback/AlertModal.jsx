import { useEffect } from "react";
import { CheckCircle, WarningCircle, Info, Warning, X } from "@phosphor-icons/react";

const alertStyles = {
  error: {
    icon: WarningCircle,
    color: "text-rose-600 bg-rose-50 border-rose-200",
    button: "bg-rose-600 hover:bg-rose-700",
  },
  success: {
    icon: CheckCircle,
    color: "text-emerald-700 bg-emerald-50 border-emerald-200",
    button: "bg-emerald-700 hover:bg-emerald-800",
  },
  warning: {
    icon: Warning,
    color: "text-amber-700 bg-amber-50 border-amber-200",
    button: "bg-amber-700 hover:bg-amber-800",
  },
  info: {
    icon: Info,
    color: "text-[#101B82] bg-blue-50 border-blue-200",
    button: "bg-[#101B82] hover:bg-[#0d176f]",
  },
};

const AlertModal = ({
  open,
  title,
  message,
  type = "info",
  closeLabel = "Dismiss",
  onClose,
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

  const style = alertStyles[type] || alertStyles.info;
  const IconComponent = style.icon;

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center overflow-y-auto bg-slate-950/50 p-4 backdrop-blur-sm transition-all duration-200"
      onMouseDown={(e) => {
        if (e.target === e.currentTarget) onClose?.();
      }}
      role="presentation"
    >
      <div
        aria-modal="true"
        role="dialog"
        className="relative w-full max-w-md rounded-2xl bg-white shadow-2xl ring-1 ring-black/5 border border-slate-100 overflow-hidden my-8 transition-all animate-in fade-in zoom-in-95 duration-150"
      >
        {/* Accent Bar */}
        <div
          className={`h-1.5 w-full ${
            type === "success"
              ? "bg-emerald-600"
              : type === "error"
              ? "bg-rose-600"
              : type === "warning"
              ? "bg-amber-500"
              : "bg-[#101B82]"
          }`}
        />

        <div className="p-6">
          <div className="flex items-start gap-4">
            <div
              className={`flex h-12 w-12 shrink-0 items-center justify-center rounded-2xl border shadow-2xs ${style.color}`}
            >
              <IconComponent size={26} weight="duotone" />
            </div>

            <div className="flex-1 pt-0.5">
              <div className="flex items-center justify-between">
                <h3 className="text-lg font-bold text-slate-900 tracking-tight font-serif-title">
                  {title}
                </h3>
                <button
                  type="button"
                  onClick={onClose}
                  className="rounded-lg p-1 text-slate-400 hover:bg-slate-100 hover:text-slate-600 transition"
                >
                  <X size={16} />
                </button>
              </div>

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
              className={`rounded-xl px-5 py-2.5 text-xs font-semibold uppercase tracking-wider text-white shadow-sm hover:shadow transition active:scale-[0.98] ${style.button}`}
            >
              {closeLabel}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AlertModal;
