package com.sliit.vehiclerental.backend.config;

import com.sliit.vehiclerental.backend.controller.AuthController;
import com.sliit.vehiclerental.backend.entity.User;
import com.sliit.vehiclerental.backend.repository.UserRepository;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AccessInterceptor implements HandlerInterceptor {
  private final UserRepository users;

  public AccessInterceptor(UserRepository u) {
    users = u;
  }

  private static final Set<String> BOOKING =
      Set.of("ROLE_ADMIN", "ROLE_BOOKING_MANAGER", "ROLE_RENTAL_OFFICER", "ROLE_BRANCH_MANAGER");
  private static final Set<String> FLEET = Set.of("ROLE_ADMIN", "ROLE_RENTAL_OFFICER");
  private static final Set<String> MAINTENANCE =
      Set.of(
          "ROLE_ADMIN",
          "ROLE_RENTAL_OFFICER",
          "ROLE_MAINTENANCE_SUPERVISOR",
          "ROLE_BRANCH_MANAGER");
  private static final Set<String> FINANCE = Set.of("ROLE_ADMIN", "ROLE_FINANCE_OFFICER");
  private static final Set<String> CONTRACT =
      Set.of("ROLE_ADMIN", "ROLE_RENTAL_OFFICER", "ROLE_FINANCE_OFFICER");
  private static final Set<String> BRANCH =
      Set.of("ROLE_ADMIN", "ROLE_BRANCH_MANAGER", "ROLE_RENTAL_OFFICER");
  private static final Map<String, Set<String>> PAGES =
      Map.ofEntries(
          Map.entry("booking-manager.html", BOOKING),
          Map.entry("vehicle-management.html", FLEET),
          Map.entry("fleet-manager.html", FLEET),
          Map.entry("categories.html", FLEET),
          Map.entry("maintenance.html", MAINTENANCE),
          Map.entry("promotions.html", FINANCE),
          Map.entry("contracts.html", CONTRACT),
          Map.entry("branches.html", BRANCH),
          Map.entry("user-management.html", Set.of("ROLE_ADMIN")),
          Map.entry("booking.html", Set.of("ROLE_CUSTOMER")),
          Map.entry("reservations.html", Set.of("ROLE_CUSTOMER")));

  @Override
  public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler)
      throws IOException {
    String path = req.getRequestURI(), method = req.getMethod();
    if ("OPTIONS".equals(method)) return true;
    if (path.startsWith("/api/") && !Set.of("GET", "HEAD", "OPTIONS").contains(method)) {
      String origin = req.getHeader("Origin");
      String own =
          req.getScheme()
              + "://"
              + req.getServerName()
              + ((req.getServerPort() == 80 || req.getServerPort() == 443)
                  ? ""
                  : ":" + req.getServerPort());
      if (origin != null && !origin.equals(own))
        return error(
            res, 403, "Open the application from the Spring Boot address to submit changes.");
      if (Set.of("POST", "PUT").contains(method)
          && !path.equals("/api/auth/logout")
          && (req.getContentType() == null || !req.getContentType().startsWith("application/json")))
        return error(res, 415, "Use JSON for this request.");
    }
    HttpSession session = req.getSession(false);
    String role = null;
    if (session != null
        && session.getAttribute(AuthController.SESSION_USER_ID) instanceof Long id) {
      User u = users.findById(id).orElse(null);
      if (u == null || !Boolean.TRUE.equals(u.getIsActive())) session.invalidate();
      else {
        role = u.getRole().getName();
        session.setAttribute(AuthController.SESSION_USER_ROLE, role);
      }
    }
    if (path.equals("/api/auth/login")
        || path.equals("/api/auth/register")
        || path.equals("/api/auth/logout")
        || path.equals("/api/health")) return true;
    if (path.startsWith("/api/")) {
      if ("GET".equals(method)
          && Set.of(
                  "/api/vehicles",
                  "/api/vehicles/available",
                  "/api/branches",
                  "/api/categories",
                  "/api/promotions")
              .contains(path)) return true;
      if (role == null) return error(res, 401, "Please sign in to continue.");
      boolean allowed = false;
      if (path.equals("/api/auth/me")) allowed = true;
      else if (path.equals("/api/users/customers") && "GET".equals(method))
        allowed = BOOKING.contains(role);
      else if (path.equals("/api/users") || path.startsWith("/api/users/"))
        allowed = "ROLE_ADMIN".equals(role);
      else if (path.equals("/api/bookings") || path.startsWith("/api/bookings/"))
        allowed =
            BOOKING.contains(role)
                || ("GET".equals(method) && FINANCE.contains(role))
                || ("ROLE_CUSTOMER".equals(role) && !path.endsWith("/status"));
      else if (path.startsWith("/api/vehicles") || path.startsWith("/api/categories"))
        allowed = FLEET.contains(role);
      else if (path.startsWith("/api/branches")) allowed = BRANCH.contains(role);
      else if (path.startsWith("/api/maintenance")) allowed = MAINTENANCE.contains(role);
      else if (path.startsWith("/api/promotions")) allowed = FINANCE.contains(role);
      else if (path.startsWith("/api/contracts")) allowed = CONTRACT.contains(role);
      if (!allowed) return error(res, 403, "Your role does not have permission for this action.");
    } else if (path.startsWith("/pages/")) {
      Set<String> roles = PAGES.get(path.substring(7));
      if (roles != null && (role == null || !roles.contains(role))) {
        res.sendRedirect("/pages/login.html");
        return false;
      }
    }
    return true;
  }

  private boolean error(HttpServletResponse r, int status, String msg) throws IOException {
    r.setStatus(status);
    r.setContentType("application/json");
    r.getWriter().write("{\"message\":\"" + msg + "\"}");
    return false;
  }
}
