package com.sliit.vehiclerental.backend.service;

import static com.sliit.vehiclerental.backend.service.Checks.*;

import com.sliit.vehiclerental.backend.entity.*;
import com.sliit.vehiclerental.backend.repository.*;
import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
public class BookingService {
  private final BookingRepository bookings;
  private final VehicleRepository vehicles;
  private final UserRepository users;
  private final BranchRepository branches;
  private final PromotionRepository promos;
  private final RentalContractRepository contracts;

  public BookingService(
      BookingRepository b,
      VehicleRepository v,
      UserRepository u,
      BranchRepository r,
      PromotionRepository p,
      RentalContractRepository c) {
    bookings = b;
    vehicles = v;
    users = u;
    branches = r;
    promos = p;
    contracts = c;
  }

  public Booking createBooking(Booking b, Long customerId) {
    require(customerId != null, "Select a customer.");
    User u = found(users.findById(customerId), "Customer");
    require(
        Boolean.TRUE.equals(u.getIsActive()) && "ROLE_CUSTOMER".equals(u.getRole().getName()),
        "Select an active customer.");
    b.setId(null);
    b.setCustomer(u);
    b.setStatus("PENDING");
    prepare(b);
    return bookings.save(b);
  }

  public List<Booking> getAllBookings() {
    return bookings.findAll();
  }

  public List<Booking> getCustomerBookings(Long id) {
    return bookings.findByCustomerIdOrderByPickupDatetimeDesc(id);
  }

  public Booking updateBooking(Long id, Booking data, Long requester, String role) {
    Booking b = found(bookings.lockById(id), "Booking");
    owner(b, requester, role);
    require(
        Set.of("PENDING", "CONFIRMED").contains(b.getStatus()),
        "Only pending or confirmed bookings can be edited.");
    require(
        !contracts.existsByBookingId(id),
        "A contract exists. Adjust the contract workflow before changing the booking.");
    require(
        data.getVehicle() != null
            && data.getVehicle().getId() != null
            && data.getVehicle().getId().equals(b.getVehicle().getId()),
        "Vehicle cannot be changed; cancel and create a new reservation.");
    // Validate and resolve request references before touching the managed booking.
    // Repository queries may flush managed entities; a blank optional promotion is not an entity.
    data.setId(b.getId());
    prepare(data);
    b.setPickupDatetime(data.getPickupDatetime());
    b.setReturnDatetime(data.getReturnDatetime());
    b.setPickupBranch(data.getPickupBranch());
    b.setReturnBranch(data.getReturnBranch());
    b.setVehicle(data.getVehicle());
    b.setPromotion(data.getPromotion());
    b.setTotalAmount(data.getTotalAmount());
    return bookings.save(b);
  }

  private void prepare(Booking b) {
    require(
        b.getPickupDatetime() != null
            && b.getReturnDatetime() != null
            && b.getReturnDatetime().isAfter(b.getPickupDatetime()),
        "Return time must be after pickup.");
    require(
        !b.getPickupDatetime().isBefore(LocalDateTime.now().minusMinutes(5)),
        "Pickup time cannot be in the past.");
    require(b.getVehicle() != null && b.getVehicle().getId() != null, "Select a vehicle.");
    Vehicle v = found(vehicles.lockById(b.getVehicle().getId()), "Vehicle");
    require(
        "AVAILABLE".equals(v.getStatus()) && !Boolean.FALSE.equals(v.getIsActive()),
        "Vehicle is not available.");
    require(
        bookings.conflicts(v.getId(), b.getId(), b.getPickupDatetime(), b.getReturnDatetime()) == 0,
        "Vehicle already reserved for these dates.");
    require(
        b.getPickupBranch() != null && b.getPickupBranch().getId() != null,
        "Select a pickup branch.");
    Integer pickupId = b.getPickupBranch().getId();
    Integer returnId = b.getReturnBranch() == null ? pickupId : b.getReturnBranch().getId();
    require(returnId != null, "Select a return branch.");
    for (Integer branchId : new java.util.TreeSet<>(List.of(pickupId, returnId)))
      found(branches.lockById(branchId), "Branch");
    Branch pickup = found(branches.findById(pickupId), "Pickup branch");
    Branch returned = found(branches.findById(returnId), "Return branch");
    require(
        Boolean.TRUE.equals(pickup.getIsActive()) && Boolean.TRUE.equals(returned.getIsActive()),
        "Choose active branches.");
    require(
        v.getBranch().getId().equals(pickup.getId()),
        "Collect this vehicle from " + v.getBranch().getName() + ".");
    b.setVehicle(v);
    b.setPickupBranch(pickup);
    b.setReturnBranch(returned);
    long days =
        Math.max(
            1,
            (long)
                Math.ceil(
                    Duration.between(b.getPickupDatetime(), b.getReturnDatetime()).toSeconds()
                        / 86400.0));
    BigDecimal total = v.getDailyRate().multiply(BigDecimal.valueOf(days));
    if (b.getPromotion() != null && b.getPromotion().getId() != null) {
      Promotion p = found(promos.lockById(b.getPromotion().getId()), "Promotion");
      LocalDate today = LocalDate.now();
      require(
          !Boolean.FALSE.equals(p.getIsActive())
              && (p.getStartDate() == null || !today.isBefore(p.getStartDate()))
              && (p.getEndDate() == null || !today.isAfter(p.getEndDate())),
          "Promotion is inactive or outside its validity dates.");
      long used = bookings.promoUses(p.getId(), b.getId());
      require(p.getMaxUses() == null || used < p.getMaxUses(), "Promotion usage limit reached.");
      BigDecimal discount =
          p.getFixedAmount() != null && p.getFixedAmount().signum() > 0
              ? p.getFixedAmount()
              : total
                  .multiply(p.getDiscountPercentage())
                  .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
      total = total.subtract(discount).max(BigDecimal.ZERO);
      b.setPromotion(p);
    } else b.setPromotion(null);
    b.setTotalAmount(total.setScale(2, RoundingMode.HALF_UP));
  }

