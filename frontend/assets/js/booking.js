let selectedVehicle = null;
let bookingPromotions = [];

document.addEventListener("DOMContentLoaded", async () => {
  const user = await AppAuth.requireRole(["ROLE_CUSTOMER"]);
  if (!user) return;
  await loadComponent("navbar-placeholder", "../components/navbar.html");
  setBookingDates();
  await Promise.all([
    loadBookingBranches(),
    loadSelectedVehicle(),
    loadBookingPromotions(),
  ]);
  const queryBranch = new URLSearchParams(window.location.search).get("branch");
  if (!queryBranch && selectedVehicle?.branch?.id) {
    document.getElementById("pickupBranch").value = String(
      selectedVehicle.branch.id,
    );
    document.getElementById("returnBranch").value = String(
      selectedVehicle.branch.id,
    );
  }
  document
    .getElementById("bookingForm")
    .addEventListener("submit", submitBooking);
  document
    .getElementById("pickupDate")
    .addEventListener("change", updateEstimate);
  document
    .getElementById("returnDate")
    .addEventListener("change", updateEstimate);
  document
    .getElementById("promotionId")
    .addEventListener("change", updateEstimate);
});

function setBookingDates() {
  const query = new URLSearchParams(window.location.search);
  const pickup = query.get("pickup")
    ? new Date(`${query.get("pickup")}T09:00`)
    : new Date(Date.now() + 86400000);
  const returned = query.get("return")
    ? new Date(`${query.get("return")}T09:00`)
    : new Date(pickup.getTime() + 3 * 86400000);
  document.getElementById("pickupDate").value = localDateTime(pickup);
  document.getElementById("returnDate").value = localDateTime(returned);
}

function localDateTime(date) {
  const offset = date.getTimezoneOffset();
  return new Date(date.getTime() - offset * 60000).toISOString().slice(0, 16);
}

async function loadBookingBranches() {
  const queryBranch = new URLSearchParams(window.location.search).get("branch");
  const response = await AppAuth.apiFetch("/api/branches");
  if (!response.ok) return;
  const branches = (await response.json()).filter(
    (branch) => branch.isActive !== false,
  );
  const options =
    '<option value="">Choose a branch</option>' +
    branches
      .map(
        (branch) =>
          `<option value="${branch.id}">${safeBooking(branch.name)}</option>`,
      )
      .join("");
  document.getElementById("pickupBranch").innerHTML = options;
  document.getElementById("returnBranch").innerHTML = options;
  if (queryBranch) {
    document.getElementById("pickupBranch").value = queryBranch;
    document.getElementById("returnBranch").value = queryBranch;
  }
}

async function loadSelectedVehicle() {
  const id =
    new URLSearchParams(window.location.search).get("vehicle") ||
    sessionStorage.getItem("selectedVehicleId");
  const response = await AppAuth.apiFetch("/api/vehicles");
  if (!response.ok)
    return showBookingFeedback("Unable to load vehicle information.");
  selectedVehicle = (await response.json()).find(
    (vehicle) => String(vehicle.id) === String(id),
  );
  if (!selectedVehicle) {
    showBookingFeedback(
      "Please return to Vehicles and select an available vehicle.",
    );
    document.getElementById("confirmBookingButton").disabled = true;
    return;
  }
  sessionStorage.setItem("selectedVehicleId", String(selectedVehicle.id));
  document.getElementById("selectedVehicle").innerHTML =
    `<h4 class="h5 fw-bold text-dark mb-1">${safeBooking(selectedVehicle.make)} ${safeBooking(selectedVehicle.model)}</h4><div>${safeBooking(selectedVehicle.category?.name || "Vehicle")} · ${selectedVehicle.year}<br>${safeBooking(selectedVehicle.branch?.name || "")}</div>`;
  document.getElementById("dailyRate").textContent = formatCurrency(
    selectedVehicle.dailyRate,
  );
  updateEstimate();
}

