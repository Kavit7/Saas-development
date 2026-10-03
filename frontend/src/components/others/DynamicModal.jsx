import { useEffect } from "react";

const modalStyles = {
  error: {
    icon: "!",
    iconClass: "bg-red-100 text-red-600",
    accent: "bg-red-500",
  },
  success: {
    icon: "✓",
    iconClass: "bg-emerald-100 text-emerald-700",
    accent: "bg-emerald-500",
  },
  warning: {
    icon: "!",
    iconClass: "bg-amber-100 text-amber-700",
    accent: "bg-amber-400",
  },
  info: {
    icon: "i",
    iconClass: "bg-[#101B82]/10 text-[#101B82]",
    accent: "bg-[#101B82]",
  },
};

const DynamicModal = ({
  open,
  title = "Ujumbe",
  message,
  type = "info",
  closeLabel = "Sawa",
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

  const style = modalStyles[type] || modalStyles.info;

  return (
    <div
      className="fixed inset-0 z-[100] flex items-center justify-center bg-slate-950/50 px-4 py-6 backdrop-blur-sm"
      onMouseDown={(event) => {
        if (event.target === event.currentTarget) onClose?.();
      }}
      role="presentation"
    >
      <section
        aria-labelledby="dynamic-modal-title"
        aria-describedby="dynamic-modal-message"
        aria-modal="true"
        role="alertdialog"
        className="relative w-full max-w-md overflow-hidden rounded-2xl border border-white/70 bg-white shadow-[0_28px_90px_-28px_rgba(15,23,42,0.45)]"
      >
        <div className={`h-1 w-full ${style.accent}`} />
        <button
          type="button"
          onClick={onClose}
          aria-label="Funga ujumbe"
          className="absolute right-4 top-4 flex h-9 w-9 items-center justify-center rounded-full text-slate-400 transition hover:bg-slate-100 hover:text-slate-700 focus:outline-none focus:ring-4 focus:ring-slate-200"
        >
          <svg
            viewBox="0 0 24 24"
            fill="none"
            className="h-5 w-5"
            aria-hidden="true"
          >
            <path
              d="m6 6 12 12M18 6 6 18"
              stroke="currentColor"
              strokeWidth="1.8"
              strokeLinecap="round"
            />
          </svg>
        </button>

        <div className="px-6 pb-6 pt-7 sm:px-8 sm:pb-8">
          <div
            className={`mb-5 flex h-12 w-12 items-center justify-center rounded-2xl text-xl font-bold ${style.iconClass}`}
          >
            {style.icon}
          </div>
          <h2
            id="dynamic-modal-title"
            className="pr-8 text-xl font-bold tracking-tight text-slate-900"
          >
            {title}
          </h2>
          <p
            id="dynamic-modal-message"
            className="mt-2 whitespace-pre-wrap text-sm leading-6 text-slate-600"
          >
            {message}
          </p>
          <div className="mt-7 flex justify-end">
            <button
              type="button"
              onClick={onClose}
              autoFocus
              className="min-w-24 rounded-xl bg-[#101B82] px-5 py-2.5 text-sm font-semibold text-white shadow-sm transition hover:bg-[#0d176f] focus:outline-none focus:ring-4 focus:ring-[#101B82]/20"
            >
              {closeLabel}
            </button>
          </div>
        </div>
      </section>
    </div>
  );
};

export default DynamicModal;
