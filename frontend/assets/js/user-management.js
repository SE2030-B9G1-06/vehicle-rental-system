document.addEventListener("DOMContentLoaded", () =>
  CrudUI.start({
    title: "User account management",
    subtitle:
      "Manage staff access and customer accounts, with history retained when accounts are deactivated.",
    singular: "staff account",
    editSingular: "user account",
    endpoint: "/api/users",
    createEndpoint: "/api/users/staff",
    roles: ["ROLE_ADMIN"],
    users: true,
    background: "user_manage.jpg",
    columns: [
      {
        label: "Name",
        path: "firstName",
        join: ["firstName", "lastName"],
      },
      {
        label: "Email",
        path: "email",
      },
      {
        label: "Contact",
        path: "phone",
      },
      {
        label: "Role",
        path: "role",
        type: "role",
      },
      {
        label: "Status",
        path: "active",
        type: "active",
      },
    ],
    fields: [
      {
        key: "firstName",
        label: "First name",
        required: true,
        maxlength: 50,
      },
      {
        key: "lastName",
        label: "Last name",
        required: true,
        maxlength: 50,
      },
      {
        key: "email",
        label: "Email",
        type: "email",
        required: true,
        maxlength: 100,
      },
      {
        key: "phone",
        label: "Contact number",
        required: true,
        maxlength: 20,
      },
      {
        key: "drivingLicenceNumber",
        label: "Driving licence (optional)",
        maxlength: 50,
      },
      {
        key: "role",
        label: "Role",
        type: "select",
        required: true,
        options: (r) =>
          [
            ["ROLE_CUSTOMER", "Customer"],
            ["ROLE_ADMIN", "Admin"],
            ["ROLE_BOOKING_MANAGER", "Booking Manager"],
            ["ROLE_RENTAL_OFFICER", "Rental Officer"],
            ["ROLE_MAINTENANCE_SUPERVISOR", "Maintenance Supervisor"],
            ["ROLE_FINANCE_OFFICER", "Finance Officer"],
            ["ROLE_BRANCH_MANAGER", "Branch Manager"],
          ].filter((x) => r || x[0] !== "ROLE_CUSTOMER"),
      },
      {
        key: "active",
        label: "Account status",
        type: "select",
        options: [
          ["true", "Active"],
          ["false", "Inactive"],
        ],
        boolean: true,
        default: "true",
        required: true,
      },
      {
        key: "password",
        label: "Password",
        type: "password",
        required: true,
        hint: "At least 8 characters. Leave blank when editing to keep the existing password.",
      },
    ],
    deleteLabel: "Deactivate",
    canDelete: (r) => r.active !== false,
    confirm:
      "Deactivate this account? Sign-in and existing sessions will be blocked; bookings and history are retained.",
    key: "user-management",
  }),
);
