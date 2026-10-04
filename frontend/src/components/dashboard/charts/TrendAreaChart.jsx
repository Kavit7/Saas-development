import { useState } from "react";

/**
 * TrendAreaChart Component
 * A modern, responsive SVG-based area and line spline chart.
 * Renders smooth gradient curves for monthly performance, revenue, and bookings without heavy external charting libraries.
 *
 * @param {Array<{label: string, value: number, secondaryValue?: number}>} data - Array of monthly points
 * @param {string} title - Chart title
 * @param {string} subtitle - Explanatory caption
 * @param {string} color - Primary stroke and gradient theme ("#101B82", "#059669", "#D97706", etc.)
 * @param {string} valuePrefix - Optional prefix (e.g. "$")
 * @param {string} valueSuffix - Optional suffix (e.g. "safaris", "bookings")
 */
const TrendAreaChart = ({
  data = [],
  title = "Performance Trend",
  subtitle = "Monthly progression over the last 6 months",
  color = "#101B82",
  valuePrefix = "",
  valueSuffix = "",
}) => {
  const [hoveredIndex, setHoveredIndex] = useState(null);

  if (!data || data.length === 0) {
    return (
      <div className="rounded-2xl border border-slate-200/90 bg-white p-6 shadow-xs flex items-center justify-center text-slate-400 text-sm">
        No trend data available
      </div>
    );
  }

  // Dimensions of SVG viewport
  const width = 600;
  const height = 240;
  const paddingX = 40;
  const paddingY = 30;

  // Extract numerical values
  const values = data.map((d) => Number(d.value) || 0);
  const maxValue = Math.max(...values, 10);
  const minValue = 0;
  const range = maxValue - minValue || 1;

  // Calculate coordinates for points
  const points = data.map((item, index) => {
    const x = paddingX + (index * (width - 2 * paddingX)) / (data.length - 1 || 1);
    const y = height - paddingY - ((Number(item.value || 0) - minValue) / range) * (height - 2 * paddingY);
    return { x, y, item, index };
  });

  // Build SVG path strings: Area polygon and spline line
  const linePath = points.reduce((acc, pt, i) => {
    if (i === 0) return `M ${pt.x},${pt.y}`;
    const prev = points[i - 1];
    // Smooth cubic bezier control points
    const cpX = (prev.x + pt.x) / 2;
    return `${acc} C ${cpX},${prev.y} ${cpX},${pt.y} ${pt.x},${pt.y}`;
  }, "");

  const areaPath = `${linePath} L ${points[points.length - 1].x},${height - paddingY} L ${points[0].x},${height - paddingY} Z`;

  // Grid line thresholds
  const gridSteps = [1, 0.66, 0.33, 0];

  return (
    <div className="rounded-2xl border border-slate-200/90 bg-white p-5 sm:p-6 shadow-xs flex flex-col justify-between font-sans">
      {/* Header section */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 mb-4">
        <div>
          <h3 className="text-base sm:text-lg font-bold text-slate-900 font-serif-title tracking-tight">
            {title}
          </h3>
          <p className="text-xs text-slate-500 mt-0.5">{subtitle}</p>
        </div>

        {hoveredIndex !== null && (
          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-xl bg-slate-900 text-white text-xs font-semibold shadow-xs animate-in fade-in">
            <span className="text-slate-300">{data[hoveredIndex]?.label}:</span>
            <span className="font-bold text-emerald-400">
              {valuePrefix}
              {Number(data[hoveredIndex]?.value || 0).toLocaleString()} {valueSuffix}
            </span>
          </div>
        )}
      </div>

      {/* SVG Canvas */}
      <div className="relative w-full overflow-hidden">
        <svg
          viewBox={`0 0 ${width} ${height}`}
          className="w-full h-auto overflow-visible select-none"
        >
          <defs>
            {/* Soft gradient fill beneath curve */}
            <linearGradient id={`gradient-${color.replace("#", "")}`} x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor={color} stopOpacity="0.25" />
              <stop offset="100%" stopColor={color} stopOpacity="0.0" />
            </linearGradient>
          </defs>

          {/* Background Grid Lines */}
          {gridSteps.map((fraction, idx) => {
            const yPos = height - paddingY - fraction * (height - 2 * paddingY);
            const labelVal = Math.round(fraction * maxValue);
            return (
              <g key={idx}>
                <line
                  x1={paddingX}
                  y1={yPos}
                  x2={width - paddingX}
                  y2={yPos}
                  stroke="#F1F5F9"
                  strokeWidth="1.5"
                  strokeDasharray={idx === gridSteps.length - 1 ? "none" : "3,3"}
                />
                <text
                  x={paddingX - 10}
                  y={yPos + 3.5}
                  fontSize="10"
                  fill="#94A3B8"
                  textAnchor="end"
                  className="font-mono font-medium"
                >
                  {valuePrefix}
                  {labelVal >= 1000 ? `${(labelVal / 1000).toFixed(1)}k` : labelVal}
                </text>
              </g>
            );
          })}

          {/* Area under curve */}
          <path d={areaPath} fill={`url(#gradient-${color.replace("#", "")})`} />

          {/* Smooth trend line */}
          <path
            d={linePath}
            fill="none"
            stroke={color}
            strokeWidth="2.8"
            strokeLinecap="round"
            strokeLinejoin="round"
          />

          {/* Data Points and interactive hover hitboxes */}
          {points.map((pt) => {
            const isHovered = hoveredIndex === pt.index;
            return (
              <g
                key={pt.index}
                className="cursor-pointer transition-transform"
                onMouseEnter={() => setHoveredIndex(pt.index)}
                onMouseLeave={() => setHoveredIndex(null)}
              >
                {/* Visual Circle */}
                <circle
                  cx={pt.x}
                  cy={pt.y}
                  r={isHovered ? 6 : 4}
                  fill="#FFFFFF"
                  stroke={color}
                  strokeWidth={isHovered ? 3 : 2}
                  className="transition-all duration-150"
                />

                {/* X-axis Month Label */}
                <text
                  x={pt.x}
                  y={height - 8}
                  fontSize="11"
                  fill={isHovered ? "#0F172A" : "#64748B"}
                  fontWeight={isHovered ? "700" : "500"}
                  textAnchor="middle"
                >
                  {pt.item.label}
                </text>

                {/* Transparent hitbox for easy hovering */}
                <rect
                  x={pt.x - 20}
                  y={paddingY}
                  width="40"
                  height={height - 2 * paddingY}
                  fill="transparent"
                />
              </g>
            );
          })}
        </svg>
      </div>
    </div>
  );
};

export default TrendAreaChart;
