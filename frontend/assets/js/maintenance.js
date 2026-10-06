document.addEventListener("DOMContentLoaded", () =>
  CrudUI.start({
    title: "Maintenance & inspections",
    subtitle:
      "Schedule service, update repair costs and keep a complete maintenance history.",
    singular: "maintenance record",
    endpoint: "/api/maintenance",
    roles: [
      "ROLE_ADMIN",
      "ROLE_RENTAL_OFFICER",
      "ROLE_MAINTENANCE_SUPERVISOR",
      "ROLE_BRANCH_MANAGER",
    ],
    background: "vehi-maintan.jpg",
    columns: [
      {
        label: "ID",
        path: "id",
      },
      {
        label: "Vehicle",
        path: "vehicle.registrationNumber",
      },
      {
        label: "Service date",
        path: "serviceDate",
        type: "date",
      },
      {
        label: "Odometer",
        path: "odometerReading",
      },
      {
        label: "Cost",
        path: "cost",
        type: "money",
      },
      {
        label: "Status",
        path: "status",
        type: "status",
      },
    ],
    fields: [
      {
        key: "vehicle.id",
        label: "Vehicle",
        type: "select",
        source: "/api/vehicles",
        optionLabel: (x) => `${x.registrationNumber} · ${x.make} ${x.model}`,
        numeric: true,
        required: true,
        lockOnEdit: true,
      },
      {
        key: "serviceDate",
        label: "Service date",
        type: "date",
        required: true,
      },
      {
        key: "odometerReading",
        label: "Odometer (km)",
        type: "number",
        min: 0,
        required: true,
      },
      {
        key: "cost",
        label: "Cost (LKR)",
        type: "number",
        min: 0,
        step: ".01",
        required: true,
        default: 0,
      },
      {
        key: "status",
        label: "Service status",
        type: "select",
        options: [
          ["SCHEDULED", "Scheduled"],
          ["IN_PROGRESS", "In Progress"],
          ["COMPLETED", "Completed"],
          ["CANCELLED", "Cancelled"],
        ],
        required: true,
        default: "SCHEDULED",
      },
    ],
    deleteLabel: (r) =>
      ["SCHEDULED", "IN_PROGRESS"].includes(r.status)
        ? "Cancel service"
        : "Delete",
    confirm: (r) =>
      ["SCHEDULED", "IN_PROGRESS"].includes(r.status)
        ? "Cancel this service? Its record is kept and vehicle availability is recalculated."
        : "Permanently remove this erroneous or duplicate completed/cancelled maintenance record?",
    key: "maintenance",
  }),
);
