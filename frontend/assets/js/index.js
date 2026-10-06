let allVehicles = [];

document.addEventListener("DOMContentLoaded", async () => {
  setDefaultDates();
  setupAuthForms();
  setupSearch();
  setupCategoryFilters();

  await Promise.all([
    loadBranches(),
    loadCategories(),
    loadFeaturedVehicles(),
  ]);

  const user = await AppAuth.refreshUser();
  updateNavbar(user);

  const parameters = new URLSearchParams(window.location.search);
  const loginRequired = parameters.get("login") === "required";
  const registerRequired = parameters.get("register") === "required";

  if (loginRequired && !user) {
    bootstrap.Modal.getOrCreateInstance(
      document.getElementById("loginModal"),
    ).show();
  } else if (registerRequired && !user) {
    bootstrap.Modal.getOrCreateInstance(
      document.getElementById("registerModal"),
    ).show();
  }
});

function setDefaultDates() {
  const pickup = new Date();
  pickup.setDate(pickup.getDate() + 1);

  const returned = new Date(pickup);
  returned.setDate(returned.getDate() + 3);

  document.getElementById("pickupDate").value = pickup
    .toISOString()
    .slice(0, 10);

  document.getElementById("returnDate").value = returned
    .toISOString()
    .slice(0, 10);
}

function showFeedback(id, message, type = "error") {
  const element = document.getElementById(id);

  if (!element) return;

  element.textContent = message;
  element.className = `feedback-message show ${type}`;
}

function setButtonBusy(button, busy, busyText) {
  if (!button.dataset.originalText) {
    button.dataset.originalText = button.textContent;
  }

  button.disabled = busy;
  button.textContent = busy
    ? busyText
    : button.dataset.originalText;
}

function setupAuthForms() {
  const loginForm = document.getElementById("modalLoginForm");
  const registerForm = document.getElementById("modalRegisterForm");

  if (loginForm) {
    loginForm.addEventListener("submit", async (event) => {
      event.preventDefault();

      const button = document.getElementById("loginButton");
      setButtonBusy(button, true, "Signing in...");

      try {
        const response = await AppAuth.apiFetch("/api/auth/login", {
          method: "POST",
          body: JSON.stringify({
            email: document.getElementById("loginEmail").value.trim(),
            password: document.getElementById("loginPassword").value,
          }),
        });

        if (!response.ok) {
          throw new Error(
            await AppAuth.responseMessage(response),
          );
        }

        const user = AppAuth.saveUser(await response.json());

        showFeedback(
          "loginFeedback",
          `Welcome, ${user.firstName}.`,
          "success",
        );

        const pendingDestination =
          sessionStorage.getItem("pendingDestination");

        sessionStorage.removeItem("pendingDestination");

        if (
          user.role === "ROLE_CUSTOMER" &&
          pendingDestination
        ) {
          const destination = new URL(
            pendingDestination,
            location.origin + "/pages/",
          );

          window.location.href =
            destination.pathname + destination.search;
        } else if (user.role === "ROLE_CUSTOMER") {
          window.location.reload();
        } else {
          AppAuth.redirectToRoleHome(user);
        }
      } catch (error) {
        showFeedback("loginFeedback", error.message);
      } finally {
        setButtonBusy(button, false, "Signing in...");
      }
    });
  }

  if (registerForm) {
    registerForm.addEventListener("submit", async (event) => {
      event.preventDefault();

      const password =
        document.getElementById("regPassword").value;

      const confirmPassword =
        document.getElementById("regConfirm").value;

      if (password !== confirmPassword) {
        showFeedback(
          "registerFeedback",
          "Passwords do not match.",
        );

        return;
      }

      const button = document.getElementById("registerButton");
      setButtonBusy(button, true, "Creating account...");

      try {
        const payload = {
          firstName: document
            .getElementById("regFirstName")
            .value.trim(),

          lastName: document
            .getElementById("regLastName")
            .value.trim(),

          email: document
            .getElementById("regEmail")
            .value.trim(),

          contactNumber: document
            .getElementById("regContact")
            .value.trim(),

          drivingLicenceNumber: document
            .getElementById("regLicense")
            .value.trim(),

          password,
        };

        const response = await AppAuth.apiFetch(
          "/api/auth/register",
          {
            method: "POST",
            body: JSON.stringify(payload),
          },
        );

        if (!response.ok) {
          throw new Error(
            await AppAuth.responseMessage(response),
          );
        }

        registerForm.reset();

        document.getElementById("loginEmail").value =
          payload.email;

        bootstrap.Modal.getOrCreateInstance(
          document.getElementById("registerModal"),
        ).hide();

        setTimeout(() => {
          bootstrap.Modal.getOrCreateInstance(
            document.getElementById("loginModal"),
          ).show();
        }, 250);

        showFeedback(
          "loginFeedback",
          "Account created. Sign in to continue.",
          "success",
        );
      } catch (error) {
        showFeedback("registerFeedback", error.message);
      } finally {
        setButtonBusy(button, false, "Creating account...");
      }
    });
  }
}

