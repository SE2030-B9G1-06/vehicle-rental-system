document.addEventListener("DOMContentLoaded", () =>
  CrudUI.start({
    title: "Bookings & reservations",
    subtitle:
      "Create reservations, edit dates and follow each booking from confirmation to return.",
    singular: "booking",
    endpoint: "/api/bookings",
    roles: [
      "ROLE_ADMIN",
      "ROLE_RENTAL_OFFICER",
      "ROLE_BOOKING_MANAGER",
      "ROLE_BRANCH_MANAGER",
    ],
    background: "booking_st.jpg",
    columns: [
      {
        label: "Reference",
        path: "id",
      },
      {
        label: "Customer",
        path: "customer.firstName",
        join: ["customer.firstName", "customer.lastName"],
      },
      {
        label: "Vehicle",
        path: "vehicle.registrationNumber",
      },
      {
        label: "Pickup",
        path: "pickupDatetime",
        type: "date",
      },
      {
        label: "Return",
        path: "returnDatetime",
        type: "date",
      },
      {
        label: "Total",
        path: "totalAmount",
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
        key: "customer.id",
        label: "Customer",
        type: "select",
        source: "/api/users/customers",
        optionLabel: (x) => `${x.firstName} ${x.lastName} · ${x.email}`,
        numeric: true,
        required: true,
        onlyCreate: true,
      },
      {
        key: "vehicle.id",
        label: "Vehicle",
        type: "select",
        source: "/api/vehicles",
        filter: (x) => x.isActive !== false && x.status === "AVAILABLE",
        optionLabel: (x) => `${x.registrationNumber} · ${x.make} ${x.model}`,
        numeric: true,
        required: true,
        lockOnEdit: true,
      },
      {
        key: "pickupBranch.id",
        label: "Pickup branch",
        type: "select",
        source: "/api/branches",
        optionLabel: (x) => x.name,
        filter: (x) => x.isActive !== false,
        numeric: true,
        required: true,
      },
      {
        key: "returnBranch.id",
        label: "Return branch",
        type: "select",
        source: "/api/branches",
        optionLabel: (x) => x.name,
        filter: (x) => x.isActive !== false,
        numeric: true,
        required: true,
      },
      {
        key: "pickupDatetime",
        label: "Pickup date & time",
        type: "datetime-local",
        required: true,
      },
      {
        key: "returnDatetime",
        label: "Return date & time",
        type: "datetime-local",
        required: true,
      },
      {
        key: "promotion.id",
        label: "Promotion (optional)",
        type: "select",
        source: "/api/promotions",
        filter: (x) => x.isActive !== false,
        optionLabel: (x) => x.code,
        numeric: true,
        nullable: true,
      },
    ],
    canEdit: (r) => ["PENDING", "CONFIRMED"].includes(r.status),
    canDelete: (r) => ["PENDING", "CONFIRMED"].includes(r.status),
    deleteLabel: "Cancel",
    confirm: "Cancel this booking? The reservation history will be retained.",
    actions: (r) =>
      r.status === "PENDING"
        ? [{ label: "Confirm", status: "CONFIRMED" }]
        : r.status === "CONFIRMED"
          ? [{ label: "Pick up", status: "IN_PROGRESS" }]
          : r.status === "IN_PROGRESS"
            ? [{ label: "Complete", status: "COMPLETED" }]
            : [],
    key: "booking-manager",
  }),
);
