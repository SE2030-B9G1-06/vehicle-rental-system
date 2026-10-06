document.addEventListener("DOMContentLoaded", () =>
  CrudUI.start({
    title: "Rental agreements & contracts",
    subtitle: "Record driver details and manage vehicle handover and return.",
    singular: "contract",
    endpoint: "/api/contracts",
    roles: ["ROLE_ADMIN", "ROLE_RENTAL_OFFICER", "ROLE_FINANCE_OFFICER"],
    background: "booking_st.jpg",
    columns: [
      {
        label: "Contract",
        path: "id",
      },
      {
        label: "Booking",
        path: "booking.id",
      },
      {
        label: "Driver",
        path: "driverName",
      },
      {
        label: "Licence",
        path: "licenseNumber",
      },
      {
        label: "Pickup",
        path: "pickupTimestamp",
        type: "date",
      },
      {
        label: "Return",
        path: "returnTimestamp",
        type: "date",
      },
      {
        label: "Status",
        path: "status",
        type: "status",
      },
    ],
    fields: [
      {
        key: "booking.id",
        label: "Confirmed booking",
        type: "select",
        source: "/api/bookings",
        optionLabel: (x) =>
          `#${x.id} · ${x.vehicle.registrationNumber} · ${x.customer.firstName}`,
        filter: (x) => x.status === "CONFIRMED",
        numeric: true,
        required: true,
        lockOnEdit: true,
      },
      {
        key: "driverName",
        label: "Driver name",
        required: true,
        maxlength: 100,
      },
      {
        key: "licenseNumber",
        label: "Driving licence number",
        required: true,
        maxlength: 50,
      },
      {
        key: "identityDocument",
        label: "NIC / passport",
        required: true,
        maxlength: 50,
      },
      {
        key: "contactDetails",
        label: "Driver contact details",
        required: true,
        maxlength: 50,
      },
      {
        key: "signature",
        label: "Driver signature / acknowledgement",
        maxlength: 255,
        hint: "Enter the driver’s signed acknowledgement reference.",
      },
      {
        key: "status",
        label: "Contract status",
        type: "select",
        options: (r) =>
          !r
            ? [["DRAFT", "Draft"]]
            : r.status === "DRAFT"
              ? [
                  ["DRAFT", "Draft"],
                  ["ACTIVE", "Active — hand over vehicle"],
                  ["VOIDED", "Voided"],
                ]
              : [
                  ["ACTIVE", "Active"],
                  ["COMPLETED", "Completed — vehicle returned"],
                ],
        required: true,
        default: "DRAFT",
        hint: "New contracts start as drafts. Handover and return times are recorded automatically.",
      },
    ],
    deleteLabel: "Void",
    canDelete: (r) => r.status === "DRAFT",
    canEdit: (r) => ["DRAFT", "ACTIVE"].includes(r.status),
    confirm:
      "Void this unfulfilled draft contract? The contract record and booking remain in history.",
    key: "contracts",
  }),
);