function updateNavbar(user) {
  const container =
    document.getElementById("nav-auth-container");

  if (!container) return;

  if (!user) {
    container.innerHTML = `
      <a
        class="btn btn-pill btn-outline-navy"
        href="/pages/login.html"
      >
        Login
      </a>

      <a
        class="btn btn-pill btn-navy"
        href="/pages/register.html"
      >
        Register
      </a>
    `;

    return;
  }

  const destination =
    user.role === "ROLE_CUSTOMER"
      ? "/pages/reservations.html"
      : AppAuth.roleHome(user.role);

  const label =
    user.role === "ROLE_CUSTOMER"
      ? "My bookings"
      : "My dashboard";

  container.innerHTML = `
    <span class="small fw-bold me-1">
      Hi, ${escapeHtml(user.firstName)}
    </span>

    <a
      class="btn btn-pill btn-outline-navy"
      href="${destination}"
    >
      ${label}
    </a>

    <button
      class="btn btn-pill btn-navy"
      type="button"
      onclick="logoutUser()"
    >
      Logout
    </button>
  `;
}

function setupSearch() {
  const searchForm =
    document.getElementById("vehicleSearchForm");

  if (!searchForm) return;

  searchForm.addEventListener("submit", (event) => {
    event.preventDefault();

    const pickup =
      document.getElementById("pickupDate").value;

    const returned =
      document.getElementById("returnDate").value;

    if (new Date(returned) <= new Date(pickup)) {
      alert("Return date must be after the pickup date.");
      return;
    }

    const query = new URLSearchParams({
      branch:
        document.getElementById("pickupLocation").value,

      pickup,

      return: returned,

      category:
        document.getElementById("vehicleCategory").value,
    });

    window.location.href = `pages/vehicles.html?${query}`;
  });
}

async function loadBranches() {
  const select =
    document.getElementById("pickupLocation");

  if (!select) return;

  try {
    const response =
      await AppAuth.apiFetch("/api/branches");

    if (!response.ok) {
      throw new Error("Could not load branches.");
    }

    const branches = await response.json();

    select.innerHTML =
      '<option value="">Choose a location</option>' +
      branches
        .filter((branch) => branch.isActive !== false)
        .map(
          (branch) => `
            <option value="${branch.id}">
              ${escapeHtml(branch.name)}
            </option>
          `,
        )
        .join("");
  } catch (error) {
    select.innerHTML = `
      <option value="">
        Start the backend to load branches
      </option>
    `;
  }
}

async function loadCategories() {
  try {
    const response =
      await AppAuth.apiFetch("/api/categories");

    if (!response.ok) return;

    const categories = await response.json();

    const select =
      document.getElementById("vehicleCategory");

    const filters =
      document.getElementById("categoryFilters");

    categories.forEach((category) => {
      if (select) {
        select.insertAdjacentHTML(
          "beforeend",
          `
            <option value="${category.id}">
              ${escapeHtml(category.name)}
            </option>
          `,
        );
      }

      if (filters) {
        filters.insertAdjacentHTML(
          "beforeend",
          `
            <button
              class="category-filter"
              type="button"
              data-category="${escapeHtml(category.name)}"
            >
              ${escapeHtml(category.name)}
            </button>
          `,
        );
      }
    });
  } catch (error) {
    console.error("Could not load categories:", error);
  }
}

function setupCategoryFilters() {
  const filters =
    document.getElementById("categoryFilters");

  if (!filters) return;

  filters.addEventListener("click", (event) => {
    const button =
      event.target.closest("[data-category]");

    if (!button) return;

    document
      .querySelectorAll(".category-filter")
      .forEach((item) => {
        item.classList.remove("active");
      });

    button.classList.add("active");

    const category = button.dataset.category;

    if (category === "ALL") {
      renderVehicles(allVehicles);
      return;
    }

    const filteredVehicles = allVehicles.filter(
      (vehicle) =>
        vehicle.category?.name === category,
    );

    renderVehicles(filteredVehicles);
  });
}

