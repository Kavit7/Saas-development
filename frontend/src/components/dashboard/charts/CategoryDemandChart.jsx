import React from "react";

/**
 * CategoryDemandChart / GoalProgressCardGrid Component
 * Visualizes goal metrics, quotas, and benchmark demand with pacing calculations.
 * Styled after the reference goal progress card design in categorial-demand.jpg,
 * utilizing the system's Safari palette (#264624, #FAF8F5, #211917) and responsive card grid.
 *
 * @param {Array<{label: string, value: number, target?: number, color?: string, unit?: string}>} items - Goal/Demand items
 * @param {string} title - Section heading
 * @param {string} subtitle - Explanatory caption
 * @param {string} badgeText - Badge text for total items (e.g. "Active Categories" or "Monthly Targets")
 */
export const CategoryDemandChart = ({
  items = [],
  title = "Lodging Category Demand",
  subtitle = "Distribution of room requirements across accommodation categories",
  badgeText,
}) => {
  const dynamicBadge =
    badgeText ||
    (title.toLowerCase().includes("quota") || title.toLowerCase().includes("target")
      ? "Monthly Targets"
      : "Active Categories");

  const gridCols =
    items.length === 3
      ? "grid-cols-1 md:grid-cols-2 lg:grid-cols-3"
      : "grid-cols-1 lg:grid-cols-2";

  return (
    <div className="rounded-3xl border border-[#211917]/10 bg-white p-6 sm:p-7 shadow-xs font-sans space-y-6">
      {/* Section Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between pb-5 border-b border-[#211917]/5 gap-3">
        <div>
          <h3 className="text-base sm:text-lg font-bold text-[#211917] font-title">
            {title}
          </h3>
          <p className="text-xs sm:text-sm text-[#211917]/60 mt-0.5">
            {subtitle}
          </p>
        </div>
        <span className="inline-flex self-start sm:self-auto items-center gap-1.5 rounded-full bg-[#264624]/10 px-3 py-1 text-xs font-semibold text-[#264624]">
          {items.length} {dynamicBadge}
        </span>
      </div>

      {/* Grid of Goal Progress Cards (matching categorial-demand.jpg) */}
      <div className={`grid ${gridCols} gap-5`}>
        {items.map((item, idx) => {
          const value = Number(item.value || 0);
          const target = Number(item.target || 100);
          const pct = Math.round((value / target) * 100);

          // Pacing determination (Benchmark target ~75%)
          const isAhead = pct >= 75;
          const paceDiff = isAhead
            ? Math.max(Math.round(pct - 70), 5)
            : Math.max(Math.round(75 - pct), 5);

          // Determine appropriate unit representation
          const unit =
            item.unit !== undefined
              ? item.unit
              : item.label.toLowerCase().includes("safari")
              ? "safaris"
              : item.label.toLowerCase().includes("client")
              ? "clients"
              : item.label.toLowerCase().includes("passenger")
              ? "pax"
              : item.label.toLowerCase().includes("night") ||
                item.label.toLowerCase().includes("camp") ||
                item.label.toLowerCase().includes("lodge") ||
                item.label.toLowerCase().includes("hotel")
              ? "nights"
              : "";

          // Geometry calculations for the layered progress bar
          let solidWidth = 0;
          let stripedLeft = 0;
          let stripedWidth = 0;
          let dotPos = 0;

          if (isAhead) {
            // Ahead of pace: Solid base fill -> Striped acceleration segment -> Dot -> Soft pastel green track
            const totalFill = Math.min(Math.max(pct, 20), 96);
            solidWidth = Math.round(totalFill * 0.65);
            stripedLeft = solidWidth;
            stripedWidth = totalFill - solidWidth;
            dotPos = totalFill;
          } else {
            // Behind pace: Solid fill -> Dot -> Striped deficit / lag gap -> Soft pastel pink track
            const currentPos = Math.min(Math.max(pct, 15), 75);
            solidWidth = currentPos;
            dotPos = currentPos;
            stripedLeft = currentPos;
            stripedWidth = Math.min(Math.max(paceDiff * 1.6, 18), 35);
          }

          return (
            <div
              key={idx}
              className="rounded-2xl border border-[#211917]/10 bg-[#FAF8F5]/50 hover:bg-[#FAF8F5] p-5 sm:p-6 transition shadow-xs flex flex-col justify-between space-y-4"
            >
              {/* Card Title & Allocation Metric */}
              <div className="flex items-center justify-between gap-3">
                <h4 className="text-sm sm:text-base font-bold text-[#211917]">
                  {item.label}
                </h4>
                <span className="text-xs font-semibold text-[#211917]/70 font-mono bg-white border border-[#211917]/10 px-2.5 py-0.5 rounded-full shrink-0 shadow-2xs">
                  {unit === "%" ? (
                    `${value}% / ${target}%`
                  ) : (
                    `${value} / ${target}${unit ? ` ${unit}` : ""} (${pct}%)`
                  )}
                </span>
              </div>

              {/* Progress Bar Container with Exact Striped & Pin Dot Visuals */}
              <div className="py-2">
                <div
                  className={`relative h-3 sm:h-3.5 w-full rounded-full transition-colors ${
                    isAhead ? "bg-[#BBF7D0]" : "bg-[#FECDD3]"
                  }`}
                >
                  {/* Solid Bar Fill */}
                  <div
                    className="absolute left-0 top-0 h-full rounded-l-full transition-all duration-500 ease-out"
                    style={{
                      width: `${solidWidth}%`,
                      backgroundColor: isAhead ? "#1B4D2E" : "#8B1D1D",
                    }}
                  />

                  {/* Striped Diagonal Pace Segment */}
                  <div
                    className="absolute top-0 h-full transition-all duration-500 ease-out"
                    style={{
                      left: `${stripedLeft}%`,
                      width: `${stripedWidth}%`,
                      backgroundImage: isAhead
                        ? "repeating-linear-gradient(45deg, #16A34A 0, #16A34A 3px, #86EFAC 3px, #86EFAC 7px)"
                        : "repeating-linear-gradient(45deg, #DC2626 0, #DC2626 3px, #FCA5A5 3px, #FCA5A5 7px)",
                      borderTopRightRadius: isAhead ? "9999px" : "0px",
                      borderBottomRightRadius: isAhead ? "9999px" : "0px",
                    }}
                  />

                  {/* Circular Milestone Pin / Dot */}
                  <div
                    className="absolute top-1/2 -translate-y-1/2 -translate-x-1/2 h-4 w-4 rounded-full border-2 border-white shadow-md z-10 transition-all duration-500"
                    style={{
                      left: `${dotPos}%`,
                      backgroundColor: isAhead ? "#1B4D2E" : "#8B1D1D",
                    }}
                  />
                </div>
              </div>

              {/* Goal Progress Schedule Caption (matching reference) */}
              <div>
                {isAhead ? (
                  <p className="text-xs sm:text-[13px] text-[#211917]/75 font-sans leading-relaxed">
                    You're{" "}
                    <span className="font-semibold text-emerald-700">
                      ahead of pace
                    </span>{" "}
                    and should reach your goal{" "}
                    <span className="font-bold text-[#211917]">
                      {paceDiff}% ahead of schedule
                    </span>
                  </p>
                ) : (
                  <p className="text-xs sm:text-[13px] text-[#211917]/75 font-sans leading-relaxed">
                    You're{" "}
                    <span className="font-semibold text-rose-600">
                      behind pace
                    </span>{" "}
                    and should reach your goal{" "}
                    <span className="font-bold text-[#211917]">
                      {paceDiff}% behind schedule
                    </span>
                  </p>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default CategoryDemandChart;
