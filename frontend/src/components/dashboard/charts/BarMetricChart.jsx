/**
 * BarMetricChart Component
 * Visualizes comparative metrics and capacity targets using sleek progress bars.
 *
 * @param {Array<{label: string, value: number, target?: number, color?: string}>} items - Metric bars
 * @param {string} title - Card header
 * @param {string} subtitle - Explanatory caption
 */
const BarMetricChart = ({
  items = [],
  title = "Operational Allocation",
  subtitle = "Performance against benchmark targets",
}) => {
  const maxVal = Math.max(...items.map((i) => Math.max(i.value || 0, i.target || 0)), 10);

  return (
    <div className="flex flex-col justify-between rounded-xl border border-[#28332C]/10 bg-white p-5 font-sans">
      <div className="mb-4">
        <h3 className="text-base font-semibold tracking-tight text-[#28332C]">
          {title}
        </h3>
        <p className="text-xs text-slate-500 mt-0.5">{subtitle}</p>
      </div>

      <div className="space-y-4 my-auto">
        {items.map((item, idx) => {
          const valPct = Math.min(Math.round((item.value / maxVal) * 100), 100);
          const barColor = item.color || "#264624";

          return (
            <div key={idx} className="space-y-1.5">
              <div className="flex items-center justify-between text-xs">
                <span className="font-semibold text-slate-700">{item.label}</span>
                <span className="font-medium text-slate-700">
                  {item.value} {item.target ? `/ ${item.target}` : ""}
                </span>
              </div>

              {/* Progress track */}
              <div className="relative h-2 w-full overflow-hidden rounded-full bg-[#E8ECE8]">
                <div
                  className="h-full rounded-full transition-all duration-500 ease-out"
                  style={{
                    width: `${valPct}%`,
                    backgroundColor: barColor,
                  }}
                />
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default BarMetricChart;
