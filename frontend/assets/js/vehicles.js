let fleet = [];

document.addEventListener("DOMContentLoaded", async () => {
  await loadComponent("navbar-placeholder", "../components/navbar.html");
  hydrateSearchFromUrl();
  await Promise.all([loadVehicleFilterOptions(), fetchVehicles()]);
  document
    .getElementById("applyFilters")
    .addEventListener("click", fetchVehicles);
});

function hydrateSearchFromUrl() {
  const query = new URLSearchParams(window.location.search);
  document.getElementById("pickupFilter").value = query.get("pickup") || "";
  document.getElementById("returnFilter").value = query.get("return") || "";
  document.getElementById("branchFilter").dataset.initial =
    query.get("branch") || "";
  document.getElementById("categoryFilter").dataset.initial =
    query.get("category") || "";
}

async function loadVehicleFilterOptions() {
  try {
    const [branchesResponse, categoriesResponse] = await Promise.all([
      AppAuth.apiFetch("/api/branches"),
      AppAuth.apiFetch("/api/categories"),
    ]);
    const branches = branchesResponse.ok ? await branchesResponse.json() : [];
    const categories = categoriesResponse.ok
      ? await categoriesResponse.json()
      : [];
    const branchSelect = document.getElementById("branchFilter");
    const categorySelect = document.getElementById("categoryFilter");
    branchSelect.insertAdjacentHTML(
      "beforeend",
      branches
        .filter((b) => b.isActive !== false)
        .map((item) => `<option value="${item.id}">${safe(item.name)}</option>`)
        .join(""),
    );
    categorySelect.insertAdjacentHTML(
      "beforeend",
      categories
        .map((item) => `<option value="${item.id}">${safe(item.name)}</option>`)
        .join(""),
    );
    branchSelect.value = branchSelect.dataset.initial;
    categorySelect.value = categorySelect.dataset.initial;
    applyFilters();
  } catch (error) {
    console.error(error);
  }
}

async function fetchVehicles() {
  const grid = document.getElementById("vehicle-grid");
  try {
    const pickup = document.getElementById("pickupFilter").value;
    const returned = document.getElementById("returnFilter").value;
    if ((pickup && !returned) || (!pickup && returned))
      throw new Error("Select both pickup and return dates.");
    if (pickup && returned && returned <= pickup)
      throw new Error("Return date must be after pickup.");
    const path = pickup
      ? `/api/vehicles/available?pickup=${encodeURIComponent(pickup + "T09:00:00")}&returned=${encodeURIComponent(returned + "T09:00:00")}`
      : "/api/vehicles";
    const response = await AppAuth.apiFetch(path);
    if (!response.ok) throw new Error(await AppAuth.responseMessage(response));
    fleet = await response.json();
    applyFilters();
  } catch (error) {
    grid.innerHTML = `<div class="col-12"><div class="alert alert-danger">${safe(error.message)} Start Spring Boot and refresh the page.</div></div>`;
  }
}

function applyFilters() {
  const branchId = document.getElementById("branchFilter").value;
  const categoryId = document.getElementById("categoryFilter").value;
  const visible = fleet.filter(
    (vehicle) =>
      vehicle.status === "AVAILABLE" &&
      vehicle.isActive !== false &&
      vehicle.branch?.isActive !== false &&
      (!branchId || String(vehicle.branch?.id) === branchId) &&
      (!categoryId || String(vehicle.category?.id) === categoryId),
  );
  renderFleet(visible);
}

function renderFleet(vehicles) {
  document.getElementById("resultCount").textContent =
    `${vehicles.length} vehicle${vehicles.length === 1 ? "" : "s"} ready to book`;
  const grid = document.getElementById("vehicle-grid");
  if (!vehicles.length) {
    grid.innerHTML =
      '<div class="col-12"><div class="module-card bg-white p-5 text-center text-secondary">No available vehicles match these filters.</div></div>';
    return;
  }
  grid.innerHTML = vehicles
    .map((vehicle) => {
      const rate = new Intl.NumberFormat("en-LK", {
        style: "currency",
        currency: "LKR",
        maximumFractionDigits: 0,
      }).format(vehicle.dailyRate);
      return `<div class="col-md-6"><article class="vehicle-card">
            <div class="vehicle-photo">${vehicle.imageUrl ? `<img src="${safe(vehicle.imageUrl)}" alt="${safe(vehicle.make)} ${safe(vehicle.model)}" style="width:100%;height:170px;object-fit:cover" loading="lazy">` : '<i class="fa-solid fa-car-side" aria-hidden="true"></i>'}</div>
            <div class="card-body">
                <div class="d-flex justify-content-between mb-2"><span class="badge text-bg-light">${safe(vehicle.category?.name || "Vehicle")}</span><span class="badge status-available">Available</span></div>
                <h4 class="h5 mb-1">${safe(vehicle.make)} ${safe(vehicle.model)}</h4>
                <p class="vehicle-meta"><i class="fa-solid fa-location-dot me-1"></i>${safe(vehicle.branch?.name || "Unassigned")} · ${vehicle.year} · ${safe(vehicle.registrationNumber)}</p>
                <div class="d-flex justify-content-between align-items-center"><strong>${rate}<small class="fw-normal text-secondary">/day</small></strong><button class="btn btn-accent" onclick="bookVehicle(${vehicle.id})">View & book</button></div>
            </div>
        </article></div>`;
    })
    .join("");
}

async function bookVehicle(vehicleId) {
  const query = new URLSearchParams();
  query.set("vehicle", vehicleId);
  ["pickupFilter", "returnFilter"].forEach((id) => {
    const value = document.getElementById(id).value;
    if (value) query.set(id === "pickupFilter" ? "pickup" : "return", value);
  });
  const branch = document.getElementById("branchFilter").value;
  if (branch) query.set("branch", branch);
  const destination = `booking.html?${query}`;
  const user = await AppAuth.refreshUser();
  if (!user) {
    sessionStorage.setItem("pendingDestination", "/pages/" + destination);
    window.location.href = "../index.html?login=required";
    return;
  }
  if (user.role !== "ROLE_CUSTOMER") {
    AppAuth.redirectToRoleHome(user);
    return;
  }
  window.location.href = destination;
}

function safe(value) {
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
