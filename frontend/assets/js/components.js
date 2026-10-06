async function loadComponent(elementId, filePath) {
  const target = document.getElementById(elementId);
  if (!target) return;
  try {
    const response = await fetch(filePath);
    if (!response.ok) throw new Error("Navigation unavailable.");
    target.innerHTML = await response.text();
    if (filePath.includes("navbar.html")) await updateNavbarAuth();
  } catch (e) {
    target.textContent = e.message;
  }
}
async function updateNavbarAuth() {
  const container = document.getElementById("nav-auth-container");
  if (!container) return;
  const user = await AppAuth.refreshUser();
  const links = {
    ROLE_ADMIN: [
      "Users",
      "Fleet",
      "Categories",
      "Bookings",
      "Contracts",
      "Maintenance",
      "Promotions",
      "Branches",
    ],
    ROLE_RENTAL_OFFICER: [
      "Fleet",
      "Categories",
      "Bookings",
      "Contracts",
      "Maintenance",
      "Branches",
    ],
    ROLE_BOOKING_MANAGER: ["Bookings"],
    ROLE_MAINTENANCE_SUPERVISOR: ["Maintenance"],
    ROLE_FINANCE_OFFICER: ["Promotions", "Contracts"],
    ROLE_BRANCH_MANAGER: ["Branches", "Bookings", "Maintenance"],
  };
  const paths = {
    Users: "user-management",
    Fleet: "vehicle-management",
    Categories: "categories",
    Bookings: "booking-manager",
    Contracts: "contracts",
    Maintenance: "maintenance",
    Promotions: "promotions",
    Branches: "branches",
  };
  const menu = document.getElementById("mainLinks");
  let nav =
    user && user.role !== "ROLE_CUSTOMER"
      ? (links[user.role] || []).map((label) => [
          label,
          "/pages/" + paths[label] + ".html",
        ])
      : [
          ["Home", "/index.html"],
          ["Vehicles", "/pages/vehicles.html"],
          ...(user ? [["My bookings", "/pages/reservations.html"]] : []),
        ];
  if (menu)
    menu.innerHTML = nav
      .map(
        ([label, url]) =>
          `<a href="${url}" ${location.pathname === url ? 'aria-current="page"' : ""}>${label}</a>`,
      )
      .join("");
  container.innerHTML = user
    ? `<span class="greeting">Hi, ${escapeComponentHtml(user.firstName)}</span><button type="button" id="navLogout">Logout</button>`
    : '<a href="/pages/login.html">Login</a><a href="/pages/register.html">Register</a>';
  document
    .getElementById("navLogout")
    ?.addEventListener("click", () => AppAuth.logout());
  const toggle = document.querySelector(".nav-toggle");
  toggle?.addEventListener("click", () => {
    const open = toggle.closest("nav").classList.toggle("open");
    toggle.setAttribute("aria-expanded", String(open));
  });
}
function escapeComponentHtml(value) {
  return String(value ?? "").replace(
    /[&<>"']/g,
    (c) =>
      ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" })[
        c
      ],
  );
}
