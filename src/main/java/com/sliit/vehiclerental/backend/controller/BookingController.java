package com.sliit.vehiclerental.backend.controller;

import com.sliit.vehiclerental.backend.entity.Booking;
import com.sliit.vehiclerental.backend.service.BookingService;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

  private final BookingService bookingService;

  public BookingController(BookingService bookingService) {
    this.bookingService = bookingService;
  }

  @PostMapping
  public ResponseEntity<Booking> createBooking(@RequestBody Booking booking, HttpSession session) {
    Long userId = (Long) session.getAttribute(AuthController.SESSION_USER_ID);
    String role = (String) session.getAttribute(AuthController.SESSION_USER_ROLE);
    if (!"ROLE_CUSTOMER".equals(role))
      userId = booking.getCustomer() == null ? null : booking.getCustomer().getId();
    return ResponseEntity.ok(bookingService.createBooking(booking, userId));
  }

  @GetMapping
  public ResponseEntity<List<Booking>> getAllBookings(HttpSession session) {
    String role = (String) session.getAttribute(AuthController.SESSION_USER_ROLE);
    Long userId = (Long) session.getAttribute(AuthController.SESSION_USER_ID);
    if ("ROLE_CUSTOMER".equals(role)) {
      return ResponseEntity.ok(bookingService.getCustomerBookings(userId));
    }
    return ResponseEntity.ok(bookingService.getAllBookings());
  }

  @PutMapping("/{id}")
  public ResponseEntity<Booking> update(
      @PathVariable Long id, @RequestBody Booking booking, HttpSession session) {
    return ResponseEntity.ok(
        bookingService.updateBooking(
            id,
            booking,
            (Long) session.getAttribute(AuthController.SESSION_USER_ID),
            (String) session.getAttribute(AuthController.SESSION_USER_ROLE)));
  }

  @PutMapping("/{id}/status")
  public ResponseEntity<Booking> updateStatus(@PathVariable Long id, @RequestParam String status) {
    return ResponseEntity.ok(bookingService.updateBookingStatus(id, status));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<String> cancelBooking(@PathVariable Long id, HttpSession session) {
    bookingService.cancelBooking(
        id,
        (Long) session.getAttribute(AuthController.SESSION_USER_ID),
        (String) session.getAttribute(AuthController.SESSION_USER_ROLE));
    return ResponseEntity.ok("Booking cancelled successfully!");
  }
}
