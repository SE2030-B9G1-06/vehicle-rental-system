document.addEventListener("DOMContentLoaded", () =>
  CrudUI.start({
    title: "Promotions & discounts",
    subtitle:
      "Create offers, adjust validity dates and control campaign usage.",
    singular: "promotion",
    endpoint: "/api/promotions",
    roles: ["ROLE_ADMIN", "ROLE_FINANCE_OFFICER"],
    background: "promo.jpg",
    columns: [
      {
        label: "Code",
        path: "code",
      },
      {
        label: "Discount %",
        path: "discountPercentage",
      },
      {
        label: "Fixed discount",
        path: "fixedAmount",
        type: "money",
      },
      {
        label: "Start",
        path: "startDate",
        type: "date",
      },
      {
        label: "End",
        path: "endDate",
        type: "date",
      },
      {
        label: "Max uses",
        path: "maxUses",
      },
      {
        label: "Status",
        path: "isActive",
        type: "active",
      },
    ],
    fields: [
      {
        key: "code",
        label: "Promo code",
        required: true,
        maxlength: 20,
      },
      {
        key: "discountPercentage",
        label: "Percentage discount",
        type: "number",
        min: 0,
        max: 100,
        step: ".01",
        default: 0,
        required: true,
        hint: "Use 0 when applying a fixed amount.",
      },
      {
        key: "fixedAmount",
        label: "Fixed discount (LKR)",
        type: "number",
        min: 0,
        step: ".01",
        nullable: true,
        hint: "Leave blank for a percentage discount.",
      },
      {
        key: "maxUses",
        label: "Maximum uses (optional)",
        type: "number",
        min: 1,
        nullable: true,
      },
      {
        key: "startDate",
        label: "Start date",
        type: "date",
        required: true,
      },
      {
        key: "endDate",
        label: "End date",
        type: "date",
        required: true,
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
    confirm:
      "Delete this promotion? If any booking uses it, it will be deactivated to retain pricing history.",
    key: "promotions",
  }),
);