async function loadFeaturedVehicles() {
  try {
    const response =
      await AppAuth.apiFetch("/api/vehicles");

    if (!response.ok) {
      throw new Error("Could not load vehicles.");
    }

    const vehicles = await response.json();

    allVehicles = vehicles.filter(
      (vehicle) =>
        vehicle.status === "AVAILABLE" &&
        vehicle.isActive !== false &&
        vehicle.branch?.isActive !== false,
    );

    renderVehicles(allVehicles);
  } catch (error) {
    const container =
      document.getElementById("featuredVehicles");

    if (container) {
      container.innerHTML = `
        <div class="w-100 py-5 text-center text-danger">
          Unable to load vehicles.
          Start Spring Boot and refresh this page.
        </div>
      `;
    }
  }
}

function renderVehicles(vehicles) {
  const container =
    document.getElementById("featuredVehicles");

  if (!container) return;

  if (!vehicles.length) {
    container.innerHTML = `
      <div class="w-100 py-5 text-center text-secondary">
        No available vehicles match this category.
      </div>
    `;

    return;
  }

  container.innerHTML = vehicles
    .map((vehicle) => {
      const rate = new Intl.NumberFormat("en-LK", {
        style: "currency",
        currency: "LKR",
        maximumFractionDigits: 0,
      }).format(vehicle.dailyRate);

      /*
       * This section displays the saved vehicle image.
       * If imageUrl is empty, the vehicle icon is displayed.
       */
      const vehiclePhoto = vehicle.imageUrl
        ? `
          <img
            src="${escapeHtml(vehicle.imageUrl)}"
            alt="${escapeHtml(
              `${vehicle.make} ${vehicle.model}`,
            )}"
            class="vehicle-card-image"
            loading="lazy"
            onerror="showVehicleImageFallback(this)"
          >
        `
        : `
          <i
            class="fa-solid fa-car-side"
            aria-hidden="true"
          ></i>
        `;

      return `
        <article
          class="col-card"
          data-category="${escapeHtml(
            vehicle.category?.name || "",
          )}"
        >
          <div class="vehicle-card">

            <div class="vehicle-photo">
              ${vehiclePhoto}
            </div>

            <div class="card-body">

              <div
                class="d-flex justify-content-between gap-2 mb-2"
              >
                <span class="badge text-bg-light">
                  ${escapeHtml(
                    vehicle.category?.name || "Vehicle",
                  )}
                </span>

                <span class="badge status-available">
                  Available
                </span>
              </div>

              <h5 class="mb-1">
                ${escapeHtml(vehicle.make)}
                ${escapeHtml(vehicle.model)}
              </h5>

              <div class="vehicle-meta mb-3">
                <i
                  class="fa-solid fa-location-dot me-1"
                ></i>

                ${escapeHtml(
                  vehicle.branch?.name || "Branch pending",
                )}
                ·
                ${escapeHtml(vehicle.year)}
              </div>

              <div
                class="d-flex align-items-center justify-content-between gap-2"
              >
                <strong>
                  ${rate}
                  <small
                    class="text-secondary fw-normal"
                  >
                    /day
                  </small>
                </strong>

                <button
                  class="btn btn-sm btn-accent"
                  type="button"
                  onclick="beginBooking(${vehicle.id})"
                >
                  View details
                </button>
              </div>

            </div>
          </div>
        </article>
      `;
    })
    .join("");
}

/*
 * If the image filename or URL is incorrect,
 * replace the broken image with the original car icon.
 */
function showVehicleImageFallback(image) {
  const icon = document.createElement("i");

  icon.className = "fa-solid fa-car-side";
  icon.setAttribute("aria-hidden", "true");

  image.replaceWith(icon);
}

async function beginBooking(vehicleId) {
  sessionStorage.setItem(
    "selectedVehicleId",
    String(vehicleId),
  );

  const destination =
    `/pages/booking.html?vehicle=${vehicleId}`;

  const user = await AppAuth.refreshUser();

  if (!user) {
    sessionStorage.setItem(
      "pendingDestination",
      destination,
    );

    bootstrap.Modal.getOrCreateInstance(
      document.getElementById("authRequiredModal"),
    ).show();

    return;
  }

  if (user.role !== "ROLE_CUSTOMER") {
    AppAuth.redirectToRoleHome(user);
    return;
  }

  window.location.href = destination;
}

function scrollCarousel(amount) {
  const carousel =
    document.getElementById("featuredVehicles");

  if (!carousel) return;

  carousel.scrollBy({
    left: amount,
    behavior: "smooth",
  });
}

function escapeHtml(value) {
  return String(value ?? "").replace(
    /[&<>'"]/g,
    (character) =>
      ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        "'": "&#039;",
        '"': "&quot;",
      })[character],
  );
}