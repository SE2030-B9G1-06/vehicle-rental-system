document.addEventListener("DOMContentLoaded", () =>
  CrudUI.start({
    title: "Vehicle categories",
    subtitle: "Organize the fleet into clear, searchable categories.",
    singular: "category",
    endpoint: "/api/categories",
    roles: ["ROLE_ADMIN", "ROLE_RENTAL_OFFICER"],
    background: "vehi_manage.jpg",
    columns: [
      {
        label: "ID",
        path: "id",
      },
      {
        label: "Category",
        path: "name",
      },
      {
        label: "Description",
        path: "description",
      },
    ],
    fields: [
      {
        key: "name",
        label: "Category name",
        required: true,
        maxlength: 50,
      },
      {
        key: "description",
        label: "Description",
        type: "textarea",
        wide: true,
        maxlength: 2000,
      },
    ],
    confirm:
      "Delete this category? Categories with linked vehicles cannot be deleted.",
    key: "categories",
  }),
);
