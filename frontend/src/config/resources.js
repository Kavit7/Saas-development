import { ROLES } from "./roles";
import { SquaresFour, UsersThree } from "@phosphor-icons/react";

const { ADMIN, RM, SALE, GUIDE } = ROLES;
export const resources = {
  dashboard: {
    title: "Dashboard",
    icon: SquaresFour,
    page: "DashboardPage",
    roles: [ADMIN, RM, SALE, GUIDE],
  },
  clients: {
    title: "Clients",
    icon: UsersThree,
    roles: [ADMIN, RM, SALE],
    permissions: {
      view: [ADMIN, RM, SALE],
      create: [SALE],
      update: [SALE],
      delete: [SALE],
    },
    table: {
      searchable: true,
      filters: [
        {
          key: "status",
          label: "Status",
          type: "select",
          defaultValue: "",
          options: [
            { label: "All status", value: "" },
            { label: "Active", value: "ACTIVE" },
            { label: "Inactive", value: "INACTIVE" },
          ],
        },
      ],
      actions: [
        { key: "view", label: "View" },
        { key: "update", label: "Edit" },
        { key: "delete", label: "Delete" },
      ],
    },
    fields: [
      {
        name: "firstName",
        label: "First Name",
        type: "text",
        showInTable: true,
      },
      {
        name: "lastName",
        label: "Last Name",
        type: "text",
        showInTable: true,
      },
      {
        name: "email",
        label: "Email",
        type: "email",
        showInTable: true,
      },
      {
        name: "phone",
        label: "Phone",
        type: "text",
        showInTable: true,
      },
      {
        name: "nationality",
        label: "Nationality",
        type: "text",
        showInTable: true,
      },
      {
        name: "preferredLanguage",
        label: "Preferred Language",
        type: "text",
        showInTable: true,
      },
      { name: "notes", label: "Notes", type: "text", showInTable: true },
      {
        name: "status",
        label: "Status",
        type: "text",
        showInTable: true,
        decorate: "status",
      },
    ],
  },
};
