import { useState, useEffect } from "react";
import { useParams } from "react-router-dom";

import DynamicTable from "../components/tables/DynamicTable";
import { useAuth } from "../hooks/useAuth";
import { getAllData } from "../api/api";

const ResourcePage = ({ title, fields, table, permissions }) => {
  const { token } = useAuth();
  const { resource } = useParams();
  const [items, setItems] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [statusFilter, setStatusFilter] = useState(
    table?.filters?.find((filter) => filter.key === "status")?.defaultValue || "",
  );
  const [page, setPage] = useState(0);
  const [pageInfo, setPageInfo] = useState({
    totalPages: 1,
    totalElements: 0,
    number: 0,
    size: 20,
  });

  useEffect(() => {
    const loadData = async () => {
      try {
        const params = new URLSearchParams({
          page: String(page),
          size: "20",
          sortBy: "createdAt",
          direction: "asc",
        });

        if (searchTerm.trim()) {
          params.set("search", searchTerm.trim());
        }

        if (statusFilter) {
          params.set("status", statusFilter);
        }

        const data = await getAllData(`/${resource}?${params.toString()}`, token);
        const result = Array.isArray(data?.content)
          ? data.content
          : Array.isArray(data)
            ? data
            : [];

        setItems(result);
        setPageInfo({
          totalPages: data?.totalPages || 1,
          totalElements: data?.totalElements || result.length,
          number: data?.number ?? page,
          size: data?.size || 20,
        });
      } catch (error) {
        console.log(error);
        setItems([]);
        setPageInfo({ totalPages: 1, totalElements: 0, number: 0, size: 20 });
      }
    };

    loadData();
  }, [resource, token, searchTerm, statusFilter, page]);

  const handleAction = (actionKey, item) => {
    console.log("Table action:", actionKey, item);
  };

  const canGoPrev = page > 0;
  const canGoNext = page + 1 < pageInfo.totalPages;

  return (
    <div className="space-y-4">
      {(table?.searchable || table?.filters?.length) && (
        <div className="flex flex-col gap-3 rounded-[16px] border border-[#211917]/10 bg-white p-3 shadow-sm sm:flex-row sm:items-center sm:justify-between">
          {table?.searchable && (
            <div className="w-full sm:max-w-xs">
              <label className="mb-1 block text-xs font-semibold uppercase tracking-[0.08em] text-[#211917]/70">
                Search
              </label>
              <input
                type="text"
                value={searchTerm}
                onChange={(event) => setSearchTerm(event.target.value)}
                placeholder="Search..."
                className="w-full rounded-xl border border-[#211917]/10 bg-[#F8F8FC] px-3 py-2 text-sm text-[#211917] outline-none transition focus:border-[#101B82]"
              />
            </div>
          )}

          {(table?.filters || []).map((filter) => (
            <div key={filter.key} className="w-full sm:max-w-[220px]">
              <label className="mb-1 block text-xs font-semibold uppercase tracking-[0.08em] text-[#211917]/70">
                {filter.label}
              </label>
              {filter.type === "select" ? (
                <select
                  value={statusFilter}
                  onChange={(event) => {
                    setStatusFilter(event.target.value);
                    setPage(0);
                  }}
                  className="w-full rounded-xl border border-[#211917]/10 bg-[#F8F8FC] px-3 py-2 text-sm text-[#211917] outline-none transition focus:border-[#101B82]"
                >
                  {(filter.options || []).map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
              ) : null}
            </div>
          ))}
        </div>
      )}

      <DynamicTable
        title={title}
        fields={fields || []}
        items={items || []}
        actions={table?.actions || []}
        permissions={permissions || {}}
        onAction={handleAction}
      />

      <div className="flex flex-col gap-3 rounded-[16px] border border-[#211917]/10 bg-white p-3 shadow-sm sm:flex-row sm:items-center sm:justify-between">
        <p className="text-sm text-[#211917]/70">
          Showing {items.length} of {pageInfo.totalElements || 0} records
        </p>

        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={() => setPage((prev) => Math.max(prev - 1, 0))}
            disabled={!canGoPrev}
            className="rounded-lg border border-[#211917]/10 px-3 py-2 text-sm font-medium text-[#211917] disabled:cursor-not-allowed disabled:opacity-40"
          >
            Previous
          </button>

          <span className="rounded-lg bg-[#101B82]/5 px-3 py-2 text-sm font-semibold text-[#101B82]">
            Page {pageInfo.number + 1} / {pageInfo.totalPages || 1}
          </span>

          <button
            type="button"
            onClick={() => setPage((prev) => prev + 1)}
            disabled={!canGoNext}
            className="rounded-lg border border-[#211917]/10 px-3 py-2 text-sm font-medium text-[#211917] disabled:cursor-not-allowed disabled:opacity-40"
          >
            Next
          </button>
        </div>
      </div>
    </div>
  );
};

export default ResourcePage;
