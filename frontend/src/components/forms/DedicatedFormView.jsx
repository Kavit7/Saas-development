import { ArrowLeft, Plus, PencilSimple } from "@phosphor-icons/react";
import DynamicForm from "./DynamicForm";

const DedicatedFormView = ({
  title,
  resourceTitle,
  singular,
  mode = "create",
  fields = [],
  initialValues = {},
  onSubmit,
  onCancel,
  loading = false,
}) => {
  const entityName = singular || resourceTitle?.slice(0, -1) || "Record";
  return (
    <div className="space-y-5 animate-in fade-in duration-200">
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
            <span className="text-slate-400">{resourceTitle}</span>
            <span className="text-slate-300">/</span>
            <span className="text-slate-900 font-semibold">{title}</span>
          </div>
        </div>
      </div>

      {/* Main Form Container */}
      <div className="relative overflow-hidden rounded-2xl border border-slate-200/90 bg-white p-6 sm:p-8 shadow-xs">
        <div className="absolute top-0 inset-x-0 h-1.5 bg-gradient-to-r from-[#264624] via-[#7A5229] to-[#B8860B]" />

        <div className="mb-6 border-b border-slate-100 pb-5">
          <div className="flex items-center gap-3">
            <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-[#264624]/10 text-[#264624] border border-[#264624]/20">
              {mode === "create" ? (
                <Plus size={22} weight="bold" />
              ) : (
                <PencilSimple size={22} weight="bold" />
              )}
            </div>
            <div>
              <h2 className="text-xl sm:text-2xl font-bold tracking-tight text-slate-900 font-serif-title">
                {title}
              </h2>
              <p className="text-xs sm:text-sm text-slate-500 mt-0.5">
                {mode === "create"
                  ? `Fill out the required information to create a new ${entityName.toLowerCase()}.`
                  : `Update the fields below to modify this ${entityName.toLowerCase()}.`}
              </p>
            </div>
          </div>
        </div>

        <DynamicForm
          fields={fields}
          initialValues={initialValues}
          onSubmit={onSubmit}
          onCancel={onCancel}
          submitLabel={mode === "create" ? `Create ${entityName}` : "Save Changes"}
          loading={loading}
        />
      </div>
    </div>
  );
};

export default DedicatedFormView;
