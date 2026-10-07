import { useState } from "react";

/**
 * DonutStatusChart Component
 * Renders a modern SVG donut chart for category/status distributions.
 * Features an interactive arc highlight, central count indicator, and clean legend.
 *
 * @param {Array<{label: string, count: number, percentage: number, color: string}>} data - Slice definitions
 * @param {string} title - Card title
 * @param {string} subtitle - Explanatory caption
 * @param {string} centerLabel - Text displayed in the donut hole (e.g. "Total Safaris")
 */
const DonutStatusChart = ({
  data = [],
  title = "Status Distribution",
  subtitle = "Breakdown by operational category",
  centerLabel = "Total",
}) => {
  const [hoveredIdx, setHoveredIdx] = useState(null);

  const totalCount = data.reduce((sum, item) => sum + (Number(item.count) || 0), 0);

  // SVG circle geometry parameters
  const size = 180;
  const strokeWidth = 24;
  const radius = (size - strokeWidth) / 2;
  const circumference = 2 * Math.PI * radius;

  // Compute accumulated stroke offsets for each slice
  let accumulatedPercent = 0;
  const slices = data.map((item, index) => {
    const count = Number(item.count) || 0;
    const percent = totalCount > 0 ? (count / totalCount) : 0;
    const strokeDasharray = `${percent * circumference} ${circumference}`;
    const strokeDashoffset = -accumulatedPercent * circumference;
    accumulatedPercent += percent;

    return {
      ...item,
      index,
      percent: Math.round(percent * 100),
      strokeDasharray,
      strokeDashoffset,
    };
  });

  return (
    <div className="rounded-2xl border border-slate-200/90 bg-white p-5 sm:p-6 shadow-xs flex flex-col justify-between font-sans">
      <div className="mb-4">
        <h3 className="text-base sm:text-lg font-bold text-slate-900 font-serif-title tracking-tight">
          {title}
        </h3>
        <p className="text-xs text-slate-500 mt-0.5">{subtitle}</p>
      </div>

      <div className="flex flex-col sm:flex-row items-center justify-around gap-6 my-auto py-2">
        {/* Donut SVG */}
        <div className="relative flex items-center justify-center shrink-0">
          <svg width={size} height={size} className="transform -rotate-90">
            {/* Background Track */}
            <circle
              cx={size / 2}
              cy={size / 2}
              r={radius}
              fill="transparent"
              stroke="#F1F5F9"
              strokeWidth={strokeWidth}
            />

            {/* Slices */}
            {slices.map((slice) => {
              const isHovered = hoveredIdx === slice.index;
              return (
                <circle
                  key={slice.index}
                  cx={size / 2}
                  cy={size / 2}
                  r={radius}
                  fill="transparent"
                  stroke={slice.color || "#264624"}
                  strokeWidth={isHovered ? strokeWidth + 4 : strokeWidth}
                  strokeDasharray={slice.strokeDasharray}
                  strokeDashoffset={slice.strokeDashoffset}
                  strokeLinecap="round"
                  className="transition-all duration-200 cursor-pointer"
                  onMouseEnter={() => setHoveredIdx(slice.index)}
                  onMouseLeave={() => setHoveredIdx(null)}
                />
              );
            })}
          </svg>

          {/* Central Summary Counter */}
          <div className="absolute inset-0 flex flex-col items-center justify-center text-center pointer-events-none">
            <span className="text-2xl sm:text-3xl font-extrabold text-slate-900 font-serif-title tracking-tight">
              {hoveredIdx !== null ? slices[hoveredIdx]?.count : totalCount.toLocaleString()}
            </span>
            <span className="text-[11px] font-semibold text-slate-400 uppercase tracking-wider">
              {hoveredIdx !== null ? slices[hoveredIdx]?.label : centerLabel}
            </span>
          </div>
        </div>

        {/* Legend */}
        <div className="flex flex-col gap-2.5 w-full sm:w-auto">
          {slices.map((slice) => {
            const isHovered = hoveredIdx === slice.index;
            return (
              <div
                key={slice.index}
                onMouseEnter={() => setHoveredIdx(slice.index)}
                onMouseLeave={() => setHoveredIdx(null)}
                className={`flex items-center justify-between sm:justify-start gap-3 p-2 rounded-xl text-xs transition cursor-pointer ${
                  isHovered ? "bg-slate-50 font-semibold" : "hover:bg-slate-50/60"
                }`}
              >
                <div className="flex items-center gap-2">
                  <span
                    className="h-2.5 w-2.5 rounded-full shrink-0 shadow-2xs"
                    style={{ backgroundColor: slice.color || "#264624" }}
                  />
                  <span className="text-slate-700">{slice.label}</span>
                </div>
                <div className="flex items-center gap-2 font-mono text-[11px] text-slate-500">
                  <span className="font-bold text-slate-900">{slice.count}</span>
                  <span className="text-slate-400">({slice.percent}%)</span>
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
};

export default DonutStatusChart;
