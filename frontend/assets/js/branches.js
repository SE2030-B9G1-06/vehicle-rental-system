document.addEventListener("DOMContentLoaded", () =>
  CrudUI.start({
    title: "Location & branch management",
    subtitle:
      "Keep rental locations, operating hours and vehicle capacity up to date.",
    singular: "branch",
    endpoint: "/api/branches",
    roles: ["ROLE_ADMIN", "ROLE_RENTAL_OFFICER", "ROLE_BRANCH_MANAGER"],
    background: "branches.jpg",
    columns: [
      {
        label: "Branch",
        path: "name",
      },
      {
        label: "Address",
        path: "address",
      },
      {
        label: "Contact",
        path: "contactNumber",
      },
      {
        label: "Hours",
        path: "operatingHours",
      },
      {
        label: "Capacity",
        path: "vehicleCapacity",
      },
      {
        label: "Status",
        path: "isActive",
        type: "active",
      },
    ],
    fields: [
      {
        key: "name",
        label: "Branch name",
        required: true,
        maxlength: 100,
      },
      {
        key: "contactNumber",
        label: "Contact number",
        required: true,
        maxlength: 20,
      },
      {
        key: "address",
        label: "Address",
        type: "textarea",
        required: true,
        wide: true,
        maxlength: 2000,
      },
      {
        key: "operatingHours",
        label: "Operating hours",
        required: true,
        maxlength: 100,
      },
      {
        key: "vehicleCapacity",
        label: "Vehicle capacity",
        type: "number",
        required: true,
        min: 1,
      },
      {
        key: "isActive",
        label: "Status",
        type: "select",
        options: [
          ["true", "Active"],
          ["false", "Inactive"],
        ],
        boolean: true,
        default: "true",
        required: true,
      },
    ],
    deleteLabel: "Close branch",
    confirm:
      "Close this branch? Its history is kept. Move its active vehicles and finish or cancel open bookings first.",
    canDelete: (r) => r.isActive !== false,
    key: "branches",
  }),
);
