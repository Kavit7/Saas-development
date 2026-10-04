import { useEffect, useState, useCallback, useMemo } from "react";
import {
  EnvelopeSimple,
  Receipt,
  ArrowsClockwise,
  MagnifyingGlass,
  CheckCircle,
  Clock,
  Printer,
  X,
  FilePlus,
  Buildings,
  Compass,
  User,
  CalendarBlank,
  WarningCircle,
  CurrencyCircleDollar,
} from "@phosphor-icons/react";
import { useAuth } from "../../hooks/useAuth";
import { getAllData, apiRequest } from "../../api/api";

/**
 * MailboxView
 * Unified Operations Mailbox & Booking Invoicing Center.
 * Enables reservation managers, sales executives, and administrators to:
 * 1. Monitor incoming lodge email communication, confirmations, and inquiries.
 * 2. Directly inspect, manage, and print official accommodation booking invoices and vouchers.
 */
const MailboxView = () => {
  const { token, user } = useAuth();

  // Active navigation tab: 'invoices' | 'emails'
  const [activeTab, setActiveTab] = useState("invoices");

  // State: Invoices
  const [invoices, setInvoices] = useState([]);
  const [invoiceLoading, setInvoiceLoading] = useState(true);
  const [invoiceStatusFilter, setInvoiceStatusFilter] = useState("ALL");
  const [invoiceSearch, setInvoiceSearch] = useState("");
  const [selectedInvoice, setSelectedInvoice] = useState(null);

  // State: Emails
  const [emails, setEmails] = useState([]);
  const [emailLoading, setEmailLoading] = useState(true);
  const [emailSearch, setEmailSearch] = useState("");
  const [emailStatusFilter, setEmailStatusFilter] = useState("ALL");
  const [selectedEmail, setSelectedEmail] = useState(null);
  const [syncing, setSyncing] = useState(false);
  const [syncMessage, setSyncMessage] = useState(null);

  // State: Record Invoice Modal
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [bookingsList, setBookingsList] = useState([]);
  const [submittingInvoice, setSubmittingInvoice] = useState(false);
  const [createForm, setCreateForm] = useState({
    bookingId: "",
    invoiceNumber: "",
    amount: "",
    currency: "USD",
    dueDate: "",
    status: "PENDING",
  });

  // Fetch Invoices
  const fetchInvoices = useCallback(async () => {
    if (!token) return;
    setInvoiceLoading(true);
    try {
      const res = await getAllData("/api/v1/mailbox/invoices", token);
      if (Array.isArray(res)) {
        setInvoices(res);
      }
    } catch (err) {
      console.error("Error fetching invoices:", err);
    } finally {
      setInvoiceLoading(false);
    }
  }, [token]);

  // Fetch Emails
  const fetchEmails = useCallback(async () => {
    if (!token) return;
    setEmailLoading(true);
    try {
      const res = await getAllData("/api/v1/mailbox/emails", token);
      if (Array.isArray(res)) {
        setEmailEmails(res);
      }
    } catch (err) {
      console.error("Error fetching emails:", err);
    } finally {
      setEmailLoading(false);
    }
  }, [token]);

  // Helper to safely set emails
  const setEmailEmails = (data) => {
    setEmails(data);
  };

  // Fetch Bookings for Manual Invoice Creation
  const fetchBookings = useCallback(async () => {
    if (!token) return;
    try {
      const res = await getAllData("/api/v1/accommodation-bookings", token);
      if (Array.isArray(res)) {
        setBookingsList(res);
      }
    } catch (err) {
      console.warn("Could not load bookings list:", err);
    }
  }, [token]);

  useEffect(() => {
    fetchInvoices();
    fetchEmails();
    fetchBookings();
  }, [fetchInvoices, fetchEmails, fetchBookings]);

  // Trigger manual IMAP mailbox sync
  const handleSyncMailbox = async () => {
    if (!token || syncing) return;
    setSyncing(true);
    setSyncMessage(null);
    try {
      await apiRequest("/api/v1/mailbox/sync", { method: "POST" }, token);
      setSyncMessage({ type: "success", text: "Mailbox synchronized successfully! New emails received." });
      await fetchEmails();
      await fetchInvoices();
    } catch (err) {
      setSyncMessage({ type: "error", text: err.message || "Mailbox synchronization failed" });
    } finally {
      setSyncing(false);
      setTimeout(() => setSyncMessage(null), 5000);
    }
  };

  // Toggle or Update Invoice Status (e.g. PENDING <-> PAID)
  const handleUpdateStatus = async (invoiceId, newStatus) => {
    try {
      const updated = await apiRequest(
        `/api/v1/mailbox/invoices/${invoiceId}/status`,
        {
          method: "PATCH",
          body: JSON.stringify({ status: newStatus }),
        },
        token
      );
      setInvoices((prev) =>
        prev.map((inv) => (inv.id === invoiceId ? { ...inv, status: updated.status } : inv))
      );
      if (selectedInvoice && selectedInvoice.id === invoiceId) {
        setSelectedInvoice((prev) => ({ ...prev, status: updated.status }));
      }
    } catch (err) {
      alert("Failed to update invoice status: " + err.message);
    }
  };

  // Submit Manual Invoice Form
  const handleCreateInvoice = async (e) => {
    e.preventDefault();
    if (!createForm.bookingId || !createForm.amount) {
      alert("Please select a booking and specify amount.");
      return;
    }
    setSubmittingInvoice(true);
    try {
      const payload = {
        bookingId: createForm.bookingId,
        invoiceNumber: createForm.invoiceNumber.trim() || undefined,
        amount: parseFloat(createForm.amount),
        currency: createForm.currency || "USD",
        dueDate: createForm.dueDate || undefined,
        status: createForm.status || "PENDING",
      };
      await apiRequest("/api/v1/mailbox/invoices", {
        method: "POST",
        body: JSON.stringify(payload),
      }, token);
      setShowCreateModal(false);
      setCreateForm({
        bookingId: "",
        invoiceNumber: "",
        amount: "",
        currency: "USD",
        dueDate: "",
        status: "PENDING",
      });
      await fetchInvoices();
    } catch (err) {
      alert("Failed to record invoice: " + err.message);
    } finally {
      setSubmittingInvoice(false);
    }
  };

  // Invoice KPIs
  const invoiceMetrics = useMemo(() => {
    const totalCount = invoices.length;
    let totalAmount = 0;
    let paidAmount = 0;
    let pendingAmount = 0;
    let pendingCount = 0;
    let paidCount = 0;

    invoices.forEach((inv) => {
      const amt = Number(inv.amount) || 0;
      totalAmount += amt;
      if (inv.status === "PAID") {
        paidAmount += amt;
        paidCount++;
      } else if (inv.status === "PENDING" || inv.status === "OVERDUE") {
        pendingAmount += amt;
        pendingCount++;
      }
    });

    return { totalCount, totalAmount, paidAmount, pendingAmount, pendingCount, paidCount };
  }, [invoices]);

  // Filtered Invoices
  const filteredInvoices = useMemo(() => {
    return invoices.filter((inv) => {
      const matchesStatus =
        invoiceStatusFilter === "ALL" || inv.status === invoiceStatusFilter;
      const searchLower = invoiceSearch.toLowerCase().trim();
      const matchesSearch =
        !searchLower ||
        (inv.invoiceNumber && inv.invoiceNumber.toLowerCase().includes(searchLower)) ||
        (inv.propertyName && inv.propertyName.toLowerCase().includes(searchLower)) ||
        (inv.safariReference && inv.safariReference.toLowerCase().includes(searchLower)) ||
        (inv.clientName && inv.clientName.toLowerCase().includes(searchLower)) ||
        (inv.bookingReference && inv.bookingReference.toLowerCase().includes(searchLower));
      return matchesStatus && matchesSearch;
    });
  }, [invoices, invoiceStatusFilter, invoiceSearch]);

  // Filtered Emails
  const filteredEmails = useMemo(() => {
    return emails.filter((em) => {
      const matchesStatus =
        emailStatusFilter === "ALL" ||
        (emailStatusFilter === "PROCESSED" && em.processed) ||
        (emailStatusFilter === "UNPROCESSED" && !em.processed);
      const searchLower = emailSearch.toLowerCase().trim();
      const matchesSearch =
        !searchLower ||
        (em.subject && em.subject.toLowerCase().includes(searchLower)) ||
        (em.fromEmail && em.fromEmail.toLowerCase().includes(searchLower)) ||
        (em.bookingReference && em.bookingReference.toLowerCase().includes(searchLower)) ||
        (em.propertyName && em.propertyName.toLowerCase().includes(searchLower)) ||
        (em.body && em.body.toLowerCase().includes(searchLower));
      return matchesStatus && matchesSearch;
    });
  }, [emails, emailStatusFilter, emailSearch]);

  return (
    <div className="space-y-6">
      {/* Top Header */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="font-serif-title text-2xl sm:text-3xl font-bold tracking-tight text-[#101B82]">
            Operations Mailbox & Invoicing
          </h1>
          <p className="mt-1 text-sm text-[#211917]/70">
            Lodge availability communications, incoming reservation emails, and verified booking invoices.
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-2 sm:gap-3">
          <button
            onClick={handleSyncMailbox}
            disabled={syncing}
            className="inline-flex items-center gap-2 rounded-[10px] border border-[#101B82]/20 bg-white px-3.5 py-2 text-xs sm:text-sm font-semibold text-[#101B82] shadow-sm transition hover:bg-[#101B82]/5 disabled:opacity-50"
            title="Check IMAP mailbox for new lodge confirmations"
          >
            <ArrowsClockwise size={16} className={syncing ? "animate-spin text-[#101B82]" : ""} />
            {syncing ? "Checking Inbox..." : "Sync Mailbox"}
          </button>

          <button
            onClick={() => setShowCreateModal(true)}
            className="inline-flex items-center gap-2 rounded-[10px] bg-[#101B82] px-4 py-2 text-xs sm:text-sm font-semibold text-white shadow-sm transition hover:bg-[#0c1566]"
          >
            <FilePlus size={16} />
            Record Invoice
          </button>
        </div>
      </div>

      {/* Sync Alert Banner */}
      {syncMessage && (
        <div
          className={`flex items-center gap-2 rounded-xl p-3 text-sm font-medium ${
            syncMessage.type === "success"
              ? "bg-emerald-50 text-emerald-800 border border-emerald-200"
              : "bg-red-50 text-red-800 border border-red-200"
          }`}
        >
          {syncMessage.type === "success" ? <CheckCircle size={18} /> : <WarningCircle size={18} />}
          <span>{syncMessage.text}</span>
        </div>
      )}

      {/* Primary Tab Switcher */}
      <div className="flex border-b border-[#211917]/10 bg-white px-4 pt-2 rounded-t-xl shadow-sm">
        <button
          onClick={() => setActiveTab("invoices")}
          className={`flex items-center gap-2.5 border-b-2 px-5 py-3.5 text-sm font-bold transition ${
            activeTab === "invoices"
              ? "border-[#101B82] text-[#101B82]"
              : "border-transparent text-[#211917]/60 hover:text-[#211917]"
          }`}
        >
          <Receipt size={20} />
          <span>Booking Invoices & Vouchers</span>
          <span
            className={`ml-1 rounded-full px-2 py-0.5 text-xs font-bold ${
              activeTab === "invoices" ? "bg-[#101B82] text-white" : "bg-[#211917]/10 text-[#211917]"
            }`}
          >
            {invoices.length}
          </span>
        </button>

        <button
          onClick={() => setActiveTab("emails")}
          className={`flex items-center gap-2.5 border-b-2 px-5 py-3.5 text-sm font-bold transition ${
            activeTab === "emails"
              ? "border-[#101B82] text-[#101B82]"
              : "border-transparent text-[#211917]/60 hover:text-[#211917]"
          }`}
        >
          <EnvelopeSimple size={20} />
          <span>Lodge Mailbox & Inquiries</span>
          <span
            className={`ml-1 rounded-full px-2 py-0.5 text-xs font-bold ${
              activeTab === "emails" ? "bg-[#101B82] text-white" : "bg-[#211917]/10 text-[#211917]"
            }`}
          >
            {emails.length}
          </span>
        </button>
      </div>

      {/* ========================================================
          TAB 1: BOOKING INVOICES & VOUCHERS
          ======================================================== */}
      {activeTab === "invoices" && (
        <div className="space-y-6">
          {/* Executive Financial Metrics */}
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
            <div className="rounded-xl border border-[#211917]/10 bg-white p-5 shadow-sm">
              <div className="flex items-center justify-between">
                <p className="text-xs font-bold uppercase tracking-wider text-[#211917]/60">
                  Total Invoiced
                </p>
                <span className="flex h-9 w-9 items-center justify-center rounded-lg bg-[#101B82]/10 text-[#101B82]">
                  <CurrencyCircleDollar size={22} weight="duotone" />
                </span>
              </div>
              <p className="font-serif-title mt-2 text-2xl font-bold text-[#101B82]">
                ${invoiceMetrics.totalAmount.toLocaleString("en-US", { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
              </p>
              <p className="mt-1 text-xs text-[#211917]/60">
                Across {invoiceMetrics.totalCount} booking invoices
              </p>
            </div>

            <div className="rounded-xl border border-emerald-100 bg-white p-5 shadow-sm">
              <div className="flex items-center justify-between">
                <p className="text-xs font-bold uppercase tracking-wider text-emerald-700">
                  Settled / Paid
                </p>
                <span className="flex h-9 w-9 items-center justify-center rounded-lg bg-emerald-100 text-emerald-700">
                  <CheckCircle size={22} weight="fill" />
                </span>
              </div>
              <p className="font-serif-title mt-2 text-2xl font-bold text-emerald-700">
                ${invoiceMetrics.paidAmount.toLocaleString("en-US", { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
              </p>
              <p className="mt-1 text-xs text-emerald-600/80">
                {invoiceMetrics.paidCount} invoices cleared
              </p>
            </div>

            <div className="rounded-xl border border-amber-100 bg-white p-5 shadow-sm">
              <div className="flex items-center justify-between">
                <p className="text-xs font-bold uppercase tracking-wider text-amber-700">
                  Awaiting Settlement
                </p>
                <span className="flex h-9 w-9 items-center justify-center rounded-lg bg-amber-100 text-amber-700">
                  <Clock size={22} weight="fill" />
                </span>
              </div>
              <p className="font-serif-title mt-2 text-2xl font-bold text-amber-700">
                ${invoiceMetrics.pendingAmount.toLocaleString("en-US", { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
              </p>
              <p className="mt-1 text-xs text-amber-600/80">
                {invoiceMetrics.pendingCount} pending payment
              </p>
            </div>

            <div className="rounded-xl border border-[#211917]/10 bg-white p-5 shadow-sm">
              <div className="flex items-center justify-between">
                <p className="text-xs font-bold uppercase tracking-wider text-[#211917]/60">
                  Collection Rate
                </p>
                <span className="flex h-9 w-9 items-center justify-center rounded-lg bg-blue-100 text-blue-700">
                  <Receipt size={22} weight="duotone" />
                </span>
              </div>
              <p className="font-serif-title mt-2 text-2xl font-bold text-[#101B82]">
                {invoiceMetrics.totalAmount > 0
                  ? Math.round((invoiceMetrics.paidAmount / invoiceMetrics.totalAmount) * 100)
                  : 100}
                %
              </p>
              <p className="mt-1 text-xs text-[#211917]/60">
                Lodging cost settlement progress
              </p>
            </div>
          </div>

          {/* Controls: Search and Status Filters */}
          <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between rounded-xl bg-white p-4 border border-[#211917]/10 shadow-sm">
            <div className="relative flex-1 max-w-md">
              <MagnifyingGlass
                size={18}
                className="absolute left-3 top-1/2 -translate-y-1/2 text-[#211917]/40"
              />
              <input
                type="text"
                placeholder="Search invoice #, lodge, safari ref, client..."
                value={invoiceSearch}
                onChange={(e) => setInvoiceSearch(e.target.value)}
                className="w-full rounded-lg border border-[#211917]/20 bg-[#F8F8FC] py-2 pl-9 pr-3 text-sm text-[#211917] outline-none transition focus:border-[#101B82] focus:bg-white"
              />
            </div>

            <div className="flex flex-wrap items-center gap-1.5">
              {["ALL", "PENDING", "PAID", "OVERDUE", "CANCELLED"].map((st) => (
                <button
                  key={st}
                  onClick={() => setInvoiceStatusFilter(st)}
                  className={`rounded-lg px-3 py-1.5 text-xs font-semibold transition ${
                    invoiceStatusFilter === st
                      ? "bg-[#101B82] text-white"
                      : "bg-[#211917]/5 text-[#211917]/70 hover:bg-[#211917]/10"
                  }`}
                >
                  {st}
                </button>
              ))}
            </div>
          </div>

          {/* Invoices List / Table */}
          <div className="overflow-hidden rounded-xl border border-[#211917]/10 bg-white shadow-sm">
            {invoiceLoading ? (
              <div className="p-12 text-center text-sm text-[#211917]/60">
                Loading lodge invoices...
              </div>
            ) : filteredInvoices.length === 0 ? (
              <div className="p-12 text-center">
                <Receipt size={40} className="mx-auto text-[#211917]/20" />
                <p className="mt-2 text-sm font-semibold text-[#211917]">No invoices found</p>
                <p className="text-xs text-[#211917]/60">
                  {invoices.length === 0
                    ? "Invoices are generated automatically when lodge bookings are confirmed, or can be recorded manually."
                    : "No invoices matched your current search filters."}
                </p>
              </div>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-left border-collapse">
                  <thead>
                    <tr className="border-b border-[#211917]/10 bg-[#F8F8FC] text-[11px] font-bold uppercase tracking-wider text-[#211917]/60">
                      <th className="py-3 px-4">Invoice #</th>
                      <th className="py-3 px-4">Lodge / Property</th>
                      <th className="py-3 px-4">Safari & Guest</th>
                      <th className="py-3 px-4">Due Date</th>
                      <th className="py-3 px-4">Amount</th>
                      <th className="py-3 px-4">Status</th>
                      <th className="py-3 px-4 text-right">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-[#211917]/5 text-sm">
                    {filteredInvoices.map((inv) => (
                      <tr key={inv.id} className="transition hover:bg-[#101B82]/[0.02]">
                        <td className="py-3.5 px-4 font-mono text-xs font-bold text-[#101B82]">
                          {inv.invoiceNumber}
                        </td>
                        <td className="py-3.5 px-4 font-semibold text-[#211917]">
                          <div className="flex items-center gap-1.5">
                            <Buildings size={16} className="text-[#101B82]/60 shrink-0" />
                            <span className="truncate max-w-[200px]">{inv.propertyName || "Lodge"}</span>
                          </div>
                        </td>
                        <td className="py-3.5 px-4">
                          <p className="font-semibold text-xs text-[#211917]">
                            {inv.safariReference || inv.bookingReference || "—"}
                          </p>
                          <p className="text-xs text-[#211917]/60">
                            {inv.clientName || "Direct Guest"}
                          </p>
                        </td>
                        <td className="py-3.5 px-4 text-xs text-[#211917]/70">
                          {inv.dueDate || "—"}
                        </td>
                        <td className="py-3.5 px-4 font-semibold text-xs text-[#211917]">
                          ${Number(inv.amount || 0).toLocaleString("en-US", { minimumFractionDigits: 2, maximumFractionDigits: 2 })} {inv.currency || "USD"}
                        </td>
                        <td className="py-3.5 px-4">
                          <span
                            className={`inline-flex items-center gap-1 rounded-full px-2.5 py-0.5 text-[11px] font-bold ${
                              inv.status === "PAID"
                                ? "bg-emerald-100 text-emerald-800"
                                : inv.status === "PENDING"
                                ? "bg-amber-100 text-amber-800"
                                : inv.status === "OVERDUE"
                                ? "bg-red-100 text-red-800"
                                : "bg-gray-100 text-gray-700"
                            }`}
                          >
                            <span className="h-1.5 w-1.5 rounded-full bg-current" />
                            {inv.status}
                          </span>
                        </td>
                        <td className="py-3.5 px-4 text-right">
                          <div className="flex items-center justify-end gap-2">
                            {inv.status !== "PAID" ? (
                              <button
                                onClick={() => handleUpdateStatus(inv.id, "PAID")}
                                className="rounded-md border border-emerald-300 bg-emerald-50 px-2.5 py-1 text-xs font-semibold text-emerald-700 transition hover:bg-emerald-100"
                                title="Mark as Paid"
                              >
                                Mark Paid
                              </button>
                            ) : (
                              <button
                                onClick={() => handleUpdateStatus(inv.id, "PENDING")}
                                className="rounded-md border border-amber-300 bg-amber-50 px-2.5 py-1 text-xs font-semibold text-amber-700 transition hover:bg-amber-100"
                                title="Mark as Pending"
                              >
                                Revert
                              </button>
                            )}
                            <button
                              onClick={() => setSelectedInvoice(inv)}
                              className="inline-flex items-center gap-1 rounded-md bg-[#101B82]/10 px-2.5 py-1 text-xs font-semibold text-[#101B82] transition hover:bg-[#101B82]/20"
                            >
                              <Printer size={14} />
                              Voucher
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      )}

      {/* ========================================================
          TAB 2: LODGE MAILBOX & INQUIRIES
          ======================================================== */}
      {activeTab === "emails" && (
        <div className="space-y-6">
          {/* Controls: Search and Filter */}
          <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between rounded-xl bg-white p-4 border border-[#211917]/10 shadow-sm">
            <div className="relative flex-1 max-w-md">
              <MagnifyingGlass
                size={18}
                className="absolute left-3 top-1/2 -translate-y-1/2 text-[#211917]/40"
              />
              <input
                type="text"
                placeholder="Search email subject, sender, lodge, booking ref..."
                value={emailSearch}
                onChange={(e) => setEmailSearch(e.target.value)}
                className="w-full rounded-lg border border-[#211917]/20 bg-[#F8F8FC] py-2 pl-9 pr-3 text-sm text-[#211917] outline-none transition focus:border-[#101B82] focus:bg-white"
              />
            </div>

            <div className="flex items-center gap-1.5">
              {["ALL", "PROCESSED", "UNPROCESSED"].map((st) => (
                <button
                  key={st}
                  onClick={() => setEmailStatusFilter(st)}
                  className={`rounded-lg px-3 py-1.5 text-xs font-semibold transition ${
                    emailStatusFilter === st
                      ? "bg-[#101B82] text-white"
                      : "bg-[#211917]/5 text-[#211917]/70 hover:bg-[#211917]/10"
                  }`}
                >
                  {st}
                </button>
              ))}
            </div>
          </div>

          {/* Emails List */}
          <div className="overflow-hidden rounded-xl border border-[#211917]/10 bg-white shadow-sm">
            {emailLoading ? (
              <div className="p-12 text-center text-sm text-[#211917]/60">
                Loading lodge email responses...
              </div>
            ) : filteredEmails.length === 0 ? (
              <div className="p-12 text-center">
                <EnvelopeSimple size={40} className="mx-auto text-[#211917]/20" />
                <p className="mt-2 text-sm font-semibold text-[#211917]">No emails found</p>
                <p className="text-xs text-[#211917]/60">
                  Lodge inquiries and reply confirmations fetched from the operational IMAP mailbox appear here.
                </p>
              </div>
            ) : (
              <div className="divide-y divide-[#211917]/5">
                {filteredEmails.map((em) => (
                  <div
                    key={em.id}
                    onClick={() => setSelectedEmail(em)}
                    className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 p-4 transition hover:bg-[#101B82]/[0.02] cursor-pointer"
                  >
                    <div className="flex items-start gap-3 min-w-0">
                      <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-[#101B82]/10 text-[#101B82]">
                        <EnvelopeSimple size={20} weight="duotone" />
                      </div>
                      <div className="min-w-0">
                        <div className="flex items-center gap-2 flex-wrap">
                          <span className="font-semibold text-sm text-[#211917]">
                            {em.fromEmail}
                          </span>
                          {em.bookingReference && (
                            <span className="font-mono text-[11px] rounded bg-blue-50 px-2 py-0.5 text-blue-800 font-semibold border border-blue-200">
                              {em.bookingReference}
                            </span>
                          )}
                          {em.propertyName && (
                            <span className="text-xs text-[#101B82] font-medium bg-[#101B82]/5 px-2 py-0.5 rounded">
                              {em.propertyName}
                            </span>
                          )}
                        </div>
                        <p className="mt-1 font-semibold text-sm text-[#211917] truncate">
                          {em.subject}
                        </p>
                        <p className="mt-0.5 text-xs text-[#211917]/60 line-clamp-1">
                          {em.body}
                        </p>
                      </div>
                    </div>

                    <div className="flex items-center gap-3 shrink-0 self-end sm:self-center">
                      <div className="text-right">
                        <p className="text-xs font-semibold text-[#211917]">
                          {em.receivedAt ? new Date(em.receivedAt).toLocaleDateString() : "Recent"}
                        </p>
                        <p className="text-[11px] text-[#211917]/50">
                          {em.receivedAt ? new Date(em.receivedAt).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }) : ""}
                        </p>
                      </div>

                      <span
                        className={`inline-flex items-center rounded-full px-2 py-0.5 text-[10px] font-bold ${
                          em.processed
                            ? "bg-emerald-100 text-emerald-800"
                            : "bg-amber-100 text-amber-800"
                        }`}
                      >
                        {em.processed ? "Processed" : "Pending"}
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      )}

      {/* ========================================================
          MODAL: EMAIL READER
          ======================================================== */}
      {selectedEmail && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
          <div className="relative w-full max-w-2xl overflow-hidden rounded-2xl bg-white shadow-2xl">
            {/* Modal Header */}
            <div className="flex items-center justify-between border-b border-[#211917]/10 bg-[#F8F8FC] px-6 py-4">
              <div className="min-w-0">
                <h3 className="font-serif-title text-lg font-bold text-[#101B82] truncate">
                  {selectedEmail.subject}
                </h3>
                <p className="text-xs text-[#211917]/60">
                  Received on {selectedEmail.receivedAt ? new Date(selectedEmail.receivedAt).toLocaleString() : "Unknown"}
                </p>
              </div>
              <button
                onClick={() => setSelectedEmail(null)}
                className="flex h-8 w-8 items-center justify-center rounded-full text-[#211917]/50 transition hover:bg-[#211917]/10 hover:text-[#211917]"
              >
                <X size={18} />
              </button>
            </div>

            {/* Email Metadata */}
            <div className="border-b border-[#211917]/10 bg-white px-6 py-3 space-y-1.5 text-xs">
              <div className="flex items-center gap-2">
                <span className="w-16 font-bold uppercase text-[#211917]/50">From:</span>
                <span className="font-semibold text-[#211917]">{selectedEmail.fromEmail}</span>
              </div>
              <div className="flex items-center gap-2">
                <span className="w-16 font-bold uppercase text-[#211917]/50">To:</span>
                <span className="text-[#211917]/80">{selectedEmail.toEmail || "operations@safarisales.com"}</span>
              </div>
              {selectedEmail.bookingReference && (
                <div className="flex items-center gap-2">
                  <span className="w-16 font-bold uppercase text-[#211917]/50">Booking:</span>
                  <span className="font-mono font-bold text-blue-700">{selectedEmail.bookingReference}</span>
                  {selectedEmail.propertyName && (
                    <span className="text-[#211917]/60">({selectedEmail.propertyName})</span>
                  )}
                </div>
              )}
            </div>

            {/* Email Body */}
            <div className="max-h-[50vh] overflow-y-auto p-6 bg-[#FAFAFC] text-sm text-[#211917] leading-relaxed whitespace-pre-wrap font-sans">
              {selectedEmail.body || "(No message body content)"}
            </div>

            {/* Modal Footer */}
            <div className="flex items-center justify-end border-t border-[#211917]/10 bg-white px-6 py-3">
              <button
                onClick={() => setSelectedEmail(null)}
                className="rounded-lg bg-[#101B82] px-4 py-2 text-xs font-semibold text-white transition hover:bg-[#0c1566]"
              >
                Close Message
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ========================================================
          MODAL: PRINTABLE LODGE VOUCHER & INVOICE
          ======================================================== */}
      {selectedInvoice && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4">
          <div className="relative w-full max-w-3xl overflow-hidden rounded-2xl bg-white shadow-2xl flex flex-col max-h-[90vh]">
            {/* Header Toolbar */}
            <div className="flex items-center justify-between border-b border-[#211917]/10 bg-[#F8F8FC] px-6 py-3.5 print:hidden">
              <span className="font-serif-title font-bold text-base text-[#101B82]">
                Booking Invoice & Voucher: {selectedInvoice.invoiceNumber}
              </span>
              <div className="flex items-center gap-2">
                <button
                  onClick={() => window.print()}
                  className="inline-flex items-center gap-1.5 rounded-lg border border-[#101B82]/20 bg-white px-3 py-1.5 text-xs font-semibold text-[#101B82] shadow-sm transition hover:bg-[#101B82]/5"
                >
                  <Printer size={16} />
                  Print / Save PDF
                </button>
                <button
                  onClick={() => setSelectedInvoice(null)}
                  className="flex h-8 w-8 items-center justify-center rounded-full text-[#211917]/50 transition hover:bg-[#211917]/10 hover:text-[#211917]"
                >
                  <X size={18} />
                </button>
              </div>
            </div>

            {/* Printable Document Body */}
            <div className="overflow-y-auto p-8 font-serif print:p-0">
              {/* Document Header */}
              <div className="flex justify-between items-start border-b-2 border-[#101B82] pb-6">
                <div>
                  <h2 className="font-serif-title text-2xl font-bold tracking-tight text-[#101B82]">
                    SAFARI OPERATIONS
                  </h2>
                  <p className="text-xs text-[#211917]/60">Lodge Accommodation Voucher & Billing Invoice</p>
                  <p className="mt-2 text-xs text-[#211917]/70 font-mono">
                    REF: {selectedInvoice.bookingReference || "ACC-BOOKING"}
                  </p>
                </div>

                <div className="text-right">
                  <span
                    className={`inline-block border-2 px-3 py-1 text-xs font-bold uppercase tracking-wider rounded ${
                      selectedInvoice.status === "PAID"
                        ? "border-emerald-600 text-emerald-700 bg-emerald-50"
                        : "border-amber-600 text-amber-700 bg-amber-50"
                    }`}
                  >
                    {selectedInvoice.status}
                  </span>
                  <p className="mt-2 text-xs font-semibold text-[#211917]">
                    Invoice: <span className="font-mono">{selectedInvoice.invoiceNumber}</span>
                  </p>
                  <p className="text-xs text-[#211917]/60">
                    Issued: {selectedInvoice.issuedAt ? new Date(selectedInvoice.issuedAt).toLocaleDateString() : new Date().toLocaleDateString()}
                  </p>
                  <p className="text-xs text-[#211917]/60">
                    Due Date: {selectedInvoice.dueDate || "Upon Receipt"}
                  </p>
                </div>
              </div>

              {/* Lodge & Guest Details Grid */}
              <div className="mt-6 grid grid-cols-2 gap-6 bg-[#F8F8FC] p-4 rounded-xl border border-[#211917]/10 text-xs">
                <div>
                  <p className="font-bold uppercase tracking-wider text-[#211917]/50 mb-1">
                    Lodge / Property
                  </p>
                  <p className="text-sm font-bold text-[#101B82]">
                    {selectedInvoice.propertyName || "Accommodation Partner"}
                  </p>
                  <p className="text-[#211917]/70 mt-1">Confirmed Lodge Reservation</p>
                </div>

                <div>
                  <p className="font-bold uppercase tracking-wider text-[#211917]/50 mb-1">
                    Safari & Guest Reference
                  </p>
                  <p className="text-sm font-bold text-[#211917]">
                    {selectedInvoice.clientName || "Direct Guest"}
                  </p>
                  <p className="text-[#211917]/70 mt-1 font-mono">
                    Safari: {selectedInvoice.safariReference || "—"}
                  </p>
                </div>
              </div>

              {/* Line Items Table */}
              <div className="mt-6">
                <table className="w-full text-left text-xs border-collapse">
                  <thead>
                    <tr className="border-b border-[#211917]/20 bg-[#101B82]/5 text-[#101B82] font-bold uppercase">
                      <th className="py-2.5 px-3">Description</th>
                      <th className="py-2.5 px-3 text-right">Status</th>
                      <th className="py-2.5 px-3 text-right">Amount</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-[#211917]/10">
                    <tr>
                      <td className="py-3 px-3">
                        <p className="font-bold text-[#211917]">
                          Accommodation Allocation & Booking Reservation
                        </p>
                        <p className="text-[#211917]/60 text-[11px]">
                          Property: {selectedInvoice.propertyName || "Lodge Partner"}
                        </p>
                      </td>
                      <td className="py-3 px-3 text-right font-medium">
                        {selectedInvoice.status}
                      </td>
                      <td className="py-3 px-3 text-right font-bold text-sm text-[#101B82]">
                        ${Number(selectedInvoice.amount || 0).toLocaleString("en-US", { minimumFractionDigits: 2, maximumFractionDigits: 2 })} {selectedInvoice.currency || "USD"}
                      </td>
                    </tr>
                  </tbody>
                  <tfoot>
                    <tr className="border-t-2 border-[#101B82] font-bold text-sm">
                      <td colSpan={2} className="py-3 px-3 text-right uppercase text-[#101B82]">
                        Total Amount Payable:
                      </td>
                      <td className="py-3 px-3 text-right text-base text-[#101B82]">
                        ${Number(selectedInvoice.amount || 0).toLocaleString("en-US", { minimumFractionDigits: 2, maximumFractionDigits: 2 })} {selectedInvoice.currency || "USD"}
                      </td>
                    </tr>
                  </tfoot>
                </table>
              </div>

              {/* Voucher Authorization Seal */}
              <div className="mt-8 border-t border-[#211917]/10 pt-4 flex justify-between items-end text-xs text-[#211917]/60">
                <div>
                  <p className="font-semibold text-[#211917]">Authorized Operations Desk</p>
                  <p>Electronically certified voucher & invoice document</p>
                </div>
                <div className="border border-dashed border-[#101B82]/30 rounded-lg p-2 text-center text-[10px] text-[#101B82]">
                  OFFICIAL SAFARI SYSTEM VOUCHER
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* ========================================================
          MODAL: RECORD MANUAL INVOICE
          ======================================================== */}
      {showCreateModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
          <div className="relative w-full max-w-lg rounded-2xl bg-white shadow-2xl overflow-hidden">
            <div className="flex items-center justify-between border-b border-[#211917]/10 bg-[#F8F8FC] px-6 py-4">
              <h3 className="font-serif-title text-lg font-bold text-[#101B82]">
                Record Booking Invoice
              </h3>
              <button
                onClick={() => setShowCreateModal(false)}
                className="flex h-8 w-8 items-center justify-center rounded-full text-[#211917]/50 transition hover:bg-[#211917]/10 hover:text-[#211917]"
              >
                <X size={18} />
              </button>
            </div>

            <form onSubmit={handleCreateInvoice} className="p-6 space-y-4 text-xs">
              <div>
                <label className="block font-bold uppercase text-[#211917]/70 mb-1">
                  Accommodation Booking *
                </label>
                <select
                  required
                  value={createForm.bookingId}
                  onChange={(e) => setCreateForm({ ...createForm, bookingId: e.target.value })}
                  className="w-full rounded-lg border border-[#211917]/20 p-2.5 text-xs text-[#211917] outline-none focus:border-[#101B82]"
                >
                  <option value="">-- Select Confirmed / Allocated Booking --</option>
                  {bookingsList.map((b) => (
                    <option key={b.id} value={b.id}>
                      {b.referenceNumber} — {b.propertyName} ({b.safariReference || "Safari"})
                    </option>
                  ))}
                </select>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block font-bold uppercase text-[#211917]/70 mb-1">
                    Invoice Number (Optional)
                  </label>
                  <input
                    type="text"
                    placeholder="Auto-generated if empty"
                    value={createForm.invoiceNumber}
                    onChange={(e) => setCreateForm({ ...createForm, invoiceNumber: e.target.value })}
                    className="w-full rounded-lg border border-[#211917]/20 p-2.5 text-xs text-[#211917] outline-none focus:border-[#101B82]"
                  />
                </div>

                <div>
                  <label className="block font-bold uppercase text-[#211917]/70 mb-1">
                    Due Date
                  </label>
                  <input
                    type="date"
                    value={createForm.dueDate}
                    onChange={(e) => setCreateForm({ ...createForm, dueDate: e.target.value })}
                    className="w-full rounded-lg border border-[#211917]/20 p-2.5 text-xs text-[#211917] outline-none focus:border-[#101B82]"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block font-bold uppercase text-[#211917]/70 mb-1">
                    Amount *
                  </label>
                  <input
                    type="number"
                    step="0.01"
                    required
                    placeholder="e.g. 850.00"
                    value={createForm.amount}
                    onChange={(e) => setCreateForm({ ...createForm, amount: e.target.value })}
                    className="w-full rounded-lg border border-[#211917]/20 p-2.5 text-xs text-[#211917] outline-none focus:border-[#101B82]"
                  />
                </div>

                <div>
                  <label className="block font-bold uppercase text-[#211917]/70 mb-1">
                    Currency
                  </label>
                  <input
                    type="text"
                    value={createForm.currency}
                    onChange={(e) => setCreateForm({ ...createForm, currency: e.target.value })}
                    className="w-full rounded-lg border border-[#211917]/20 p-2.5 text-xs text-[#211917] outline-none focus:border-[#101B82]"
                  />
                </div>
              </div>

              <div>
                <label className="block font-bold uppercase text-[#211917]/70 mb-1">
                  Status
                </label>
                <select
                  value={createForm.status}
                  onChange={(e) => setCreateForm({ ...createForm, status: e.target.value })}
                  className="w-full rounded-lg border border-[#211917]/20 p-2.5 text-xs text-[#211917] outline-none focus:border-[#101B82]"
                >
                  <option value="PENDING">PENDING</option>
                  <option value="PAID">PAID</option>
                  <option value="OVERDUE">OVERDUE</option>
                  <option value="CANCELLED">CANCELLED</option>
                </select>
              </div>

              <div className="flex items-center justify-end gap-2 border-t border-[#211917]/10 pt-4">
                <button
                  type="button"
                  onClick={() => setShowCreateModal(false)}
                  className="rounded-lg border border-[#211917]/20 px-4 py-2 font-semibold text-[#211917]/70 hover:bg-[#211917]/5"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submittingInvoice}
                  className="rounded-lg bg-[#101B82] px-5 py-2 font-semibold text-white hover:bg-[#0c1566] disabled:opacity-50"
                >
                  {submittingInvoice ? "Saving..." : "Record Invoice"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default MailboxView;
