document.addEventListener("DOMContentLoaded", () =>
  CrudUI.start({
    title: "Vehicle & fleet management",

    subtitle:
      "Manage your fleet, update rental rates and " +
      "reallocate vehicles between branches.",

    singular: "vehicle",

    endpoint: "/api/vehicles",

    roles: [
      "ROLE_ADMIN",
      "ROLE_RENTAL_OFFICER",
    ],

    background: "vehi_manage.jpg",

    columns: [
      {
        label: "Registration",
        path: "registrationNumber",
      },
      {
        label: "Vehicle",
        path: "make",
        join: [
          "make",
          "model",
        ],
      },
      {
        label: "Year",
        path: "year",
      },
      {
        label: "Daily rate",
        path: "dailyRate",
        type: "money",
      },
      {
        label: "Branch",
        path: "branch.name",
      },
      {
        label: "Mileage",
        path: "mileage",
      },
      {
        label: "Status",
        path: "status",
        type: "status",
      },
    ],

    fields: [
      {
        key: "registrationNumber",
        label: "Registration number",
        required: true,
        maxlength: 20,
      },
      {
        key: "vin",
        label: "VIN (optional)",
        maxlength: 50,
      },
      {
        key: "make",
        label: "Make",
        required: true,
        maxlength: 50,
      },
      {
        key: "model",
        label: "Model",
        required: true,
        maxlength: 50,
      },
      {
        key: "year",
        label: "Model year",
        type: "number",
        min: 1900,
        max: 2027,
        required: true,
      },
      {
        key: "dailyRate",
        label: "Daily rate (LKR)",
        type: "number",
        min: 0.01,
        step: ".01",
        required: true,
      },
      {
        key: "mileage",
        label: "Mileage (km)",
        type: "number",
        min: 0,
        default: 0,
        required: true,
      },
      {
        key: "branch.id",
        label: "Branch",
        type: "select",
        source: "/api/branches",

        optionLabel: (branch) =>
          branch.name,

        filter: (branch, vehicle) =>
          branch.isActive !== false
            || branch.id === vehicle?.branch?.id,

        numeric: true,
        required: true,
      },
      {
        key: "category.id",
        label: "Category",
        type: "select",
        source: "/api/categories",

        optionLabel: (category) =>
          category.name,

        numeric: true,
        required: true,
      },
      {
        key: "status",
        label: "Vehicle status",
        type: "select",

        options: [
          [
            "AVAILABLE",
            "Available",
          ],
          [
            "MAINTENANCE",
            "Maintenance",
          ],
          [
            "INACTIVE",
            "Inactive",
          ],
          [
            "RENTED",
            "Rented",
          ],
        ],

        default: "AVAILABLE",
        required: true,

        hint:
          "Rented is controlled by the booking handover.",
      },
      {
        key: "imageUrl",

        label:
          "Vehicle asset image path (optional)",

        wide: true,

        maxlength: 500,

        hint:
          "Copy the image into frontend/assets/images, " +
          "then enter a path such as " +
          "/assets/images/benz_50.jpg. " +
          "Online image URLs are not accepted.",
      },
    ],

    deleteLabel: "Deactivate",

    canDelete: (vehicle) =>
      vehicle.status !== "INACTIVE",

    confirm:
      "Deactivate this vehicle? It will leave the " +
      "available fleet, while its rental history " +
      "is retained.",

    key: "vehicle-management",
  }),
);