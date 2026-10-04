import { decorations } from "../../config/decorations";
import { useAuth } from "../../hooks/useAuth";

const DynamicTable = ({
  title,
  fields = [],
  items = [],
  actions = [],
  permissions = {},
  onAction = () => {},
}) => {
  const { can } = useAuth();
  const safeFields = Array.isArray(fields)
    ? fields.filter((field) => field.showInTable !== false)
    : [];
  const safeItems = Array.isArray(items) ? items : [];
  const safeActions = Array.isArray(actions) ? actions : [];
  const visibleActions = safeActions.filter((action) =>
    can(action.key, permissions),
  );

  const getActionClass = (actionKey) => {
    switch (actionKey) {
      case "view":
        return "bg-[#101B82] text-white hover:bg-[#0d176f] shadow-2xs";
      case "update":
        return "bg-slate-100 text-slate-700 hover:bg-slate-200 border border-slate-200";
      case "delete":
        return "bg-rose-50 text-rose-600 hover:bg-rose-100 border border-rose-200";
      default:
        return "bg-slate-100 text-slate-700 hover:bg-slate-200";
    }
  };

  const renderCell = (value, field) => {
    if (value === null || value === undefined || value === "") {
      return <span className="text-slate-400 font-normal">—</span>;
    }

    if (!field?.decorate) {
      return <span className="text-slate-800 font-medium text-[13px]">{String(value)}</span>;
    }

    const style = decorations[field.decorate]?.[value];

    if (!style) {
      return <span className="text-slate-800 font-medium text-[13px]">{String(value)}</span>;
    }

    return (
      <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-[11px] font-semibold tracking-wide border shadow-2xs ${style}`}>
        {value}
      </span>
    );
  };

  return (
    <div className="w-full overflow-hidden rounded-2xl border border-slate-200/90 bg-white shadow-xs">
      {title && (
        <div className="border-b border-slate-100 px-5 py-4">
          <h2 className="text-lg font-bold text-[#101B82] tracking-tight">
            {title}
          </h2>
        </div>
      )}

      <div className="overflow-x-auto">
        <table className="min-w-[720px] w-full border-collapse text-left text-sm font-sans">
          <thead className="bg-slate-50/75 border-b border-slate-200/80">
            <tr>
              {safeFields.map((field) => (
                <th
                  key={field.name}
                  className="whitespace-nowrap px-4 py-3.5 text-left text-xs font-semibold uppercase tracking-wider text-slate-500"
                >
                  {field.label}
                </th>
              ))}
              {visibleActions.length > 0 && (
                <th className="whitespace-nowrap px-4 py-3.5 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">
                  Actions
                </th>
              )}
            </tr>
          </thead>

          <tbody className="divide-y divide-slate-100">
            {safeItems.length === 0 ? (
              <tr>
                <td
                  colSpan={
                    (safeFields.length || 1) +
                    (visibleActions.length > 0 ? 1 : 0)
                  }
                  className="px-4 py-12 text-center text-sm text-slate-500"
                >
                  No records available
                </td>
              </tr>
            ) : (
              safeItems.map((item) => (
                <tr
                  key={item.id ?? item._id ?? JSON.stringify(item)}
                  className="hover:bg-slate-50/60 transition-colors"
                >
                  {safeFields.map((field) => (
                    <td
                      key={`${item.id ?? item._id ?? JSON.stringify(item)}-${field.name}`}
                      className="px-4 py-3.5 align-middle text-slate-700"
                    >
                      {renderCell(item[field.name], field)}
                    </td>
                  ))}

                  {visibleActions.length > 0 && (
                    <td className="px-4 py-3.5 align-middle">
                      <div className="flex flex-wrap items-center gap-1.5">
                        {visibleActions.map((action) => (
                          <button
                            key={action.key}
                            type="button"
                            onClick={() => onAction(action.key, item)}
                            className={`rounded-lg px-3 py-1.5 text-xs font-semibold transition active:scale-95 ${getActionClass(action.key)}`}
                          >
                            {action.label || action.key}
                          </button>
                        ))}
                      </div>
                    </td>
                  )}
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default DynamicTable;
