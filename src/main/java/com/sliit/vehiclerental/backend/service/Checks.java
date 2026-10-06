package com.sliit.vehiclerental.backend.service;

import java.math.BigDecimal;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class Checks {
  private Checks() {}

  public static void require(boolean condition, String message) {
    if (!condition) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
  }

  public static <T> T found(java.util.Optional<T> item, String name) {
    return item.orElseThrow(
        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, name + " not found."));
  }

  public static String text(String s, int max, String label) {
    require(
        s != null && !s.isBlank() && s.trim().length() <= max,
        label + " is required (maximum " + max + " characters).");
    return s.trim();
  }

  public static void money(BigDecimal v, boolean zero, String label) {
    require(
        v != null
            && (zero ? v.signum() >= 0 : v.signum() > 0)
            && v.compareTo(new BigDecimal("99999999.99")) <= 0,
        label + " must be a valid " + (zero ? "non-negative" : "positive") + " amount.");
  }

  public static void phone(String s) {
    require(
        s != null && s.matches("[0-9+() -]{7,20}"),
        "Enter a valid contact number (7–20 characters).");
  }
}
