import { useState, useEffect } from "react";
import {
  UserPlus,
  Buildings,
  EnvelopeSimple,
  Phone,
  GenderIntersex,
  ShieldCheck,
  X,
  CheckCircle,
  WarningCircle,
  User,
  UsersThree,
  Compass,
  CalendarCheck,
  Crown,
} from "@phosphor-icons/react";
import { apiRequest, getAllData } from "../../api/api";

const ROLE_DEFINITIONS = {
  SUPER_ADMIN: {
    label: "Platform Admin",
    description: "Full platform-level governance, company management & global administration.",
    icon: Crown,
    badgeColor: "bg-purple-100 text-purple-800 border-purple-200",
    superAdminOnly: true,
  },
  ADMIN: {
    label: "Company Admin",
    description: "Primary administrator of the safari operator company with full company controls.",
    icon: ShieldCheck,
    badgeColor: "bg-blue-100 text-blue-800 border-blue-200",
    superAdminOnly: true,
  },
  SALES_PERSON: {
    label: "Sales Person",
    description: "Manages clients, safari quotations, itineraries, inquiries and invoices.",
    icon: UsersThree,
    badgeColor: "bg-emerald-100 text-emerald-800 border-emerald-200",
    superAdminOnly: false,
  },
  RESERVATION_MANAGER: {
    label: "Reservation Manager",
    description: "Coordinates lodge bookings, supplier confirmations, payments & logistics.",
    icon: CalendarCheck,
    badgeColor: "bg-amber-100 text-amber-800 border-amber-200",
    superAdminOnly: false,
  },
  GUIDE: {
    label: "Safari Guide",
    description: "Field safari leader assigned to day trips, park transfers and guest hospitality.",
    icon: Compass,
    badgeColor: "bg-teal-100 text-teal-800 border-teal-200",
    superAdminOnly: false,
  },
};

