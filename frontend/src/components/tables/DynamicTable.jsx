import React from "react";
import { decorations } from "../../config/decorations";
import { useAuth } from "../../context/AuthProvider";

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
        return "bg-[#101B82] text-white hover:bg-[#0d1b70]";
      case "update":
        return "bg-amber-500 text-white hover:bg-amber-600";
      case "delete":
        return "bg-red-500 text-white hover:bg-red-600";
      default:
        return "bg-slate-200 text-slate-700 hover:bg-slate-300";
    }
  };

  const renderCell = (value, field) => {
    if (!field?.decorate) {
      return value ?? "—";
    }

    const style = decorations[field.decorate]?.[value];

    if (!style) {
      return value ?? "—";
    }

    return (
      <span className={`rounded-full px-2 py-1 ${style} lowercase`}>
        {value ?? "—"}
      </span>
    );
  };

  return (
    <div className="w-full overflow-hidden rounded-[18px] border border-[#211917]/10 bg-white shadow-sm">
      {title && (
        <div className="border-b border-[#211917]/10 px-4 py-4 sm:px-6">
          <h2 className="text-lg font-bold text-[#101B82] sm:text-xl">
            {title}
          </h2>
        </div>
      )}

      <div className="overflow-x-auto">
        <table className="min-w-[720px] w-full border-collapse text-left text-sm text-[#211917]">
          <thead className="bg-[#101B82]/5">
            <tr>
              {safeFields.map((field) => (
                <th
                  key={field.name}
                  className="whitespace-nowrap border-b border-[#211917]/10 px-3 py-3 text-left text-xs font-bold uppercase tracking-[0.08em] text-[#211917]/70 sm:px-4"
                >
                  {field.label}
                </th>
              ))}
              {visibleActions.length > 0 && (
                <th className="whitespace-nowrap border-b border-[#211917]/10 px-3 py-3 text-left text-xs font-bold uppercase tracking-[0.08em] text-[#211917]/70 sm:px-4">
                  Actions
                </th>
              )}
            </tr>
          </thead>

          <tbody>
            {safeItems.length === 0 ? (
              <tr>
                <td
                  colSpan={
                    (safeFields.length || 1) +
                    (visibleActions.length > 0 ? 1 : 0)
                  }
                  className="px-3 py-8 text-center text-sm text-[#211917]/60 sm:px-4"
                >
                  No records available
                </td>
              </tr>
            ) : (
              safeItems.map((item, index) => (
                <tr key={index} className="transition hover:bg-[#101B82]/5">
                  {safeFields.map((field) => (
                    <td
                      key={field.name}
                      className="border-b border-[#211917]/5 px-3 py-3 align-top text-sm text-[#211917] sm:px-4"
                    >
                      {renderCell(item?.[field.name], field)}
                    </td>
                  ))}
                  {visibleActions.length > 0 && (
                    <td className="border-b border-[#211917]/5 px-3 py-3 align-top sm:px-4">
                      <div className="flex flex-wrap gap-2">
                        {visibleActions.map((action) => (
                          <button
                            key={action.key}
                            type="button"
                            onClick={() => onAction(action.key, item)}
                            className={`rounded-md px-2.5 py-1.5 text-xs font-semibold shadow-sm transition ${getActionClass(action.key)}`}
                          >
                            {action.label}
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