function updateEstimate() {
  if (!selectedVehicle) return;
  const pickup = new Date(document.getElementById("pickupDate").value);
  const returned = new Date(document.getElementById("returnDate").value);
  const days = Math.max(1, Math.ceil((returned - pickup) / 86400000));
  document.getElementById("estDays").textContent = Number.isFinite(days)
    ? days
    : "-";
  let total = days * Number(selectedVehicle.dailyRate);
  const promotion = bookingPromotions.find(
    (p) => String(p.id) === document.getElementById("promotionId").value,
  );
  if (promotion)
    total = Math.max(
      0,
      total -
        (Number(promotion.fixedAmount) > 0
          ? Number(promotion.fixedAmount)
          : (total * Number(promotion.discountPercentage)) / 100),
    );
  document.getElementById("totalPrice").textContent = Number.isFinite(total)
    ? formatCurrency(total)
    : "-";
}

async function submitBooking(event) {
  event.preventDefault();
  if (!selectedVehicle) return;
  const pickupDatetime = document.getElementById("pickupDate").value;
  const returnDatetime = document.getElementById("returnDate").value;
  if (new Date(returnDatetime) <= new Date(pickupDatetime))
    return showBookingFeedback("Return date must be after pickup date.");
  const button = document.getElementById("confirmBookingButton");
  button.disabled = true;
  button.textContent = "Confirming...";
  try {
    const payload = {
      vehicle: { id: selectedVehicle.id },
      pickupBranch: {
        id: Number(document.getElementById("pickupBranch").value),
      },
      returnBranch: {
        id: Number(document.getElementById("returnBranch").value),
      },
      pickupDatetime,
      returnDatetime,
      promotion: document.getElementById("promotionId").value
        ? { id: Number(document.getElementById("promotionId").value) }
        : null,
    };
    const response = await AppAuth.apiFetch("/api/bookings", {
      method: "POST",
      body: JSON.stringify(payload),
    });
    if (response.status === 401)
      return AppAuth.requireLogin(window.location.href);
    if (!response.ok) throw new Error(await AppAuth.responseMessage(response));
    const booking = await response.json();
    sessionStorage.removeItem("selectedVehicleId");
    showBookingFeedback(
      `Booking #${booking.id} was submitted successfully.`,
      "success",
    );
    setTimeout(() => {
      window.location.href = "reservations.html";
    }, 800);
  } catch (error) {
    showBookingFeedback(error.message);
  } finally {
    button.disabled = false;
    button.textContent = "Confirm reservation";
  }
}

function showBookingFeedback(message, type = "error") {
  const element = document.getElementById("bookingFeedback");
  element.textContent = message;
  element.className = `feedback-message show ${type}`;
}
function formatCurrency(value) {
  return new Intl.NumberFormat("en-LK", {
    style: "currency",
    currency: "LKR",
    maximumFractionDigits: 0,
  }).format(value);
}
function safeBooking(value) {
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

async function loadBookingPromotions() {
  try {
    const response = await AppAuth.apiFetch("/api/promotions");
    if (!response.ok) return;
    const today = localDateTime(new Date()).slice(0, 10);
    bookingPromotions = (await response.json()).filter(
      (p) =>
        p.isActive !== false &&
        (!p.startDate || p.startDate <= today) &&
        (!p.endDate || p.endDate >= today),
    );
    document.getElementById("promotionId").innerHTML =
      '<option value="">No promotion</option>' +
      bookingPromotions
        .map(
          (p) =>
            `<option value="${p.id}">${safeBooking(p.code)} — ${Number(p.fixedAmount) > 0 ? formatCurrency(p.fixedAmount) : p.discountPercentage + "%"} off</option>`,
        )
        .join("");
    updateEstimate();
  } catch (e) {
    showBookingFeedback(
      "Promotions could not be loaded. You can still book without a promotion.",
    );
  }
}
