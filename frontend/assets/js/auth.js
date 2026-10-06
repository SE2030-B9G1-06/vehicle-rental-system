(function () {
  const API_BASE = "";

  const inPagesFolder = window.location.pathname.includes("/pages/");

  const ROLE_HOME = {
    ROLE_CUSTOMER: "index.html",

    ROLE_BOOKING_MANAGER: "pages/booking-manager.html",

    ROLE_RENTAL_OFFICER: "pages/vehicle-management.html",

    // Changed for System Admin
    ROLE_ADMIN: "pages/user-management.html",

    ROLE_MAINTENANCE_SUPERVISOR: "pages/maintenance.html",

    ROLE_FINANCE_OFFICER: "pages/promotions.html",

    ROLE_BRANCH_MANAGER: "pages/branches.html",
  };

  function appUrl(path) {
    const cleanPath = path.replace(/^\//, "");

    if (window.location.protocol.startsWith("http")) {
      return `/${cleanPath}`;
    }

    return `${inPagesFolder ? "../" : ""}${cleanPath}`;
  }

  async function apiFetch(path, options = {}) {
    const headers = new Headers(options.headers || {});

    if (options.body && !headers.has("Content-Type")) {
      headers.set("Content-Type", "application/json");
    }

    return fetch(`${API_BASE}${path}`, {
      ...options,
      headers,
      credentials: "include",
    });
  }

  async function responseMessage(response) {
    const contentType = response.headers.get("content-type") || "";

    if (contentType.includes("application/json")) {
      const data = await response.json().catch(() => ({}));

      return data.message || "The request could not be completed.";
    }

    return (await response.text()) || "The request could not be completed.";
  }

  function saveUser(user) {
    localStorage.setItem("loggedInUser", JSON.stringify(user));

    return user;
  }

  function getUser() {
    try {
      return JSON.parse(localStorage.getItem("loggedInUser"));
    } catch (_error) {
      localStorage.removeItem("loggedInUser");

      return null;
    }
  }

  async function refreshUser() {
    try {
      const response = await apiFetch("/api/auth/me");

      if (!response.ok) {
        localStorage.removeItem("loggedInUser");

        return null;
      }

      return saveUser(await response.json());
    } catch (_error) {
      localStorage.removeItem("loggedInUser");

      return null;
    }
  }

  function roleHome(role) {
    return appUrl(ROLE_HOME[role] || "index.html");
  }

  function redirectToRoleHome(user) {
    window.location.href = roleHome(user.role);
  }

  async function requireLogin(destination) {
    const user = await refreshUser();

    if (user) {
      if (destination) {
        window.location.href = destination;
      }

      return user;
    }

    if (destination) {
      sessionStorage.setItem("pendingDestination", destination);
    }

    window.location.href = appUrl("index.html?login=required");

    return null;
  }

  async function requireRole(allowedRoles) {
    const user = await refreshUser();

    if (!user) {
      window.location.href = appUrl("index.html?login=required");

      return null;
    }

    if (!allowedRoles.includes(user.role)) {
      redirectToRoleHome(user);

      return null;
    }

    return user;
  }

  async function logout() {
    try {
      await apiFetch("/api/auth/logout", {
        method: "POST",
      });
    } finally {
      localStorage.removeItem("loggedInUser");

      sessionStorage.removeItem("pendingDestination");

      window.location.href = appUrl("index.html");
    }
  }

  window.AppAuth = {
    apiFetch,
    responseMessage,
    saveUser,
    getUser,
    refreshUser,
    requireLogin,
    requireRole,
    redirectToRoleHome,
    roleHome,
    appUrl,
    logout,
  };

  window.requireAuthToNavigate = function (destination) {
    requireLogin(destination);

    return false;
  };

  window.logoutUser = logout;
})();
