import { useEffect, useState, useCallback } from "react";
import {
  Crown,
  CheckCircle,
  Lightning,
  Sparkle,
  Buildings,
  UsersThree,
  ArrowRight,
  ArrowsClockwise,
  ShieldCheck,
  CreditCard,
} from "@phosphor-icons/react";
import { useAuth } from "../hooks/useAuth";
import { getAllData, apiRequest } from "../api/api";
import AlertModal from "../components/feedback/AlertModal";
import ConfirmModal from "../components/feedback/ConfirmModal";

const CompanyPlanView = () => {
  const { token, user } = useAuth();
  const [company, setCompany] = useState(null);
  const [plans, setPlans] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [selectedPlanToSwitch, setSelectedPlanToSwitch] = useState(null);
  const [alertModal, setAlertModal] = useState({
    open: false,
    title: "",
    message: "",
    type: "info",
  });

  const loadData = useCallback(async () => {
    setLoading(true);
    try {
      const [companyRes, plansRes] = await Promise.allSettled([
        getAllData("/api/company/my-company", token),
        getAllData("/api/subscription-plans/active", token),
      ]);

      if (companyRes.status === "fulfilled" && companyRes.value) {
        setCompany(companyRes.value.data || companyRes.value);
      }

      if (plansRes.status === "fulfilled" && plansRes.value) {
        const pList = plansRes.value.data || plansRes.value || [];
        setPlans(Array.isArray(pList) ? pList : []);
      }
    } catch (err) {
      console.error("Failed to load subscription plan data:", err);
      setAlertModal({
        open: true,
        title: "Load Error",
        message: err.message || "Failed to load company plan information.",
        type: "error",
      });
    } finally {
      setLoading(false);
    }
  }, [token]);

  useEffect(() => {
    if (token) {
      loadData();
    }
  }, [token, loadData]);

  const handleSwitchPlan = async () => {
    if (!selectedPlanToSwitch) return;
    setActionLoading(true);
    try {
      const payload = {
        plan: selectedPlanToSwitch.name,
        planId: selectedPlanToSwitch.id,
      };

      const res = await apiRequest(
        "/api/company/my-company/change-plan",
        {
          method: "PUT",
          body: JSON.stringify(payload),
        },
        token
      );

      const updatedCompany = res?.data || res;
      setCompany(updatedCompany);
      setSelectedPlanToSwitch(null);

      setAlertModal({
        open: true,
        title: "Plan Changed Successfully",
        message: `Your company plan has been updated to "${selectedPlanToSwitch.name}".`,
        type: "success",
      });
    } catch (err) {
      setAlertModal({
        open: true,
        title: "Change Plan Failed",
        message: err.message || "Failed to update company plan. Please try again.",
        type: "error",
      });
    } finally {
      setActionLoading(false);
    }
  };

  const currentPlanName = company?.subscriptionPlanName || "Standard Plan";

  if (loading) {
    return (
      <div className="flex h-96 w-full items-center justify-center">
        <div className="flex flex-col items-center gap-3">
          <ArrowsClockwise size={32} className="animate-spin text-[#101B82]" />
          <p className="text-sm font-medium text-slate-500 font-sans">
            Loading company subscription details...
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6 font-sans animate-in fade-in duration-200">
      {/* 1. Header Navigation & Company Info */}
      <div className="flex flex-col gap-4 rounded-2xl border border-slate-200/90 bg-white p-6 shadow-xs sm:flex-row sm:items-center sm:justify-between">
        <div>
          <div className="flex items-center gap-2 text-xs font-semibold uppercase tracking-wider text-slate-400">
            <span>Company Account</span>
            <span>/</span>
            <span className="text-[#101B82]">Subscription & Plans</span>
          </div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900 font-serif-title mt-1">
            {company?.name ? `${company.name} Plan` : "Company Subscription"}
          </h1>
          <p className="text-xs text-slate-500 mt-0.5">
            Manage your operational tier, user limits, and features.
          </p>
        </div>

        <button
          type="button"
          onClick={loadData}
          className="inline-flex items-center gap-2 self-start sm:self-auto rounded-xl border border-slate-200 bg-slate-50 px-3.5 py-2 text-xs font-semibold text-slate-700 hover:bg-slate-100 transition shadow-2xs"
        >
          <ArrowsClockwise size={16} />
          <span>Refresh</span>
        </button>
      </div>

      {/* 2. Current Plan Active Banner */}
      <div className="relative overflow-hidden rounded-2xl border border-[#101B82]/20 bg-gradient-to-r from-[#101B82] via-indigo-900 to-[#1e2eaa] p-6 sm:p-8 text-white shadow-md">
        <div className="absolute right-0 top-0 -mr-16 -mt-16 h-64 w-64 rounded-full bg-white/5 blur-2xl pointer-events-none" />

        <div className="relative z-10 flex flex-col md:flex-row md:items-center justify-between gap-6">
          <div className="space-y-3">
            <div className="inline-flex items-center gap-2 rounded-full bg-white/15 px-3 py-1 text-xs font-semibold backdrop-blur-md">
              <Crown size={16} className="text-amber-300" weight="fill" />
              <span>Current Company Subscription</span>
            </div>

            <h2 className="text-3xl font-extrabold tracking-tight font-serif-title text-white">
              {currentPlanName}
            </h2>

            <div className="flex flex-wrap items-center gap-4 text-xs text-white/80">
              <div className="flex items-center gap-1.5">
                <Buildings size={16} className="text-white/60" />
                <span>{company?.name || "Your Company"}</span>
              </div>
              <div className="h-3 w-px bg-white/20" />
              <div className="flex items-center gap-1.5">
                <ShieldCheck size={16} className="text-emerald-400" />
                <span className="uppercase font-bold tracking-wider text-emerald-300">
                  {company?.status || "ACTIVE"}
                </span>
              </div>
              {company?.email && (
                <>
                  <div className="h-3 w-px bg-white/20" />
                  <span>{company.email}</span>
                </>
              )}
            </div>
          </div>

          <div className="rounded-xl bg-white/10 p-4 backdrop-blur-md border border-white/10 text-right">
            <span className="block text-xs uppercase font-medium text-white/70">
              Billing Status
            </span>
            <span className="text-xl font-bold font-serif-title text-white">
              Active Tier
            </span>
            <p className="text-[11px] text-white/60 mt-1">
              Select any plan below to switch instantly.
            </p>
          </div>
        </div>
      </div>

      {/* 3. Available Plans Comparison Grid */}
      <div className="space-y-4">
        <div>
          <h3 className="text-lg font-bold text-slate-900 font-serif-title">
            Available Subscription Tiers
          </h3>
          <p className="text-xs text-slate-500">
            Choose the best plan for your safari business. You can change plans at any time.
          </p>
        </div>

        {plans.length === 0 ? (
          <div className="rounded-2xl border border-slate-200 bg-white p-12 text-center text-slate-500">
            <CreditCard size={36} className="mx-auto text-slate-300 mb-2" />
            <p className="text-sm font-semibold">No active subscription plans configured.</p>
            <p className="text-xs text-slate-400 mt-1">
              Please contact the Platform Administrator to configure available plans.
            </p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {plans.map((plan) => {
              const isCurrent =
                plan.name?.toLowerCase() === currentPlanName?.toLowerCase() ||
                (company?.subscriptionPlanId && plan.id === company.subscriptionPlanId);

              const isPopular = plan.name?.toLowerCase().includes("professional");

              return (
                <div
                  key={plan.id}
                  className={`relative flex flex-col justify-between rounded-2xl border transition-all duration-200 p-6 bg-white shadow-xs ${
                    isCurrent
                      ? "border-emerald-500 ring-2 ring-emerald-500/20"
                      : isPopular
                      ? "border-[#101B82] shadow-md shadow-indigo-950/5 hover:-translate-y-0.5"
                      : "border-slate-200/90 hover:border-slate-300 hover:-translate-y-0.5"
                  }`}
                >
                  {isCurrent && (
                    <div className="absolute -top-3 left-6 inline-flex items-center gap-1 rounded-full bg-emerald-600 px-3 py-0.5 text-[11px] font-bold uppercase tracking-wider text-white shadow-xs">
                      <CheckCircle size={14} weight="fill" />
                      <span>Current Plan</span>
                    </div>
                  )}

                  {!isCurrent && isPopular && (
                    <div className="absolute -top-3 left-6 inline-flex items-center gap-1 rounded-full bg-[#101B82] px-3 py-0.5 text-[11px] font-bold uppercase tracking-wider text-white shadow-xs">
                      <Sparkle size={14} weight="fill" />
                      <span>Recommended</span>
                    </div>
                  )}

                  <div className="space-y-4">
                    <div className="pt-2">
                      <h4 className="text-xl font-bold text-slate-900 font-serif-title">
                        {plan.name}
                      </h4>
                      <p className="text-xs text-slate-500 mt-1">
                        Up to {plan.maxUsers || "unlimited"} company users
                      </p>
                    </div>

                    <div className="flex items-baseline gap-1 border-y border-slate-100 py-4">
                      <span className="text-3xl font-extrabold text-slate-900 font-serif-title">
                        {plan.currency || "$"}
                        {Number(plan.price || 0).toLocaleString()}
                      </span>
                      <span className="text-xs font-semibold text-slate-400">/ month</span>
                    </div>

                    <div className="space-y-2.5 pt-2">
                      <span className="block text-[11px] font-bold uppercase tracking-wider text-slate-400">
                        What&apos;s Included
                      </span>
                      <ul className="space-y-2 text-xs text-slate-600">
                        <li className="flex items-center gap-2">
                          <CheckCircle size={16} className="text-emerald-500 shrink-0" weight="fill" />
                          <span>Up to {plan.maxUsers || 5} operational team users</span>
                        </li>
                        <li className="flex items-center gap-2">
                          <CheckCircle size={16} className="text-emerald-500 shrink-0" weight="fill" />
                          <span>Safari circuit & itinerary creation</span>
                        </li>
                        <li className="flex items-center gap-2">
                          <CheckCircle size={16} className="text-emerald-500 shrink-0" weight="fill" />
                          <span>Client guest passport & flight schedules</span>
                        </li>
                        <li className="flex items-center gap-2">
                          <CheckCircle size={16} className="text-emerald-500 shrink-0" weight="fill" />
                          <span>Lodge booking allocations & invoices</span>
                        </li>
                        <li className="flex items-center gap-2">
                          <CheckCircle size={16} className="text-emerald-500 shrink-0" weight="fill" />
                          <span>Property verification compliance tracking</span>
                        </li>
                      </ul>
                    </div>
                  </div>

                  <div className="mt-6 pt-4 border-t border-slate-100">
                    {isCurrent ? (
                      <button
                        type="button"
                        disabled
                        className="w-full inline-flex items-center justify-center gap-2 rounded-xl border border-emerald-300 bg-emerald-50/80 px-4 py-2.5 text-xs font-bold text-emerald-700 cursor-default"
                      >
                        <CheckCircle size={16} weight="bold" />
                        <span>Active Plan for Your Company</span>
                      </button>
                    ) : (
                      <button
                        type="button"
                        onClick={() => setSelectedPlanToSwitch(plan)}
                        className="w-full inline-flex items-center justify-center gap-2 rounded-xl bg-gradient-to-r from-[#101B82] to-[#1e2eaa] px-4 py-2.5 text-xs font-bold text-white shadow-xs hover:from-[#0d176f] hover:to-[#17258c] transition active:scale-[0.98]"
                      >
                        <span>Switch to {plan.name}</span>
                        <ArrowRight size={14} weight="bold" />
                      </button>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>

      {/* Confirmation Modal */}
      <ConfirmModal
        open={Boolean(selectedPlanToSwitch)}
        title="Switch Company Plan"
        message={`Are you sure you want to change your company subscription from "${currentPlanName}" to "${selectedPlanToSwitch?.name}" (${selectedPlanToSwitch?.currency || "$"}${selectedPlanToSwitch?.price || 0}/month)? Your company team limits and features will be adjusted immediately.`}
        confirmLabel={actionLoading ? "Switching..." : `Switch to ${selectedPlanToSwitch?.name || "Plan"}`}
        cancelLabel="Cancel"
        type="info"
        onConfirm={handleSwitchPlan}
        onCancel={() => setSelectedPlanToSwitch(null)}
      />

      {/* Alert Notification Modal */}
      <AlertModal
        open={alertModal.open}
        title={alertModal.title}
        message={alertModal.message}
        type={alertModal.type}
        onClose={() => setAlertModal((prev) => ({ ...prev, open: false }))}
      />
    </div>
  );
};

export default CompanyPlanView;