  public Booking updateBookingStatus(Long id, String status) {
    Booking b = found(bookings.lockById(id), "Booking");
    String target = status == null ? "" : status.trim().toUpperCase();
    if (target.equals(b.getStatus())) return b;
    Map<String, Set<String>> next =
        Map.of(
            "PENDING",
            Set.of("CONFIRMED", "CANCELLED"),
            "CONFIRMED",
            Set.of("IN_PROGRESS", "CANCELLED"),
            "IN_PROGRESS",
            Set.of("COMPLETED"));
    require(
        next.getOrDefault(b.getStatus(), Set.of()).contains(target),
        "Invalid booking transition: " + b.getStatus() + " to " + target + ".");
    Vehicle v = found(vehicles.lockById(b.getVehicle().getId()), "Vehicle");
    if (Set.of("CONFIRMED", "IN_PROGRESS").contains(target)) {
      require(
          "AVAILABLE".equals(v.getStatus()) && Boolean.TRUE.equals(v.getBranch().getIsActive()),
          "Vehicle or branch is unavailable.");
      require(
          bookings.conflicts(v.getId(), b.getId(), b.getPickupDatetime(), b.getReturnDatetime())
              == 0,
          "Booking dates overlap another reservation.");
    }
    if ("IN_PROGRESS".equals(target)) {
      require(
          !LocalDateTime.now().isBefore(b.getPickupDatetime()), "Pickup time has not arrived yet.");
      contracts
          .findByBookingId(id)
          .ifPresent(
              c -> {
                require(
                    "DRAFT".equals(c.getStatus()),
                    "This booking's contract is not a valid draft. Cancel this booking and create a"
                        + " new reservation if its contract was voided.");
                require(
                    c.getSignature() != null && !c.getSignature().isBlank(),
                    "Capture the driver's acknowledgement in the contract before handover.");
              });
      v.setStatus("RENTED");
    }
    if ("COMPLETED".equals(target)) {
      Branch r =
          b.getReturnBranch() == null
              ? null
              : found(branches.lockById(b.getReturnBranch().getId()), "Return branch");
      if (r != null && !r.getId().equals(v.getBranch().getId())) {
        require(Boolean.TRUE.equals(r.getIsActive()), "Return branch is closed.");
        require(
            vehicles.countByBranchIdAndStatusNot(r.getId(), "INACTIVE") < r.getVehicleCapacity(),
            "Return branch is at capacity.");
        v.setBranch(r);
      }
      v.setStatus("AVAILABLE");
    }
    b.setStatus(target);
    vehicles.save(v);
    contracts
        .findByBookingId(id)
        .ifPresent(
            c -> {
              if ("CANCELLED".equals(target)) {
                c.setStatus("VOIDED");
              }
              if ("IN_PROGRESS".equals(target)) {
                c.setStatus("ACTIVE");
                c.setPickupTimestamp(LocalDateTime.now());
              }
              if ("COMPLETED".equals(target)) {
                c.setStatus("COMPLETED");
                c.setReturnTimestamp(LocalDateTime.now());
              }
              contracts.save(c);
            });
    return bookings.save(b);
  }

  public void cancelBooking(Long id, Long requester, String role) {
    Booking b = found(bookings.lockById(id), "Booking");
    owner(b, requester, role);
    if ("CANCELLED".equals(b.getStatus())) return;
    updateBookingStatus(id, "CANCELLED");
  }

  private void owner(Booking b, Long id, String role) {
    if ("ROLE_CUSTOMER".equals(role))
      require(b.getCustomer().getId().equals(id), "You can only change your own bookings.");
  }
}
