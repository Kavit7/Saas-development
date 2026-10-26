import { decorations } from "../../config/decorations";
import { useAuth } from "../../hooks/useAuth";

const DynamicTable = ({
  title,
  fields = [],
  items = [],
  actions = [],
  permissions = {},
  onAction = () => {},
  sortField,
  sortDirection,
  onSort,
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
        return "bg-[#35634B] text-white hover:bg-[#284C39]";
      case "update":
        return "border border-slate-200 bg-white text-slate-700 hover:bg-slate-50";
      case "delete":
        return "border border-rose-200 bg-white text-rose-700 hover:bg-rose-50";
      default:
        return "border border-slate-200 bg-white text-slate-700 hover:bg-slate-50";
    }
  };

  const renderCell = (value, field) => {
    if (value === null || value === undefined || value === "") {
      return <span className="text-slate-400 font-normal">—</span>;
    }

    if (!field?.decorate) {
      return <span className="text-[13px] text-slate-700">{String(value)}</span>;
    }

    const style = decorations[field.decorate]?.[value];

    if (!style) {
      return <span className="text-[13px] text-slate-700">{String(value)}</span>;
    }

    return (
      <span className={`inline-flex items-center rounded-md border px-2 py-1 text-[11px] font-medium ${style}`}>
        {value}
      </span>
    );
  };

  return (
    <div className="w-full overflow-hidden rounded-xl border border-[#28332C]/10 bg-white">
      {title && (
        <div className="border-b border-[#28332C]/10 px-5 py-4">
          <h2 className="text-base font-semibold tracking-tight text-[#28332C]">
            {title}
          </h2>
        </div>
      )}

      <div className="overflow-x-auto">
        <table className="min-w-[720px] w-full border-collapse text-left text-sm font-sans">
          <thead className="border-b border-[#28332C]/10 bg-[#F7F8F6]">
            <tr>
              {safeFields.map((field) => {
                const isSorted = sortField === field.name || (field.name === "firstName" && sortField === "name");
                return (
                  <th
                    key={field.name}
                    onClick={() => onSort && onSort(field.name)}
                    className={`whitespace-nowrap px-4 py-3 text-left text-[11px] font-medium text-slate-500 ${
                      onSort ? "cursor-pointer hover:text-slate-800 transition select-none" : ""
                    }`}
                  >
                    <div className="flex items-center gap-1.5">
                      <span>{field.label}</span>
                      {isSorted && (
                        <span className="text-[#35634B] font-semibold text-xs">
                          {sortDirection?.toLowerCase() === "desc" ? "↓" : "↑"}
                        </span>
                      )}
                    </div>
                  </th>
                );
              })}
              {visibleActions.length > 0 && (
                <th className="whitespace-nowrap px-4 py-3 text-left text-[11px] font-medium text-slate-500">
                  Actions
                </th>
              )}
            </tr>
          </thead>

          <tbody className="divide-y divide-[#28332C]/[0.06]">
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
                  className="transition-colors hover:bg-[#F8FAF8]"
                >
                  {safeFields.map((field) => (
                    <td
                      key={`${item.id ?? item._id ?? JSON.stringify(item)}-${field.name}`}
                      className="px-4 py-3 align-middle text-slate-700"
                    >
                      {renderCell(item[field.name], field)}
                    </td>
                  ))}

                  {visibleActions.length > 0 && (
                    <td className="px-4 py-3 align-middle">
                      <div className="flex flex-wrap items-center gap-1">
                        {visibleActions.map((action) => (
                          <button
                            key={action.key}
                            type="button"
                            onClick={() => onAction(action.key, item)}
                            className={`rounded-md px-2.5 py-1.5 text-xs font-medium transition focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#35634B] ${getActionClass(action.key)}`}
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
