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
  const shortEntityName = entityName.trim().split(/\s+/).pop();

  return (
    <div className="py-4 px-2 sm:px-4 font-sans animate-in fade-in duration-200">
      {/* 1. Centered Header matching form_example.jpg */}
      <div className="text-center mb-6 max-w-xl mx-auto">
        <h1 className="text-2xl sm:text-3xl font-bold tracking-tight text-[#28332C] font-sans">
          {title || (mode === "create" ? `Add New ${entityName}` : `Edit ${entityName}`)}
        </h1>
        <p className="text-xs sm:text-sm text-slate-500 mt-1.5 leading-relaxed">
          {mode === "create"
            ? `Please fill in the information below to configure this ${entityName.toLowerCase()}.`
            : `Please update the information below for this ${entityName.toLowerCase()}.`}
        </p>
      </div>

      {/* 2. Main Form Component (Card + Shaded Header + 3-col fields + Bottom Buttons) */}
      <DynamicForm
        fields={fields}
        initialValues={initialValues}
        onSubmit={onSubmit}
        onCancel={onCancel}
        submitLabel={mode === "create" ? `Save ${shortEntityName}` : "Save Changes"}
        cancelLabel="Cancel"
        singular={shortEntityName}
        mode={mode}
        loading={loading}
      />
    </div>
  );
};

export default DedicatedFormView;