const UserCreateModal = ({
  isOpen,
  onClose,
  onSuccess,
  currentUser,
  token,
}) => {
  const isSuperAdmin = String(
    currentUser?.role_name || currentUser?.role || ""
  )
    .toUpperCase()
    .includes("SUPER_ADMIN");

  // Determine allowed roles based on current user's privileges
  // Company Admin can only create: SALES_PERSON, RESERVATION_MANAGER, GUIDE
  // Super Admin can create all: SUPER_ADMIN, ADMIN, SALES_PERSON, RESERVATION_MANAGER, GUIDE
  const availableRoleKeys = isSuperAdmin
    ? ["SUPER_ADMIN", "ADMIN", "SALES_PERSON", "RESERVATION_MANAGER", "GUIDE"]
    : ["SALES_PERSON", "RESERVATION_MANAGER", "GUIDE"];

  const [formData, setFormData] = useState({
    firstName: "",
    lastName: "",
    email: "",
    phone: "",
    gender: "",
    roleName: isSuperAdmin ? "ADMIN" : "SALES_PERSON",
    companyId: "",
    companyName: "",
  });

  const [companies, setCompanies] = useState([]);
  const [loadingCompanies, setLoadingCompanies] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  // Fetch companies list for Super Admin company selector
  useEffect(() => {
    if (!isOpen) return;

    // Reset state on open
    setError(null);
    setFormData({
      firstName: "",
      lastName: "",
      email: "",
      phone: "",
      gender: "",
      roleName: isSuperAdmin ? "ADMIN" : "SALES_PERSON",
      companyId: "",
      companyName: currentUser?.companyName || currentUser?.company?.name || "",
    });

    if (isSuperAdmin && token) {
      const fetchCompanies = async () => {
        setLoadingCompanies(true);
        try {
          const res = await getAllData("/api/company/companies?size=200", token);
          const list = Array.isArray(res?.data?.content)
            ? res.data.content
            : Array.isArray(res?.content)
            ? res.content
            : Array.isArray(res?.data)
            ? res.data
            : Array.isArray(res)
            ? res
            : [];
          setCompanies(list);
          if (list.length > 0 && !formData.companyId) {
            setFormData((prev) => ({
              ...prev,
              companyId: list[0].id,
              companyName: list[0].name,
            }));
          }
        } catch (err) {
          console.warn("Could not load companies for user assignment:", err);
        } finally {
          setLoadingCompanies(false);
        }
      };
      fetchCompanies();
    }
  }, [isOpen, isSuperAdmin, token, currentUser]);

  if (!isOpen) return null;

  const isCreatingPlatformAdmin =
    formData.roleName === "SUPER_ADMIN" ||
    formData.roleName === "PLATFORM_ADMIN";

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    if (error) setError(null);
  };

  const handleRoleSelect = (roleKey) => {
    setFormData((prev) => ({
      ...prev,
      roleName: roleKey,
    }));
    if (error) setError(null);
  };

  const handleCompanySelect = (e) => {
    const selectedId = e.target.value;
    const comp = companies.find((c) => String(c.id) === String(selectedId));
    setFormData((prev) => ({
      ...prev,
      companyId: selectedId,
      companyName: comp ? comp.name : "",
    }));
    if (error) setError(null);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);

    // Validation
    if (!formData.firstName.trim()) {
      setError("First Name is required.");
      return;
    }
    if (!formData.lastName.trim()) {
      setError("Last Name is required.");
      return;
    }
    if (!formData.email.trim() || !formData.email.includes("@")) {
      setError("A valid Email address is required.");
      return;
    }
    if (!formData.roleName) {
      setError("Please select a user role.");
      return;
    }

    if (isSuperAdmin && !isCreatingPlatformAdmin && !formData.companyId && !formData.companyName) {
      setError("Please select a company for this user.");
      return;
    }

    setSubmitting(true);
    try {
      const payload = {
        firstName: formData.firstName.trim(),
        lastName: formData.lastName.trim(),
        email: formData.email.trim().toLowerCase(),
        phone: formData.phone.trim() || null,
        gender: formData.gender || null,
        roleName: formData.roleName,
      };

      if (isSuperAdmin) {
        if (!isCreatingPlatformAdmin) {
          if (formData.companyId) {
            payload.companyId = formData.companyId;
          }
          if (formData.companyName) {
            payload.companyName = formData.companyName;
          }
        }
      } else {
        // Company Admin assigns to their company
        if (currentUser?.companyName) {
          payload.companyName = currentUser.companyName;
        }
      }

      const res = await apiRequest("/api/users/create_user", {
        method: "POST",
        body: JSON.stringify(payload),
      }, token);

      onSuccess?.(res);
      onClose();
    } catch (err) {
      setError(err.message || "Failed to create user account. Please check your inputs.");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4 backdrop-blur-2xs overflow-y-auto animate-in fade-in duration-200">
      <div className="w-full max-w-2xl max-h-[92vh] flex flex-col rounded-3xl border border-slate-200 bg-white shadow-2xl overflow-hidden my-auto">
        {/* Header */}
        <div className="relative bg-gradient-to-r from-[#264624] via-[#325b30] to-[#264624] px-6 py-5 text-white">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="flex h-11 w-11 items-center justify-center rounded-2xl bg-white/15 text-white backdrop-blur-sm border border-white/20">
                <UserPlus size={24} weight="bold" />
              </div>
              <div>
                <h2 className="text-xl font-bold tracking-tight font-serif-title">
                  {isSuperAdmin ? "Register System User" : "Add Team Member"}
                </h2>
                <p className="text-xs text-white/80 mt-0.5">
                  {isSuperAdmin
                    ? "Create platform administrators, company admins, or operator staff."
                    : `Register a new member to ${currentUser?.companyName || "your company"}.`}
                </p>
              </div>
            </div>
            <button
              type="button"
              onClick={onClose}
              disabled={submitting}
              className="rounded-xl p-2 text-white/70 hover:bg-white/15 hover:text-white transition cursor-pointer"
            >
              <X size={20} weight="bold" />
            </button>
          </div>
        </div>

        {/* Content Body */}
        <form onSubmit={handleSubmit} className="flex-1 overflow-y-auto p-6 space-y-6">
          {error && (
            <div className="flex items-start gap-3 rounded-2xl border border-rose-200 bg-rose-50 p-4 text-xs sm:text-sm text-rose-800">
              <WarningCircle size={20} weight="fill" className="shrink-0 text-rose-600 mt-0.5" />
              <div className="flex-1">{error}</div>
            </div>
          )}

          {/* 1. ROLE SELECTION */}
          <div>
            <label className="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-2">
              Select Role & Responsibilities <span className="text-rose-500">*</span>
            </label>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
              {availableRoleKeys.map((roleKey) => {
                const roleDef = ROLE_DEFINITIONS[roleKey];
                const isSelected = formData.roleName === roleKey;
                const IconComponent = roleDef.icon || User;

                return (
                  <button
                    key={roleKey}
                    type="button"
                    onClick={() => handleRoleSelect(roleKey)}
                    className={`flex items-start gap-3 rounded-2xl border p-3.5 text-left transition cursor-pointer ${
                      isSelected
                        ? "border-[#264624] bg-[#264624]/5 ring-2 ring-[#264624]/20 shadow-xs"
                        : "border-slate-200 bg-slate-50/50 hover:bg-white hover:border-slate-300"
                    }`}
                  >
                    <div
                      className={`flex h-9 w-9 shrink-0 items-center justify-center rounded-xl border ${
                        isSelected
                          ? "bg-[#264624] text-white border-[#264624]"
                          : "bg-white text-slate-600 border-slate-200"
                      }`}
                    >
                      <IconComponent size={20} weight={isSelected ? "fill" : "bold"} />
                    </div>
                    <div className="min-w-0 flex-1">
                      <div className="flex items-center justify-between gap-1">
                        <span className="font-semibold text-xs sm:text-sm text-slate-900">
                          {roleDef.label}
                        </span>
                        {isSelected && (
                          <CheckCircle size={16} weight="fill" className="text-[#264624] shrink-0" />
                        )}
                      </div>
                      <p className="text-[11px] text-slate-500 line-clamp-2 mt-0.5 leading-snug">
                        {roleDef.description}
                      </p>
                    </div>
                  </button>
                );
              })}
            </div>
          </div>

          {/* 2. COMPANY ASSIGNMENT (Super Admin dropdown or Company Admin badge) */}
          {isSuperAdmin ? (
            <div className="rounded-2xl border border-slate-200 bg-slate-50/60 p-4 space-y-2">
              <label className="block text-xs font-bold uppercase tracking-wider text-slate-700">
                Company Assignment{" "}
                {!isCreatingPlatformAdmin && <span className="text-rose-500">*</span>}
              </label>

              {isCreatingPlatformAdmin ? (
                <div className="flex items-center gap-2.5 rounded-xl border border-purple-200 bg-purple-50/60 p-3 text-xs text-purple-900">
                  <Crown size={20} weight="fill" className="text-purple-600 shrink-0" />
                  <div>
                    <span className="font-bold">Global Platform Level:</span> This user is a Platform Super Admin and is not constrained to any single safari company.
                  </div>
                </div>
              ) : (
                <div>
                  <div className="relative">
                    <select
                      value={formData.companyId}
                      onChange={handleCompanySelect}
                      disabled={loadingCompanies}
                      className="w-full appearance-none rounded-xl border border-slate-300 bg-white px-3.5 py-2.5 text-xs sm:text-sm font-medium text-slate-900 outline-none transition focus:border-[#264624] focus:ring-2 focus:ring-[#264624]/10 cursor-pointer disabled:bg-slate-100"
                    >
                      <option value="">-- Choose Company --</option>
                      {companies.map((c) => (
                        <option key={c.id} value={c.id}>
                          {c.name} {c.country ? `(${c.country})` : ""} {c.subscriptionPlan ? `• ${c.subscriptionPlan.name || ""}` : ""}
                        </option>
                      ))}
                    </select>
                    <div className="pointer-events-none absolute inset-y-0 right-0 flex items-center px-3 text-slate-400">
                      <Buildings size={18} />
                    </div>
                  </div>
                  {companies.length === 0 && !loadingCompanies && (
                    <p className="text-[11px] text-amber-600 mt-1">
                      No companies found. Please create a company first in the Companies menu.
                    </p>
                  )}
                </div>
              )}
            </div>
          ) : (
            <div className="flex items-center justify-between rounded-2xl border border-slate-200 bg-slate-50/70 p-3.5 px-4 text-xs">
              <div className="flex items-center gap-2.5 text-slate-600">
                <Buildings size={20} weight="bold" className="text-[#264624]" />
                <span>Assigned Company:</span>
              </div>
              <span className="font-bold text-slate-900 bg-white px-2.5 py-1 rounded-lg border border-slate-200 shadow-2xs">
                {currentUser?.companyName || "Your Company"}
              </span>
            </div>
          )}

          {/* 3. PERSONAL DETAILS */}
          <div className="space-y-4">
            <h3 className="text-xs font-bold uppercase tracking-wider text-slate-700">
              Personal Information
            </h3>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-medium text-slate-700 mb-1">
                  First Name <span className="text-rose-500">*</span>
                </label>
                <div className="relative">
                  <input
                    type="text"
                    name="firstName"
                    value={formData.firstName}
                    onChange={handleChange}
                    placeholder="e.g. Amani"
                    required
                    className="w-full rounded-xl border border-slate-300 bg-white px-3.5 py-2.5 text-xs sm:text-sm text-slate-900 outline-none transition focus:border-[#264624] focus:ring-2 focus:ring-[#264624]/10"
                  />
                  <div className="pointer-events-none absolute inset-y-0 right-0 flex items-center px-3 text-slate-400">
                    <User size={16} />
                  </div>
                </div>
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-700 mb-1">
                  Last Name <span className="text-rose-500">*</span>
                </label>
                <div className="relative">
                  <input
                    type="text"
                    name="lastName"
                    value={formData.lastName}
                    onChange={handleChange}
                    placeholder="e.g. Mushi"
                    required
                    className="w-full rounded-xl border border-slate-300 bg-white px-3.5 py-2.5 text-xs sm:text-sm text-slate-900 outline-none transition focus:border-[#264624] focus:ring-2 focus:ring-[#264624]/10"
                  />
                  <div className="pointer-events-none absolute inset-y-0 right-0 flex items-center px-3 text-slate-400">
                    <User size={16} />
                  </div>
                </div>
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-medium text-slate-700 mb-1">
                  Email Address <span className="text-rose-500">*</span>
                </label>
                <div className="relative">
                  <input
                    type="email"
                    name="email"
                    value={formData.email}
                    onChange={handleChange}
                    placeholder="amani.mushi@safari.com"
                    required
                    className="w-full rounded-xl border border-slate-300 bg-white px-3.5 py-2.5 text-xs sm:text-sm text-slate-900 outline-none transition focus:border-[#264624] focus:ring-2 focus:ring-[#264624]/10"
                  />
                  <div className="pointer-events-none absolute inset-y-0 right-0 flex items-center px-3 text-slate-400">
                    <EnvelopeSimple size={16} />
                  </div>
                </div>
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-700 mb-1">
                  Phone Number
                </label>
                <div className="relative">
                  <input
                    type="tel"
                    name="phone"
                    value={formData.phone}
                    onChange={handleChange}
                    placeholder="+255 754 123 456"
                    className="w-full rounded-xl border border-slate-300 bg-white px-3.5 py-2.5 text-xs sm:text-sm text-slate-900 outline-none transition focus:border-[#264624] focus:ring-2 focus:ring-[#264624]/10"
                  />
                  <div className="pointer-events-none absolute inset-y-0 right-0 flex items-center px-3 text-slate-400">
                    <Phone size={16} />
                  </div>
                </div>
              </div>
            </div>

            <div>
              <label className="block text-xs font-medium text-slate-700 mb-1">
                Gender (Optional)
              </label>
              <div className="relative">
                <select
                  name="gender"
                  value={formData.gender}
                  onChange={handleChange}
                  className="w-full appearance-none rounded-xl border border-slate-300 bg-white px-3.5 py-2.5 text-xs sm:text-sm text-slate-900 outline-none transition focus:border-[#264624] focus:ring-2 focus:ring-[#264624]/10 cursor-pointer"
                >
                  <option value="">-- Select Gender --</option>
                  <option value="MALE">Male</option>
                  <option value="FEMALE">Female</option>
                </select>
                <div className="pointer-events-none absolute inset-y-0 right-0 flex items-center px-3 text-slate-400">
                  <GenderIntersex size={16} />
                </div>
              </div>
            </div>
          </div>

          {/* 4. AUTOMATED CREDENTIAL NOTICE (NO PASSWORD INPUT) */}
          <div className="rounded-2xl border border-emerald-200 bg-gradient-to-br from-emerald-50/70 to-teal-50/40 p-4">
            <div className="flex items-start gap-3">
              <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-xl bg-emerald-600 text-white shadow-xs">
                <ShieldCheck size={20} weight="fill" />
              </div>
              <div className="space-y-1">
                <h4 className="text-xs sm:text-sm font-bold text-emerald-950">
                  Automated Secure Password Policy
                </h4>
                <p className="text-xs text-emerald-800/90 leading-relaxed">
                  For account security and compliance, you do not manually set user passwords. The platform automatically generates a temporary initial credential (default: <code className="bg-emerald-100/80 px-1.5 py-0.5 rounded text-emerald-900 font-mono text-[11px] font-semibold">{formData.lastName.trim() ? `${formData.lastName.toLowerCase().trim()}123!` : "[lastname]123!"}</code>).
                </p>
                <p className="text-[11px] text-emerald-700 leading-normal">
                  • The user will be required to choose a new password upon their very first login.
                  <br />
                  • The temporary password automatically expires in 3 days.
                </p>
              </div>
            </div>
          </div>
        </form>

        {/* Footer */}
        <div className="flex items-center justify-end gap-3 border-t border-slate-200 bg-slate-50/90 px-6 py-4">
          <button
            type="button"
            onClick={onClose}
            disabled={submitting}
            className="rounded-xl border border-slate-300 bg-white px-4 py-2.5 text-xs sm:text-sm font-semibold text-slate-700 hover:bg-slate-100 transition cursor-pointer"
          >
            Cancel
          </button>
          <button
            type="button"
            onClick={handleSubmit}
            disabled={submitting}
            className="inline-flex items-center gap-2 rounded-xl bg-[#264624] px-5 py-2.5 text-xs sm:text-sm font-semibold text-white shadow-md hover:bg-[#1b331a] transition cursor-pointer disabled:opacity-60"
          >
            {submitting ? (
              <>
                <div className="h-4 w-4 animate-spin rounded-full border-2 border-white border-t-transparent" />
                <span>Creating User...</span>
              </>
            ) : (
              <>
                <UserPlus size={18} weight="bold" />
                <span>Create User Account</span>
              </>
            )}
          </button>
        </div>
      </div>
    </div>
  );
};

export default UserCreateModal;
