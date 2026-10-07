import { useState, useEffect, useCallback } from "react";
import { useParams } from "react-router-dom";
import { Plus } from "@phosphor-icons/react";

import DynamicTable from "../components/tables/DynamicTable";
import DedicatedFormView from "../components/forms/DedicatedFormView";
import AccommodationFormView from "../components/forms/AccommodationFormView";
import SubFormView from "../components/forms/SubFormView";
import ResourceDetailView from "../components/views/ResourceDetailView";
import BookingModal from "../components/bookings/BookingModal";
import AlertModal from "../components/feedback/AlertModal";
import ConfirmModal from "../components/feedback/ConfirmModal";

import { useAuth } from "../hooks/useAuth";
import {
  getAllData,
  createData,
  updateData,
  deleteData,
  apiRequest,
} from "../api/api";

const ResourcePage = ({
  title,
  singular,
  resourceName,
  fields = [],
  table = {},
  permissions = {},
  endpoint,
  createEndpoint,
  updateEndpoint,
  deleteEndpoint,
}) => {
  const { token, user, can } = useAuth();
  const { resource: routeResource } = useParams();
  const resource = resourceName || routeResource;

  const singularName =
    singular ||
    (resource === "clients"
      ? "Client"
      : resource === "safaris"
      ? "Safari"
      : resource === "properties"
      ? "Property"
      : resource === "property-categories"
      ? "Category"
      : resource === "price-tiers"
      ? "Price Tier"
      : resource === "room-types"
      ? "Room Type"
      : resource === "users"
      ? "User"
      : resource === "companies"
      ? "Company"
      : title?.endsWith("ies")
      ? `${title.slice(0, -3)}y`
      : title?.endsWith("s")
      ? title.slice(0, -1)
      : title || "Record");

  // Navigation / View Mode: "list" | "detail" | "form" | "accommodation-form" | "sub-form"
  const [viewMode, setViewMode] = useState("list");

  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(false);
  const [actionLoading, setActionLoading] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");
  const [statusFilter, setStatusFilter] = useState(
    table?.filters?.find((f) => f.key === "status")?.defaultValue || ""
  );
  const [page, setPage] = useState(0);
  const [pageInfo, setPageInfo] = useState({
    totalPages: 1,
    totalElements: 0,
    number: 0,
    size: 20,
  });

  // Dynamic dropdown options (for categoryId, priceId, clientId, etc.)
  const [dynamicOptions, setDynamicOptions] = useState({});

  // Active Selected Item for Detail View
  const [selectedItem, setSelectedItem] = useState(null);
  const [subData, setSubData] = useState({ guests: [], flights: [], days: [], requirements: [], bookings: [] });
  const [activeTab, setActiveTab] = useState("overview");

  // Form Configurations
  const [formConfig, setFormConfig] = useState({
    mode: "create", // "create" | "edit"
    initialValues: {},
    title: "",
  });

  const [subFormConfig, setSubFormConfig] = useState({
    type: "", // "guest" | "flight" | "itineraryDay"
    title: "",
    initialValues: {},
  });

  const [accommodationConfig, setAccommodationConfig] = useState({
    mode: "create", // "create" | "edit"
    itineraryDay: null,
    initialValues: {},
  });

  const [reqForm, setReqForm] = useState({
    categoryId: "",
    pricetierId: "",
    numberOfrooms: 1,
    roomPreferences: "",
    specialRequests: "",
    roomRequirements: [{ roomType: "", quantity: 1 }],
  });

  // Popups strictly for Alerts and Confirmations
  const [alertModal, setAlertModal] = useState({
    open: false,
    title: "",
    message: "",
    type: "info", // "info" | "success" | "error" | "warning"
  });

  const [deleteModal, setDeleteModal] = useState({
    open: false,
    item: null,
  });

  // Modal to allocate lodge & create accommodation booking from safari itinerary
  const [bookingModal, setBookingModal] = useState({
    open: false,
    requirement: null,
    day: null,
  });

  const apiBasePath = endpoint || `/${resource}`;

  // Fetch dynamic options (categories, price tiers, clients, room types)
  useEffect(() => {
    const fetchOptions = async () => {
      try {
        if (resource === "properties") {
          const [catRes, tierRes, amenRes, tagRes] = await Promise.allSettled([
            getAllData("/property-category", token),
            getAllData("/price-tier", token),
            getAllData("/amenities", token),
            getAllData("/tags", token),
          ]);
          setDynamicOptions({
            categoryId: (catRes.status === "fulfilled" ? catRes.value?.data || catRes.value || [] : []).map((c) => ({
              label: c.name,
              value: c.id,
            })),
            priceId: (tierRes.status === "fulfilled" ? tierRes.value?.data || tierRes.value || [] : []).map((t) => ({
              label: `${t.name} (${t.currency} ${t.minPrice} - ${t.maxPrice})`,
              value: t.id,
            })),
            amenityIds: (amenRes.status === "fulfilled" ? amenRes.value?.data || amenRes.value || [] : []).map((a) => ({
              label: a.name,
              description: a.description,
              value: a.id,
            })),
            tagIds: (tagRes.status === "fulfilled" ? tagRes.value?.data || tagRes.value || [] : []).map((t) => ({
              label: `${t.name}${t.tagType ? ` (${t.tagType.replace("_", " ")})` : ""}`,
              description: t.description,
              tagType: t.tagType,
              value: t.id,
            })),
          });
        } else if (resource === "safaris") {
          const [clientRes, catRes, tierRes, roomRes] = await Promise.allSettled([
            getAllData("/clients?size=100", token),
            getAllData("/property-category", token),
            getAllData("/price-tier", token),
            getAllData("/api/v1/room-types", token),
          ]);
          const clientList = clientRes.status === "fulfilled" ? (clientRes.value?.content || clientRes.value?.data || clientRes.value || []) : [];
          const categories = catRes.status === "fulfilled" ? (catRes.value?.data || catRes.value || []) : [];
          const tiers = tierRes.status === "fulfilled" ? (tierRes.value?.data || tierRes.value || []) : [];
          const rooms = roomRes.status === "fulfilled" ? (roomRes.value?.data || roomRes.value || []) : [];

          setDynamicOptions({
            clientId: clientList.map((c) => ({
              label: `${c.firstName} ${c.lastName} (${c.email || ""})`,
              value: c.id,
            })),
            categories: categories.map((c) => ({ label: c.name, value: c.id })),
            priceTiers: tiers.map((t) => ({
              label: `${t.name}${t.minPrice != null ? ` (${t.currency || "$"} ${t.minPrice} - ${t.maxPrice})` : ""}`,
              value: t.id,
            })),
            roomTypes: rooms.map((r) => ({ label: r.name, value: r.name })),
          });
        } else if (resource === "companies") {
          let plansRes = await getAllData("/api/subscription-plans/active", token).catch(() => null);
          let plansList = plansRes?.data || (Array.isArray(plansRes) ? plansRes : []);
          if (!plansList || plansList.length === 0) {
            const allPlansRes = await getAllData("/api/subscription-plans", token).catch(() => null);
            plansList = allPlansRes?.data || (Array.isArray(allPlansRes) ? allPlansRes : []);
          }
          if (!plansList || plansList.length === 0) {
            plansList = [
              { name: "Starter Plan", currency: "$", price: 49, maxUsers: 3 },
              { name: "Professional Plan", currency: "$", price: 149, maxUsers: 15 },
              { name: "Enterprise Plan", currency: "$", price: 399, maxUsers: 100 },
            ];
          }
          setDynamicOptions({
            subscription_plan: plansList.map((p) => ({
              label: `${p.name} (${p.currency || "$"}${Number(p.price || 0).toLocaleString()}/mo - max ${p.maxUsers || "unlimited"} users)`,
              value: p.name,
            })),
          });
        }
      } catch (err) {
        console.error("Failed to load select options:", err);
      }
    };

    if (token) {
      fetchOptions();
    }
  }, [resource, token]);

  // Load Main Table Data
  const loadData = useCallback(async () => {
    setLoading(true);
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

      const queryString = params.toString();
      const separator = apiBasePath.includes("?") ? "&" : "?";
      const fullUrl = `${apiBasePath}${queryString ? `${separator}${queryString}` : ""}`;

      const data = await getAllData(fullUrl, token);

      let result = [];
      let totalPages = 1;
      let totalElements = 0;
      let pageNumber = page;

      if (Array.isArray(data)) {
        result = data;
        totalElements = data.length;
      } else if (Array.isArray(data?.content)) {
        result = data.content;
        totalPages = data.totalPages ?? 1;
        totalElements = data.totalElements ?? result.length;
        pageNumber = data.number ?? page;
      } else if (Array.isArray(data?.data?.content)) {
        result = data.data.content;
        totalPages = data.data.totalPages ?? 1;
        totalElements = data.data.totalElements ?? result.length;
        pageNumber = data.data.number ?? page;
      } else if (Array.isArray(data?.data)) {
        result = data.data;
        totalElements = data.data.length;
      }

      setItems(result);
      setPageInfo({
        totalPages,
        totalElements,
        number: pageNumber,
        size: 20,
      });
    } catch {
      setItems([]);
      setPageInfo({ totalPages: 1, totalElements: 0, number: 0, size: 20 });
    } finally {
      setLoading(false);
    }
  }, [apiBasePath, page, searchTerm, statusFilter, token]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  // Load Subdata for Details (Safaris: Days & Requirements, Clients: Guests & Flights)
  const loadSubDataForView = async (item) => {
    try {
      if (resource === "clients") {
        const [guestsRes, flightsRes] = await Promise.allSettled([
          getAllData(`/client/${item.id}/guests`, token),
          getAllData(`/client-flight/${item.id}/view`, token),
        ]);

        setSubData({
          guests: guestsRes.status === "fulfilled" ? guestsRes.value?.data || guestsRes.value || [] : [],
          flights: flightsRes.status === "fulfilled" ? flightsRes.value?.data || flightsRes.value || [] : [],
          days: [],
          requirements: [],
          bookings: [],
        });
      } else if (resource === "safaris") {
        const clientId = item.clientId || item.client?.id;
        const [daysRes, reqsRes, bookingsRes, guestsRes, flightsRes] = await Promise.allSettled([
          getAllData(`/safari/${item.id}/itineraryDays`, token),
          getAllData(`/api/v1/accommodation-requirements/safari/${item.id}`, token),
          getAllData(`/accommodation-bookings/by-safari/${item.id}`, token),
          clientId ? getAllData(`/client/${clientId}/guests`, token) : Promise.resolve([]),
          clientId ? getAllData(`/client-flight/${clientId}/view`, token) : Promise.resolve([]),
        ]);
        const days = daysRes.status === "fulfilled" ? daysRes.value?.data || daysRes.value || [] : [];
        const reqs = reqsRes.status === "fulfilled" ? reqsRes.value?.data || reqsRes.value || [] : [];
        const bookings = bookingsRes.status === "fulfilled" ? bookingsRes.value?.data || bookingsRes.value || [] : [];

        setSubData({
          days,
          requirements: reqs,
          bookings,
          guests: guestsRes.status === "fulfilled" ? guestsRes.value?.data || guestsRes.value || [] : [],
          flights: flightsRes.status === "fulfilled" ? flightsRes.value?.data || flightsRes.value || [] : [],
        });

        // Dynamically compute and reflect safari status
        if (days.length > 0) {
          const allDestSet = days.every(
            (d) => d.destination && !d.destination.toLowerCase().includes("not set") && !d.destination.toLowerCase().includes("pending")
          );
          const allBooked = allDestSet && days.every((d) => {
            const req = reqs.find((r) => String(r.itineraryDayId) === String(d.id));
            const b = bookings.find(
              (bk) => (req && bk.requirementId && String(bk.requirementId) === String(req.id)) || (bk.checkIn && bk.checkIn === d.date)
            );
            return (b && String(bk.status || "").toUpperCase() === "CONFIRMED") || (req && (String(req.status || "").toUpperCase() === "COMPLETED" || String(req.status || "").toUpperCase() === "CONFIRMED"));
          });
          const computedStatus = allBooked ? "COMPLETED" : allDestSet ? "CONFIRMED" : "DRAFT";
          setSelectedItem((prev) => (prev ? { ...prev, status: computedStatus } : prev));
        }
      } else if (resource === "properties") {
        try {
          const propRes = await getAllData(`/properties/${item.id}`, token);
          if (propRes) {
            const detailedProp = propRes?.data || propRes;
            setSelectedItem(detailedProp);
          }
        } catch (e) {
          console.warn("Could not reload detailed property:", e);
        }
      }
    } catch (err) {
      console.error("Failed to load subdata:", err);
    }
  };

  // Actions from Table
  const handleAction = async (actionKey, item) => {
    if (actionKey === "view") {
      const defaultTab =
        resource === "safaris"
          ? "itinerary"
          : resource === "clients"
          ? "guests"
          : "overview";

      setSelectedItem(item);
      setActiveTab(defaultTab);
      setViewMode("detail");
      loadSubDataForView(item);
    } else if (actionKey === "update") {
      let initialValues = { ...item };
      if (resource === "properties") {
        initialValues.amenityIds = Array.isArray(item.amenities)
          ? item.amenities.map((a) => a.id)
          : item.amenityIds || [];
        initialValues.tagIds = Array.isArray(item.tags)
          ? item.tags.map((t) => t.id)
          : item.tagIds || [];
      }
      setFormConfig({
        mode: "edit",
        initialValues: initialValues,
        title: `Edit ${singularName}`,
      });
      setViewMode("form");
    } else if (actionKey === "delete") {
      setDeleteModal({
        open: true,
        item,
      });
    }
  };

  // Form Submit Handler (Create & Edit Record)
  const handleFormSubmit = async (formData) => {
    setActionLoading(true);
    try {
      if (formConfig.mode === "create") {
        let postUrl = createEndpoint || apiBasePath;
        let payload = { ...formData };

        if (resource === "clients") {
          const salesPersonId = user?.id || user?.sub;
          postUrl = `/clients/${salesPersonId}`;
        } else if (resource === "safaris") {
          if (!formData.clientId) {
            throw new Error("Please select a client for this safari.");
          }
          postUrl = `/safari/${formData.clientId}/create`;
          delete payload.clientId;
        } else if (resource === "room-types") {
          postUrl = `/api/v1/room-types?name=${encodeURIComponent(formData.name)}`;
        } else if (resource === "users") {
          if (!payload.companyName && user?.companyName) {
            payload.companyName = user.companyName;
          }
          if (!payload.companyName) {
            delete payload.companyName;
          }
        }

        await createData(postUrl, payload, token);
        setViewMode("list");
        setAlertModal({
          open: true,
          title: "Created Successfully",
          message: `${singularName} has been created.`,
          type: "success",
        });
        loadData();
      } else {
        // Edit mode
        let putUrl = `${updateEndpoint || apiBasePath}/${formConfig.initialValues.id}`;
        let payload = { ...formData };

        if (resource === "clients") {
          putUrl = `/client/${formConfig.initialValues.id}`;
        } else if (resource === "room-types") {
          putUrl = `/api/v1/room-types/${formConfig.initialValues.id}?name=${encodeURIComponent(formData.name)}`;
        } else if (resource === "users") {
          putUrl = `/api/users/user/${formConfig.initialValues.id}`;
        }

        await updateData(putUrl, payload, token);

        if (selectedItem && selectedItem.id === formConfig.initialValues.id) {
          setSelectedItem((prev) => ({ ...prev, ...payload }));
          setViewMode("detail");
        } else {
          setViewMode("list");
        }

        setAlertModal({
          open: true,
          title: "Updated Successfully",
          message: `${singularName} has been updated.`,
          type: "success",
        });
        loadData();
      }
    } catch (error) {
      setAlertModal({
        open: true,
        title: "Error",
        message: error.message || "Operation failed",
        type: "error",
      });
    } finally {
      setActionLoading(false);
    }
  };

  // Accommodation Requirements Handlers
  const handleAddRequirement = (day) => {
    const defaultCat = dynamicOptions.categories?.[0]?.value || "";
    const defaultTier = dynamicOptions.priceTiers?.[0]?.value || "";
    const defaultRoom = dynamicOptions.roomTypes?.[0]?.value || "Standard";

    setAccommodationConfig({
      mode: "create",
      itineraryDay: day,
      initialValues: {},
    });
    setReqForm({
      categoryId: defaultCat,
      pricetierId: defaultTier,
      numberOfrooms: 1,
      roomPreferences: "",
      specialRequests: "",
      roomRequirements: [{ roomType: defaultRoom, quantity: 1 }],
    });
    setViewMode("accommodation-form");
  };

  const handleEditRequirement = (day, req) => {
    setAccommodationConfig({
      mode: "edit",
      itineraryDay: day,
      initialValues: req,
    });
    setReqForm({
      categoryId: req.categoryId || "",
      pricetierId: req.pricetierId || "",
      numberOfrooms: req.numberOfrooms || 1,
      roomPreferences: req.roomPreferences || "",
      specialRequests: req.specialRequests || "",
      roomRequirements: req.roomRequirements?.length
        ? req.roomRequirements.map((r) => ({ roomType: r.roomType, quantity: r.quantity }))
        : [{ roomType: dynamicOptions.roomTypes?.[0]?.value || "Standard", quantity: 1 }],
    });
    setViewMode("accommodation-form");
  };

  const handleDeleteRequirement = async (reqId) => {
    if (!window.confirm("Are you sure you want to delete this accommodation requirement?")) return;
    setActionLoading(true);
    try {
      await deleteData(`/api/v1/accommodation-requirements/${reqId}`, token);
      setAlertModal({
        open: true,
        title: "Requirement Deleted",
        message: "Accommodation requirement deleted successfully.",
        type: "success",
      });
      if (selectedItem) {
        loadSubDataForView(selectedItem);
      }
    } catch (err) {
      setAlertModal({
        open: true,
        title: "Error",
        message: err.message || "Failed to delete requirement",
        type: "error",
      });
    } finally {
      setActionLoading(false);
    }
  };

  const handleAccommodationSubmit = async (e) => {
    e.preventDefault();
    if (!reqForm.categoryId) {
      setAlertModal({
        open: true,
        title: "Missing Category",
        message: "Please select a property category.",
        type: "error",
      });
      return;
    }
    if (!reqForm.pricetierId) {
      setAlertModal({
        open: true,
        title: "Missing Price Tier",
        message: "Please select a price tier.",
        type: "error",
      });
      return;
    }

    const numRooms = parseInt(reqForm.numberOfrooms, 10) || 1;
    const totalAssigned = reqForm.roomRequirements.reduce(
      (sum, r) => sum + (parseInt(r.quantity, 10) || 0),
      0
    );

    if (totalAssigned !== numRooms) {
      setAlertModal({
        open: true,
        title: "Validation Error",
        message: `Total room quantities (${totalAssigned}) must equal the number of rooms (${numRooms}).`,
        type: "error",
      });
      return;
    }

    setActionLoading(true);
    try {
      const payload = {
        categoryId: reqForm.categoryId,
        pricetierId: reqForm.pricetierId,
        numberOfrooms: numRooms,
        roomPreferences: reqForm.roomPreferences || "",
        specialRequests: reqForm.specialRequests || "",
        roomRequirements: reqForm.roomRequirements.map((r) => ({
          roomType: r.roomType,
          quantity: parseInt(r.quantity, 10) || 1,
        })),
      };

      if (accommodationConfig.mode === "create") {
        await createData(
          `/api/v1/accommodation-requirements/itinerary-day/${accommodationConfig.itineraryDay.id}`,
          payload,
          token
        );
      } else {
        await updateData(
          `/api/v1/accommodation-requirements/${accommodationConfig.initialValues.id}`,
          payload,
          token
        );
      }

      setViewMode("detail");
      setAlertModal({
        open: true,
        title: "Success",
        message: "Accommodation requirement saved successfully!",
        type: "success",
      });

      if (selectedItem) {
        loadSubDataForView(selectedItem);
      }
    } catch (err) {
      setAlertModal({
        open: true,
        title: "Error",
        message: err.message || "Failed to save accommodation requirement",
        type: "error",
      });
    } finally {
      setActionLoading(false);
    }
  };

  // Accommodation Booking Handlers
  const handleBookLodge = (req, day) => {
    const normalizedRole = String(user?.role_name || user?.role || "").toUpperCase().replace(/^ROLE_/, "").trim();
    const isSalesPerson = ["SALES_PERSON", "SALE", "SALES", "SALESPERSON"].includes(normalizedRole);

    if (isSalesPerson) {
      setAlertModal({
        open: true,
        title: "Access Restricted",
        message: "Sales personnel are not authorized to allocate lodges or generate accommodation bookings. Please contact your Reservation Manager.",
        type: "error",
      });
      return;
    }

    const isReqConfirmed = req && (
      String(req.status || "").toUpperCase() === "COMPLETED" ||
      String(req.status || "").toUpperCase() === "CONFIRMED"
    );
    const existingConfirmedBooking = (subData?.bookings || []).find((b) =>
      req && String(b.requirementId) === String(req.id) &&
      String(b.status || "").toUpperCase() === "CONFIRMED"
    );

    if (isReqConfirmed || existingConfirmedBooking) {
      setAlertModal({
        open: true,
        title: "Booking Already Confirmed",
        message: `This accommodation requirement is already booked and confirmed${existingConfirmedBooking?.propertyName ? ` with '${existingConfirmedBooking.propertyName}'` : ""}.`,
        type: "info",
      });
      return;
    }

    setBookingModal({
      open: true,
      requirement: req,
      day: day,
    });
  };

  const handleBookingSuccess = (booking) => {
    setAlertModal({
      open: true,
      title: "Lodge Booked Successfully",
      message: `Booking ${booking.referenceNumber} has been created for ${booking.propertyName}.`,
      type: "success",
    });
    if (selectedItem) {
      loadSubDataForView(selectedItem);
    }
    loadData();
  };

  const handleSendBooking = async () => {
    if (!selectedItem) return;
    setActionLoading(true);
    try {
      const res = await apiRequest(`/accommodation-bookings/${selectedItem.id}/send`, {
        method: "POST",
      }, token);
      setSelectedItem(res);
      setAlertModal({
        open: true,
        title: "Booking Request Sent",
        message: `Booking ${res.referenceNumber} inquiry has been dispatched to ${res.propertyName}.`,
        type: "success",
      });
      loadData();
    } catch (err) {
      setAlertModal({
        open: true,
        title: "Dispatch Failed",
        message: err.message || "Failed to dispatch booking request.",
        type: "error",
      });
    } finally {
      setActionLoading(false);
    }
  };

  const handleConfirmBookingSuccess = (updated) => {
    setSelectedItem(updated);
    setAlertModal({
      open: true,
      title: "Booking Confirmed",
      message: `Booking ${updated.referenceNumber} has been verified and confirmed with code ${updated.confirmationNumber}.`,
      type: "success",
    });
    loadData();
  };

  const handleDeclineBooking = async () => {
    if (!selectedItem) return;
    if (!window.confirm("Are you sure you want to decline or cancel this accommodation booking?")) return;
    setActionLoading(true);
    try {
      const res = await apiRequest(`/accommodation-bookings/${selectedItem.id}/decline`, {
        method: "POST",
      }, token);
      setSelectedItem(res);
      setAlertModal({
        open: true,
        title: "Booking Cancelled",
        message: `Booking ${res.referenceNumber} has been marked as cancelled.`,
        type: "success",
      });
      loadData();
    } catch (err) {
      setAlertModal({
        open: true,
        title: "Decline Failed",
        message: err.message || "Failed to cancel booking.",
        type: "error",
      });
    } finally {
      setActionLoading(false);
    }
  };

  // Sub-Form (Guest, Flight, Day Destination) Submit
  const handleSubFormSubmit = async (formData) => {
    setActionLoading(true);
    try {
      if (!selectedItem) return;

      const targetClientId = subFormConfig.clientId || selectedItem.id;

      if (subFormConfig.type === "guest") {
        await createData(`/guest/${targetClientId}/client`, formData, token);
      } else if (subFormConfig.type === "flight") {
        const toIsoStringOrNull = (val) => {
          if (!val) return null;
          if (typeof val === "string") {
            const trimmed = val.trim();
            if (!trimmed) return null;
            const d = new Date(trimmed);
            return isNaN(d.getTime()) ? null : d.toISOString();
          }
          if (val instanceof Date) {
            return isNaN(val.getTime()) ? null : val.toISOString();
          }
          return null;
        };

        const payload = {
          ...formData,
          arrivalDatetime: toIsoStringOrNull(formData.arrivalDatetime),
          departureDatetime: toIsoStringOrNull(formData.departureDatetime),
        };
        await createData(`/client-flight/${targetClientId}/create`, payload, token);
      } else if (subFormConfig.type === "itineraryDay") {
        const payload = {
          destination: formData.destination || "",
          notes: formData.notes || "",
          date: formData.date || subFormConfig.initialValues?.date || null,
        };
        await updateData(
          `/safari/${selectedItem.id}/itinerary-days/${subFormConfig.initialValues.id}`,
          payload,
          token
        );
      }

      setViewMode("detail");
      setAlertModal({
        open: true,
        title: "Success",
        message: "Details saved successfully!",
        type: "success",
      });
      loadSubDataForView(selectedItem);
    } catch (error) {
      setAlertModal({
        open: true,
        title: "Error",
        message: error.message || "Failed to save details",
        type: "error",
      });
    } finally {
      setActionLoading(false);
    }
  };

  // Regenerate / Retry Itinerary Days
  const handleRegenerateDays = async () => {
    if (!selectedItem || !selectedItem.id) return;
    setActionLoading(true);
    try {
      const res = await createData(`/safari/${selectedItem.id}/regenerate-days`, {}, token);
      const days = res?.data || (Array.isArray(res) ? res : []);
      setSubData((prev) => ({ ...prev, days }));
      setAlertModal({
        open: true,
        title: "Days Synchronized",
        message: "Safari itinerary days have been successfully generated and synchronized.",
        type: "success",
      });
      loadSubDataForView(selectedItem);
      loadData();
    } catch (err) {
      setAlertModal({
        open: true,
        title: "Synchronization Failed",
        message: err.message || "Failed to generate itinerary days",
        type: "error",
      });
    } finally {
      setActionLoading(false);
    }
  };

  // Delete Action
  const confirmDelete = async () => {
    if (!deleteModal.item) return;
    setActionLoading(true);
    try {
      let delUrl = deleteEndpoint || apiBasePath;
      if (resource === "room-types") {
        delUrl = `/api/v1/room-types/${deleteModal.item.id}`;
      } else {
        delUrl = `${delUrl}/${deleteModal.item.id}`;
      }

      await deleteData(delUrl, token);

      if (selectedItem && selectedItem.id === deleteModal.item.id) {
        setSelectedItem(null);
        setViewMode("list");
      }

      setDeleteModal({ open: false, item: null });
      setAlertModal({
        open: true,
        title: "Deleted",
        message: `${singularName} deleted successfully`,
        type: "success",
      });
      loadData();
    } catch (error) {
      setAlertModal({
        open: true,
        title: "Error",
        message: error.message || "Failed to delete item",
        type: "error",
      });
    } finally {
      setActionLoading(false);
    }
  };

  // Property Verification Handler
  const handleVerifyProperty = async (propertyId) => {
    setActionLoading(true);
    try {
      await apiRequest(`/properties/${propertyId}/verify`, { method: "PATCH" }, token);
      setAlertModal({
        open: true,
        title: "Property Verified",
        message: "Property successfully verified and active for bookings!",
        type: "success",
      });
      if (selectedItem && selectedItem.id === propertyId) {
        setSelectedItem((prev) => ({
          ...prev,
          verificationStatus: "VERIFIED",
          verifiedAt: new Date().toISOString(),
          verifiedBy: user?.email || "Manager",
        }));
      }
      loadData();
    } catch (error) {
      setAlertModal({
        open: true,
        title: "Verification Failed",
        message: error.message || "Failed to verify property",
        type: "error",
      });
    } finally {
      setActionLoading(false);
    }
  };

  // Property Amenities & Tags Handlers
  const handleAddPropertyAmenity = async (amenityIds) => {
    if (!selectedItem) return;
    setActionLoading(true);
    try {
      const res = await apiRequest(`/properties/${selectedItem.id}/amenities`, {
        method: "POST",
        body: JSON.stringify(Array.isArray(amenityIds) ? amenityIds : [amenityIds]),
      }, token);
      const updated = res?.data || res;
      setSelectedItem(updated);
      setAlertModal({
        open: true,
        title: "Amenities Added",
        message: "Property amenities updated successfully.",
        type: "success",
      });
      loadData();
    } catch (error) {
      setAlertModal({
        open: true,
        title: "Action Failed",
        message: error.message || "Failed to add amenities",
        type: "error",
      });
    } finally {
      setActionLoading(false);
    }
  };

  const handleRemovePropertyAmenity = async (amenityId) => {
    if (!selectedItem) return;
    setActionLoading(true);
    try {
      const res = await apiRequest(`/properties/${selectedItem.id}/amenities/${amenityId}`, {
        method: "DELETE",
      }, token);
      const updated = res?.data || res;
      setSelectedItem(updated);
      loadData();
    } catch (error) {
      setAlertModal({
        open: true,
        title: "Action Failed",
        message: error.message || "Failed to remove amenity",
        type: "error",
      });
    } finally {
      setActionLoading(false);
    }
  };

  const handleAddPropertyTag = async (tagIds) => {
    if (!selectedItem) return;
    setActionLoading(true);
    try {
      const res = await apiRequest(`/properties/${selectedItem.id}/tags`, {
        method: "POST",
        body: JSON.stringify(Array.isArray(tagIds) ? tagIds : [tagIds]),
      }, token);
      const updated = res?.data || res;
      setSelectedItem(updated);
      setAlertModal({
        open: true,
        title: "Tags Added",
        message: "Property tags updated successfully.",
        type: "success",
      });
      loadData();
    } catch (error) {
      setAlertModal({
        open: true,
        title: "Action Failed",
        message: error.message || "Failed to add tags",
        type: "error",
      });
    } finally {
      setActionLoading(false);
    }
  };

  const handleRemovePropertyTag = async (tagId) => {
    if (!selectedItem) return;
    setActionLoading(true);
    try {
      const res = await apiRequest(`/properties/${selectedItem.id}/tags/${tagId}`, {
        method: "DELETE",
      }, token);
      const updated = res?.data || res;
      setSelectedItem(updated);
      loadData();
    } catch (error) {
      setAlertModal({
        open: true,
        title: "Action Failed",
        message: error.message || "Failed to remove tag",
        type: "error",
      });
    } finally {
      setActionLoading(false);
    }
  };

  // User Status Toggle (Activate / Deactivate)
  const handleToggleUserStatus = async (targetUser) => {
    const newStatus = targetUser.status === "ACTIVE" ? "INACTIVE" : "ACTIVE";
    const actionLabel = newStatus === "ACTIVE" ? "activate" : "deactivate";
    if (!window.confirm(`Are you sure you want to ${actionLabel} ${targetUser.firstName} ${targetUser.lastName}?`)) return;

    setActionLoading(true);
    try {
      await apiRequest(`/api/users/user/${targetUser.id}/status`, {
        method: "PATCH",
        body: JSON.stringify({ status: newStatus }),
      }, token);

      setSelectedItem((prev) => ({ ...prev, status: newStatus }));
      setAlertModal({
        open: true,
        title: "Status Updated",
        message: `User status changed to ${newStatus}.`,
        type: "success",
      });
      loadData();
    } catch (err) {
      setAlertModal({
        open: true,
        title: "Status Update Failed",
        message: err.message || "Failed to update user status.",
        type: "error",
      });
    } finally {
      setActionLoading(false);
    }
  };

  // User Password Reset on behalf of user
  const handleResetUserPassword = async (targetUser) => {
    if (!window.confirm(`Trigger secure password reset for ${targetUser.email}?`)) return;

    setActionLoading(true);
    try {
      const res = await apiRequest(`/api/users/user/${targetUser.id}/reset-password`, {
        method: "POST",
      }, token);

      setAlertModal({
        open: true,
        title: "Password Reset Generated",
        message: `Temporary credentials created for ${targetUser.email}:\nPassword: ${res.temporaryPassword}\nExpires: ${res.expiresAt ? new Date(res.expiresAt).toLocaleTimeString() : "in 1 hour"}.`,
        type: "success",
      });
      loadData();
    } catch (err) {
      setAlertModal({
        open: true,
        title: "Reset Failed",
        message: err.message || "Failed to trigger password reset.",
        type: "error",
      });
    } finally {
      setActionLoading(false);
    }
  };

  // Active form fields with dynamic options merged
  const activeFormFields = fields.map((f) => {
    if (f.dynamicOptions && dynamicOptions[f.name]) {
      return { ...f, options: dynamicOptions[f.name] };
    }
    return f;
  });

  const canGoPrev = page > 0;
  const canGoNext = page + 1 < pageInfo.totalPages;

  return (
    <div className="space-y-5 font-sans">
      {/* 1. DEDICATED FORM VIEW (NO POPUP) */}
      {viewMode === "form" && (
        <DedicatedFormView
          title={formConfig.title || (formConfig.mode === "create" ? `Add New ${singularName}` : `Edit ${singularName}`)}
          resourceTitle={title}
          singular={singularName}
          mode={formConfig.mode}
          fields={activeFormFields}
          initialValues={formConfig.initialValues}
          onSubmit={handleFormSubmit}
          onCancel={() => setViewMode(selectedItem ? "detail" : "list")}
          loading={actionLoading}
        />
      )}

      {/* 2. DEDICATED ACCOMMODATION REQUIREMENT FORM (NO POPUP) */}
      {viewMode === "accommodation-form" && (
        <AccommodationFormView
          mode={accommodationConfig.mode}
          itineraryDay={accommodationConfig.itineraryDay}
          safariTitle={selectedItem?.referenceNumber || "Safari"}
          dynamicOptions={dynamicOptions}
          reqForm={reqForm}
          setReqForm={setReqForm}
          onSubmit={handleAccommodationSubmit}
          onCancel={() => setViewMode("detail")}
          loading={actionLoading}
        />
      )}

      {/* 3. DEDICATED SUB-FORM VIEW (GUESTS, FLIGHTS, ITINERARY DAY) (NO POPUP) */}
      {viewMode === "sub-form" && (
        <SubFormView
          type={subFormConfig.type}
          title={subFormConfig.title}
          parentName={selectedItem?.firstName ? `${selectedItem.firstName} ${selectedItem.lastName || ""}` : (selectedItem?.referenceNumber || singularName)}
          initialValues={subFormConfig.initialValues}
          onSubmit={handleSubFormSubmit}
          onCancel={() => setViewMode("detail")}
          loading={actionLoading}
        />
      )}

      {/* 4. DEDICATED FULL-PAGE DETAIL VIEW (NO POPUP) */}
      {viewMode === "detail" && selectedItem && (
        <ResourceDetailView
          title={title}
          resource={resource}
          item={selectedItem}
          subData={subData}
          activeTab={activeTab}
          setActiveTab={setActiveTab}
          fields={fields}
          permissions={permissions}
          user={user}
          can={can}
          actionLoading={actionLoading}
          onBack={() => {
            setSelectedItem(null);
            setViewMode("list");
          }}
          onRefresh={() => loadSubDataForView(selectedItem)}
          onEdit={() => {
            let initialValues = { ...selectedItem };
            if (resource === "properties") {
              initialValues.amenityIds = Array.isArray(selectedItem.amenities)
                ? selectedItem.amenities.map((a) => a.id)
                : selectedItem.amenityIds || [];
              initialValues.tagIds = Array.isArray(selectedItem.tags)
                ? selectedItem.tags.map((t) => t.id)
                : selectedItem.tagIds || [];
            }
            setFormConfig({
              mode: "edit",
              initialValues: initialValues,
              title: `Edit ${singularName}`,
            });
            setViewMode("form");
          }}
          onEditDay={(day) => {
            setSubFormConfig({
              type: "itineraryDay",
              title: `Edit Day ${day.dayNumber} Destination`,
              initialValues: day,
            });
            setViewMode("sub-form");
          }}
          onAddRequirement={handleAddRequirement}
          onEditRequirement={handleEditRequirement}
          onDeleteRequirement={handleDeleteRequirement}
          onRegenerateDays={handleRegenerateDays}
          onAddGuest={() => {
            const targetClient = resource === "safaris" ? (selectedItem.client || { id: selectedItem.clientId }) : selectedItem;
            const targetClientId = targetClient?.id || selectedItem.clientId || selectedItem.id;
            const clientName = targetClient?.firstName ? `${targetClient.firstName} ${targetClient.lastName || ""}`.trim() : (selectedItem.firstName || "Client");
            setSubFormConfig({
              type: "guest",
              clientId: targetClientId,
              title: `Add Guest to ${clientName}`,
              initialValues: {},
            });
            setViewMode("sub-form");
          }}
          onAddFlight={() => {
            const targetClient = resource === "safaris" ? (selectedItem.client || { id: selectedItem.clientId }) : selectedItem;
            const targetClientId = targetClient?.id || selectedItem.clientId || selectedItem.id;
            const clientName = targetClient?.firstName ? `${targetClient.firstName} ${targetClient.lastName || ""}`.trim() : (selectedItem.firstName || "Client");
            setSubFormConfig({
              type: "flight",
              clientId: targetClientId,
              title: `Add Flight for ${clientName}`,
              initialValues: {},
            });
            setViewMode("sub-form");
          }}
          onVerifyProperty={handleVerifyProperty}
          allAmenities={dynamicOptions.amenityIds || []}
          allTags={dynamicOptions.tagIds || []}
          onAddPropertyAmenity={handleAddPropertyAmenity}
          onRemovePropertyAmenity={handleRemovePropertyAmenity}
          onAddPropertyTag={handleAddPropertyTag}
          onRemovePropertyTag={handleRemovePropertyTag}
          onBookLodge={handleBookLodge}
          onSendBooking={handleSendBooking}
          onConfirmBookingSuccess={handleConfirmBookingSuccess}
          onDeclineBooking={handleDeclineBooking}
          onToggleUserStatus={handleToggleUserStatus}
          onResetUserPassword={handleResetUserPassword}
        />
      )}

      {/* 5. MAIN TABLE LIST VIEW */}
      {viewMode === "list" && (
        <div className="space-y-4">
          {/* Top Header & Search Bar */}
          <div className="flex flex-col gap-3 rounded-2xl border border-slate-200 bg-white p-4 shadow-sm sm:flex-row sm:items-center sm:justify-between">
            <div>
              <h1 className="text-xl font-bold text-[#264624] sm:text-2xl tracking-tight font-title">
                {title}
              </h1>
              <p className="text-xs text-slate-500 font-sans mt-0.5">
                Total {pageInfo.totalElements} records found
              </p>
            </div>

            <div className="flex flex-wrap items-center gap-3">
              {table?.searchable && (
                <input
                  type="text"
                  value={searchTerm}
                  onChange={(e) => {
                    setSearchTerm(e.target.value);
                    setPage(0);
                  }}
                  placeholder="Search records..."
                  className="rounded-xl border border-slate-200 bg-slate-50/70 px-3.5 py-2 text-sm text-slate-800 outline-none transition focus:border-[#264624] focus:bg-white sm:w-56"
                />
              )}

              {(table?.filters || []).map((filter) => (
                <select
                  key={filter.key}
                  value={statusFilter}
                  onChange={(e) => {
                    setStatusFilter(e.target.value);
                    setPage(0);
                  }}
                  className="rounded-xl border border-slate-200 bg-slate-50/70 px-3.5 py-2 text-sm text-slate-800 outline-none transition focus:border-[#264624] focus:bg-white"
                >
                  {(filter.options || []).map((opt) => (
                    <option key={opt.value} value={opt.value}>
                      {opt.label}
                    </option>
                  ))}
                </select>
              ))}

              {can("create", permissions) && (
                <button
                  type="button"
                  onClick={() => {
                    setFormConfig({
                      mode: "create",
                      initialValues: {},
                      title: `Add New ${singularName}`,
                    });
                    setViewMode("form");
                  }}
                  className="inline-flex items-center gap-1.5 rounded-xl bg-[#264624] px-4 py-2 text-sm font-semibold text-white shadow-sm transition hover:bg-[#1b331a]"
                >
                  <Plus size={16} weight="bold" />
                  <span>Create {singularName}</span>
                </button>
              )}
            </div>
          </div>

          {/* Main Table */}
          <DynamicTable
            title=""
            fields={fields}
            items={items}
            actions={table?.actions || []}
            permissions={permissions}
            onAction={handleAction}
          />

          {/* Pagination Footer */}
          <div className="flex flex-col gap-3 rounded-2xl border border-slate-200 bg-white p-3.5 shadow-sm sm:flex-row sm:items-center sm:justify-between">
            <p className="text-sm text-slate-600 font-sans">
              Showing {items.length} of {pageInfo.totalElements || 0} records
            </p>

            <div className="flex items-center gap-2">
              <button
                type="button"
                onClick={() => setPage((prev) => Math.max(prev - 1, 0))}
                disabled={!canGoPrev || loading}
                className="rounded-xl border border-slate-200 px-3 py-1.5 text-sm font-medium text-slate-700 hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-40 transition"
              >
                Previous
              </button>

              <span className="rounded-xl bg-[#264624]/10 px-3 py-1.5 text-sm font-semibold text-[#264624]">
                Page {pageInfo.number + 1} / {pageInfo.totalPages || 1}
              </span>

              <button
                type="button"
                onClick={() => setPage((prev) => prev + 1)}
                disabled={!canGoNext || loading}
                className="rounded-xl border border-slate-200 px-3 py-1.5 text-sm font-medium text-slate-700 hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-40 transition"
              >
                Next
              </button>
            </div>
          </div>
        </div>
      )}

      {/* POPUP 1: Feedback Modal Strictly for Error & Success Messages */}
      <AlertModal
        open={alertModal.open}
        title={alertModal.title}
        message={alertModal.message}
        type={alertModal.type}
        onClose={() => setAlertModal({ open: false, title: "", message: "", type: "info" })}
      />

      {/* POPUP 2: Confirmation Dialog Strictly for Delete Actions */}
      <ConfirmModal
        open={deleteModal.open}
        title="Confirm Deletion"
        message={`Are you sure you want to delete this ${singularName.toLowerCase()}? This action cannot be reversed.`}
        loading={actionLoading}
        onConfirm={confirmDelete}
        onCancel={() => setDeleteModal({ open: false, item: null })}
      />

      {/* POPUP 3: Allocate & Book Lodge Modal */}
      <BookingModal
        open={bookingModal.open}
        requirement={bookingModal.requirement}
        day={bookingModal.day}
        days={subData?.days || []}
        safari={selectedItem}
        onClose={() => setBookingModal({ open: false, requirement: null, day: null })}
        onSuccess={handleBookingSuccess}
      />
    </div>
  );
};

export default ResourcePage;
