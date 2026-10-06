document
  .getElementById("registerForm")
  .addEventListener("submit", async (event) => {
    event.preventDefault();
    const button = event.target.querySelector("button[type=submit]");
    button.disabled = true;
    const newUser = {
      firstName: document.getElementById("firstName").value.trim(),
      lastName: document.getElementById("lastName").value.trim(),
      email: document.getElementById("email").value.trim(),
      contactNumber: document.getElementById("contactNumber").value.trim(),
      drivingLicenceNumber: document
        .getElementById("drivingLicenceNumber")
        .value.trim(),
      password: document.getElementById("password").value,
    };
    try {
      const response = await AppAuth.apiFetch("/api/auth/register", {
        method: "POST",
        body: JSON.stringify(newUser),
      });
      if (!response.ok)
        throw new Error(await AppAuth.responseMessage(response));
      showRegisterMessage("Registration successful. Opening sign in…", true);
      window.location.href = "login.html";
    } catch (error) {
      showRegisterMessage(error.message, false);
    } finally {
      button.disabled = false;
    }
  });

function showRegisterMessage(text, ok) {
  let box = document.getElementById("registerFeedback");
  if (!box) {
    box = document.createElement("div");
    box.id = "registerFeedback";
    box.setAttribute("role", "status");
    document.getElementById("registerForm").prepend(box);
  }
  box.className = "alert " + (ok ? "alert-success" : "alert-danger");
  box.textContent = text;
}
