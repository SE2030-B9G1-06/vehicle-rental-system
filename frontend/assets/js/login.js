document
  .getElementById("loginForm")
  .addEventListener("submit", async (event) => {
    event.preventDefault();
    const feedback = document.getElementById("loginFeedback");
    try {
      const response = await AppAuth.apiFetch("/api/auth/login", {
        method: "POST",
        body: JSON.stringify({
          email: document.getElementById("email").value.trim(),
          password: document.getElementById("password").value,
        }),
      });
      if (!response.ok)
        throw new Error(await AppAuth.responseMessage(response));
      const user = AppAuth.saveUser(await response.json());
      const pending = sessionStorage.getItem("pendingDestination");
      sessionStorage.removeItem("pendingDestination");
      if (
        user.role === "ROLE_CUSTOMER" &&
        pending &&
        pending.startsWith("/pages/")
      )
        window.location.href = pending;
      else AppAuth.redirectToRoleHome(user);
    } catch (error) {
      feedback.textContent = error.message;
      feedback.className = "feedback-message show error";
    }
  });
