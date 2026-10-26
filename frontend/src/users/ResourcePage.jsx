import { useState, useEffect, useCallback } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {
  ArrowsDownUp,
  CaretDown,
  FunnelSimple,
  MagnifyingGlass,
  Plus,
  X,
} from "@phosphor-icons/react";

import DynamicTable from "../components/tables/DynamicTable";
import DedicatedFormView from "../components/forms/DedicatedFormView";
import AccommodationFormView from "../components/forms/AccommodationFormView";
import SubFormView from "../components/forms/SubFormView";
import ResourceDetailView from "../components/views/ResourceDetailView";
import BookingModal from "../components/bookings/BookingModal";
import AlertModal from "../components/feedback/AlertModal";
import ConfirmModal from "../components/feedback/ConfirmModal";
import PersonalizeUserModal from "../components/forms/PersonalizeUserModal";
import UserCreateModal from "../components/forms/UserCreateModal";

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
  const { token, user, can, impersonateUser } = useAuth();
  const navigate = useNavigate();
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
  const [filtersState, setFiltersState] = useState(() => {
    const init = {};
    (table?.filters || []).forEach((f) => {
      init[f.key] = f.defaultValue || "";
    });
    return init;
  });
  const [sortField, setSortField] = useState(table?.defaultSort || (resource === "properties" ? "name" : "createdAt"));
  const [sortDirection, setSortDirection] = useState(table?.defaultDirection || (resource === "properties" ? "asc" : "desc"));
  const [pageSize, setPageSize] = useState(20);
  const [page, setPage] = useState(0);
  const [pageInfo, setPageInfo] = useState({
    totalPages: 1,
    totalElements: 0,
    number: 0,
    size: 20,
  });

  useEffect(() => {
    const init = {};
    (table?.filters || []).forEach((f) => {
      init[f.key] = f.defaultValue || "";
    });
    setFiltersState(init);
    setSortField(table?.defaultSort || (resource === "properties" ? "name" : "createdAt"));
    setSortDirection(table?.defaultDirection || (resource === "properties" ? "asc" : "desc"));
    setSearchTerm("");
    setPage(0);
  }, [resource, table]);

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

  // Modal for Administrative User Personalization
  const [personalizeModal, setPersonalizeModal] = useState({
    open: false,
    user: null,
  });

  // Dedicated Modal for User Registration
  const [userCreateModalOpen, setUserCreateModalOpen] = useState(false);

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
          const tagList = tagRes.status === "fulfilled" ? tagRes.value?.data || tagRes.value || [] : [];
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
            tagIds: tagList.map((t) => ({
              label: `${t.name}${t.tagType ? ` (${t.tagType.replace("_", " ")})` : ""}`,
              description: t.description,
              tagType: t.tagType,
              value: t.id,
            })),
            tagFilterOptions: [
              { label: "All Tags", value: "" },
              ...tagList.map((t) => ({
                label: `${t.name}${t.tagType ? ` (${t.tagType.replace(/_/g, " ").toLowerCase()})` : ""}`,
                value: t.name,
              })),
            ],
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
        } else if (resource === "users") {
          const isSuperAdmin = String(user?.role_name || user?.role || "").toUpperCase().includes("SUPER_ADMIN");
          const roleOptions = isSuperAdmin
            ? [
                { label: "Platform Admin", value: "SUPER_ADMIN" },
                { label: "Company Admin", value: "ADMIN" },
                { label: "Sales Person", value: "SALES_PERSON" },
                { label: "Reservation Manager", value: "RESERVATION_MANAGER" },
                { label: "Guide", value: "GUIDE" },
              ]
            : [
                { label: "Sales Person", value: "SALES_PERSON" },
                { label: "Reservation Manager", value: "RESERVATION_MANAGER" },
                { label: "Guide", value: "GUIDE" },
              ];

          let companyOptions = [];
          if (isSuperAdmin) {
            try {
              const compRes = await getAllData("/api/company/companies?size=200", token);
              const compList = Array.isArray(compRes?.data?.content)
                ? compRes.data.content
                : Array.isArray(compRes?.content)
                ? compRes.content
                : Array.isArray(compRes?.data)
                ? compRes.data
                : Array.isArray(compRes)
                ? compRes
                : [];
              companyOptions = compList.map((c) => ({
                label: c.name,
                value: c.name,
                id: c.id,
              }));
            } catch (ce) {
              console.warn("Could not load companies for user assignment:", ce);
            }
          }

          let companyFilterList = [
            { label: "All Companies", value: "" },
            ...companyOptions.map((c) => ({ label: c.label, value: c.label })),
          ];
          setDynamicOptions((prev) => ({
            ...prev,
            roleName: roleOptions,
            ...(companyOptions.length > 0 ? { companyName: companyOptions } : {}),
            companyFilterOptions: companyFilterList,
          }));
        }
      } catch (err) {
        console.error("Failed to load select options:", err);
      }
    };

    if (token) {
      fetchOptions();
    }
  }, [resource, token, user]);

  // Load Main Table Data
  const loadData = useCallback(async () => {
    setLoading(true);
    try {
      const params = new URLSearchParams({
        page: String(page),
        size: String(pageSize),
        sortBy: sortField || (resource === "properties" ? "name" : "createdAt"),
        direction: sortDirection || (resource === "properties" ? "asc" : "desc"),
      });

      if (searchTerm.trim()) {
        params.set("search", searchTerm.trim());
      }

      Object.entries(filtersState).forEach(([key, val]) => {
        if (val !== undefined && val !== null && String(val).trim() !== "") {
          params.set(key, String(val).trim());
        }
      });

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
        size: pageSize,
      });
    } catch {
      setItems([]);
      setPageInfo({ totalPages: 1, totalElements: 0, number: 0, size: pageSize });
    } finally {
      setLoading(false);
    }
  }, [apiBasePath, page, pageSize, sortField, sortDirection, searchTerm, filtersState, token, resource]);

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
    } else if (actionKey === "resetPassword") {
      handleResetUserPassword(item);
    } else if (actionKey === "toggleStatus") {
      handleToggleUserStatus(item);
    } else if (actionKey === "personalize") {
      setPersonalizeModal({
        open: true,
        user: item,
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
          if (!payload.gender) {
            delete payload.gender;
          }
          if (!payload.password) {
            delete payload.password;
          }
        } else if (resource === "support-tickets") {
          if (!payload.priority) payload.priority = "MEDIUM";
          if (!payload.category) payload.category = "OTHER";
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
          if (!payload.gender) {
            delete payload.gender;
          }
          if (!payload.password) {
            delete payload.password;
          }
          if (!payload.companyName) {
            delete payload.companyName;
          }
        } else if (resource === "support-tickets") {
          putUrl = `/api/v1/support-tickets/${formConfig.initialValues.id}`;
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
      numberOfNights: 1,
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
      numberOfNights: req.numberOfNights || 1,
      roomPreferences: req.roomPreferences || "",
      specialRequests: req.specialRequests || "",
      roomRequirements: req.roomRequirements?.length
        ? req.roomRequirements.map((r) => ({ roomType: r.roomType, quantity: r.quantity }))
        : [{ roomType: dynamicOptions.roomTypes?.[0]?.value || "Standard", quantity: 1 }],
    });
    setViewMode("accommodation-form");
  };

  const handleDeleteRequirement = async (reqId) => {
    // Booking Protection Policy: Do not allow deleting requirement with active booking
    const activeBooking = (subData?.bookings || []).find((b) =>
      String(b.requirementId) === String(reqId) &&
      String(b.status || "").toUpperCase() !== "CANCELLED" &&
      String(b.status || "").toUpperCase() !== "DECLINED"
    );
    if (activeBooking) {
      setAlertModal({
        open: true,
        title: "Action Restricted",
        message: `Cannot delete requirement: Active lodge booking '${activeBooking.referenceNumber || activeBooking.propertyName}' exists. Please cancel the booking first.`,
        type: "error",
      });
      return;
    }

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
        numberOfNights: Math.max(1, parseInt(reqForm.numberOfNights, 10) || 1),
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

  const handleAddDay = async () => {
    if (!selectedItem || !selectedItem.id) return;
    setActionLoading(true);
    try {
      await createData(`/safari/${selectedItem.id}/add-day`, {}, token);
      setAlertModal({
        open: true,
        title: "Day Added Successfully",
        message: "New itinerary day has been appended to the safari and the safari schedule was extended safely.",
        type: "success",
      });
      loadSubDataForView(selectedItem);
      loadData();
    } catch (err) {
      setAlertModal({
        open: true,
        title: "Failed to Add Day",
        message: err.message || "Failed to append itinerary day",
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
        message: `Temporary credentials created for ${targetUser.email}:\nPassword: ${res.temporaryPassword}\nExpires: ${res.expiresAt ? new Date(res.expiresAt).toLocaleString() : "in 3 days"}\n(User must change password upon logging in).`,
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

  // User Personalization Intervention Success Handler
  const handlePersonalizeSuccess = (res) => {
    setAlertModal({
      open: true,
      title: "Personalization Completed",
      message: res?.message || "Account personalization executed successfully with an audit support ticket.",
      type: "success",
    });
    if (selectedItem && personalizeModal?.user && selectedItem.id === personalizeModal.user.id) {
      if (res?.targetUser) {
        setSelectedItem((prev) => ({ ...prev, ...res.targetUser }));
      }
    }
    loadData();
  };

  // User Impersonation / Personalization Success Handler
  const handleImpersonateSuccess = (res) => {
    if (res?.token) {
      impersonateUser(res.token);
      navigate("/dashboard");
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
            const allDays = (subData?.days || []).slice().sort((a, b) => (a.dayNumber || 0) - (b.dayNumber || 0));
            const prevDay = allDays.find((d) => d.dayNumber === day.dayNumber - 1);
            const isDestUnset = !day.destination || day.destination.trim().toLowerCase() === "destination not set" || day.destination.trim().toLowerCase().includes("pending");
            const autoSuggestedDestination = isDestUnset && prevDay?.destination && !prevDay.destination.toLowerCase().includes("pending")
              ? prevDay.destination
              : "";

            setSubFormConfig({
              type: "itineraryDay",
              title: `Edit Day ${day.dayNumber} Destination`,
              initialValues: {
                ...day,
                destination: day.destination && !isDestUnset ? day.destination : autoSuggestedDestination,
                _autoSuggested: !!autoSuggestedDestination,
                _prevDayNumber: prevDay?.dayNumber,
                _prevDestination: prevDay?.destination,
              },
            });
            setViewMode("sub-form");
          }}
          onAddRequirement={handleAddRequirement}
          onEditRequirement={handleEditRequirement}
          onDeleteRequirement={handleDeleteRequirement}
          onRegenerateDays={handleRegenerateDays}
          onAddDay={handleAddDay}
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
          onPersonalizeUser={(target) => setPersonalizeModal({ open: true, user: target || selectedItem })}
        />
      )}

      {/* 5. MAIN TABLE LIST VIEW */}
      {viewMode === "list" && (
        <div className="space-y-4">
          <div className="overflow-hidden rounded-xl border border-[#28332C]/10 bg-white">
            <div className="flex flex-col gap-3 px-5 py-4 sm:flex-row sm:items-center sm:justify-between">
              <div>
                <h1 className="text-xl font-semibold tracking-tight text-[#28332C] sm:text-2xl">
                  {title}
                </h1>
                <p className="mt-0.5 text-sm text-slate-500">
                  {Number(pageInfo.totalElements).toLocaleString()} records
                </p>
              </div>

              {can("create", permissions) && (
                <button
                  type="button"
                  onClick={() => {
                    if (resource === "users") {
                      setUserCreateModalOpen(true);
                      return;
                    }
                    setFormConfig({
                      mode: "create",
                      initialValues: {},
                      title: `Add New ${singularName}`,
                    });
                    setViewMode("form");
                  }}
                  className="inline-flex min-h-10 items-center justify-center gap-2 self-start rounded-lg bg-[#35634B] px-4 py-2 text-sm font-medium text-white transition hover:bg-[#284C39] focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#35634B] sm:self-auto"
                >
                  <Plus size={17} />
                  <span>Create {singularName}</span>
                </button>
              )}
            </div>

            <div className="border-t border-[#28332C]/[0.08] bg-[#F8FAF8] px-5 py-4">
              <div className="flex items-center gap-2 text-xs font-medium text-slate-500">
                <FunnelSimple size={15} />
                <span>Find and filter</span>
              </div>

              <div className="mt-3 flex flex-wrap items-end gap-3">
                {table?.searchable && (
                  <label className="min-w-[220px] flex-1 sm:max-w-xs">
                    <span className="mb-1.5 block text-xs font-medium text-slate-600">
                      Search
                    </span>
                    <span className="flex h-10 items-center gap-2 rounded-lg border border-slate-200 bg-white px-3 text-slate-400 transition focus-within:border-[#6F927D] focus-within:ring-2 focus-within:ring-[#35634B]/10">
                      <MagnifyingGlass size={16} />
                      <input
                        type="text"
                        value={searchTerm}
                        onChange={(e) => {
                          setSearchTerm(e.target.value);
                          setPage(0);
                        }}
                        placeholder="Search records..."
                        aria-label="Search records"
                        className="min-w-0 flex-1 bg-transparent text-sm text-slate-800 outline-none placeholder:text-slate-400"
                      />
                      {searchTerm && (
                        <button
                          type="button"
                          onClick={() => {
                            setSearchTerm("");
                            setPage(0);
                          }}
                          className="rounded p-0.5 text-slate-400 transition hover:bg-slate-100 hover:text-slate-700"
                          aria-label="Clear search"
                        >
                          <X size={14} />
                        </button>
                      )}
                    </span>
                  </label>
                )}

                {(table?.filters || []).map((filter) => {
                  const options = (filter.dynamicKey && dynamicOptions[filter.dynamicKey])
                    ? dynamicOptions[filter.dynamicKey]
                    : filter.options || [];

                  return (
                    <label key={filter.key} className="min-w-[150px]">
                      <span className="mb-1.5 block text-xs font-medium text-slate-600">
                        {filter.label}
                      </span>
                      <span className="relative flex h-10 items-center rounded-lg border border-slate-200 bg-white transition focus-within:border-[#6F927D] focus-within:ring-2 focus-within:ring-[#35634B]/10">
                        <select
                          value={filtersState[filter.key] ?? ""}
                          onChange={(e) => {
                            const val = e.target.value;
                            setFiltersState((prev) => ({ ...prev, [filter.key]: val }));
                            setPage(0);
                          }}
                          className="h-full w-full cursor-pointer appearance-none rounded-lg bg-transparent py-2 pl-3 pr-9 text-sm text-slate-700 outline-none"
                          title={`Filter by ${filter.label}`}
                        >
                          {options.map((opt) => (
                            <option key={opt.value} value={opt.value}>
                              {opt.label}
                            </option>
                          ))}
                        </select>
                        <CaretDown
                          size={15}
                          className="pointer-events-none absolute right-3 text-slate-400"
                        />
                      </span>
                    </label>
                  );
                })}

                {(table?.sortOptions || []).length > 0 && (
                  <div className="min-w-[190px]">
                    <span className="mb-1.5 block text-xs font-medium text-slate-600">
                      Sort by
                    </span>
                    <div className="flex h-10 items-center rounded-lg border border-slate-200 bg-white transition focus-within:border-[#6F927D] focus-within:ring-2 focus-within:ring-[#35634B]/10">
                      <ArrowsDownUp size={15} className="ml-3 shrink-0 text-slate-400" />
                      <select
                        value={sortField}
                        onChange={(e) => {
                          setSortField(e.target.value);
                          setPage(0);
                        }}
                        className="h-full min-w-0 flex-1 cursor-pointer appearance-none bg-transparent px-2 text-sm text-slate-700 outline-none"
                        title="Sort field"
                      >
                        {table.sortOptions.map((opt) => (
                          <option key={opt.value} value={opt.value}>
                            {opt.label}
                          </option>
                        ))}
                      </select>
                      <button
                        type="button"
                        onClick={() => {
                          setSortDirection((prev) => (prev.toLowerCase() === "asc" ? "desc" : "asc"));
                          setPage(0);
                        }}
                        title={`Sort ${sortDirection === "asc" ? "ascending" : "descending"}. Click to change direction.`}
                        aria-label={`Sort ${sortDirection === "asc" ? "ascending" : "descending"}`}
                        className="mr-1.5 inline-flex h-7 items-center gap-1 rounded-md border border-slate-200 px-2 text-[11px] font-medium text-slate-600 transition hover:bg-[#F2F4F0] hover:text-[#35634B] focus-visible:outline-2 focus-visible:outline-offset-1 focus-visible:outline-[#35634B]"
                      >
                        <span>{sortDirection.toUpperCase()}</span>
                        <CaretDown
                          size={12}
                          className={sortDirection.toLowerCase() === "asc" ? "rotate-180" : ""}
                        />
                      </button>
                    </div>
                  </div>
                )}

                {(searchTerm || Object.values(filtersState).some((v) => v !== "") || (table?.defaultDirection && sortDirection !== table.defaultDirection)) && (
                  <button
                    type="button"
                    onClick={() => {
                      const init = {};
                      (table?.filters || []).forEach((f) => {
                        init[f.key] = f.defaultValue || "";
                      });
                      setFiltersState(init);
                      setSearchTerm("");
                      setSortField(table?.defaultSort || (resource === "properties" ? "name" : "createdAt"));
                      setSortDirection(table?.defaultDirection || (resource === "properties" ? "asc" : "desc"));
                      setPage(0);
                    }}
                    className="inline-flex h-10 items-center gap-1.5 rounded-lg px-2.5 text-sm font-medium text-slate-500 transition hover:bg-white hover:text-[#35634B] focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#35634B]"
                    title="Reset all filters and search"
                  >
                    <X size={15} />
                    <span>Clear all</span>
                  </button>
                )}
              </div>
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
            sortField={sortField}
            sortDirection={sortDirection}
            onSort={(fieldName) => {
              if (sortField === fieldName) {
                setSortDirection((prev) => (prev.toLowerCase() === "asc" ? "desc" : "asc"));
              } else {
                setSortField(fieldName);
                setSortDirection("asc");
              }
              setPage(0);
            }}
          />

          {/* Pagination Footer */}
          <div className="flex flex-col gap-3 rounded-2xl border border-slate-200 bg-white p-3.5 shadow-sm sm:flex-row sm:items-center sm:justify-between">
            <div className="flex flex-wrap items-center gap-3">
              <p className="text-sm text-slate-600 font-sans">
                Showing {items.length} of {pageInfo.totalElements || 0} records
              </p>

              <div className="flex items-center gap-1.5 text-xs text-slate-500">
                <span>Rows:</span>
                <select
                  value={pageSize}
                  onChange={(e) => {
                    setPageSize(Number(e.target.value));
                    setPage(0);
                  }}
                  className="rounded-lg border border-slate-200 bg-slate-50 px-2 py-1 text-xs font-semibold text-slate-700 outline-none cursor-pointer"
                >
                  <option value={10}>10</option>
                  <option value={20}>20</option>
                  <option value={50}>50</option>
                  <option value={100}>100</option>
                </select>
              </div>
            </div>

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
                Page {pageInfo.number + 1} / {Math.max(pageInfo.totalPages || 1, 1)}
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

      {/* POPUP 4: User Personalization / Support Modal */}
      <PersonalizeUserModal
        isOpen={personalizeModal.open}
        user={personalizeModal.user}
        token={token}
        onClose={() => setPersonalizeModal({ open: false, user: null })}
        onSuccess={handlePersonalizeSuccess}
        onImpersonate={handleImpersonateSuccess}
      />

      {/* POPUP 5: Dedicated User Creation Modal */}
      <UserCreateModal
        isOpen={userCreateModalOpen}
        onClose={() => setUserCreateModalOpen(false)}
        onSuccess={(newUser) => {
          setUserCreateModalOpen(false);
          setAlertModal({
            open: true,
            title: "User Created Successfully",
            message: `User account for ${newUser?.data?.firstName || newUser?.firstName || "new user"} ${newUser?.data?.lastName || newUser?.lastName || ""} has been created. An automated secure temporary password was assigned.`,
            type: "success",
          });
          loadData();
        }}
        currentUser={user}
        token={token}
      />
    </div>
  );
};

export default ResourcePage;
