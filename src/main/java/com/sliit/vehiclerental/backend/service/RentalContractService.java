package com.sliit.vehiclerental.backend.service;

import static com.sliit.vehiclerental.backend.service.Checks.*;

import com.sliit.vehiclerental.backend.entity.*;
import com.sliit.vehiclerental.backend.repository.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
public class RentalContractService {
  private final RentalContractRepository contracts;
  private final BookingRepository bookings;
  private final BookingService bookingService;

  public RentalContractService(RentalContractRepository c, BookingRepository b, BookingService s) {
    contracts = c;
    bookings = b;
    bookingService = s;
  }

  public List<RentalContract> getAllContracts() {
    return contracts.findAll();
  }

  public RentalContract createContract(RentalContract c) {
    c.setId(null);
    require(
        c.getBooking() != null && c.getBooking().getId() != null, "Select a confirmed booking.");
    Booking b = found(bookings.lockById(c.getBooking().getId()), "Booking");
    require("CONFIRMED".equals(b.getStatus()), "Only confirmed bookings can receive a contract.");
    require(!contracts.existsByBookingId(b.getId()), "This booking already has a contract.");
    validate(c);
    c.setBooking(b);
    c.setStatus("DRAFT");
    c.setPickupTimestamp(null);
    c.setReturnTimestamp(null);
    return contracts.save(c);
  }

  public RentalContract updateContract(Long id, RentalContract c) {
    RentalContract old = found(contracts.findById(id), "Contract");
    require(
        !Set.of("VOIDED", "COMPLETED").contains(old.getStatus()),
        "Completed or voided contracts cannot be edited.");
    validate(c);
    String state = c.getStatus();
    require(
        state != null && Set.of("DRAFT", "ACTIVE", "COMPLETED", "VOIDED").contains(state),
        "Invalid contract status.");
    if (!state.equals(old.getStatus())) {
      if ("ACTIVE".equals(state)) {
        require("DRAFT".equals(old.getStatus()), "Only draft contracts can start.");
        require(
            c.getSignature() != null && !c.getSignature().isBlank(),
            "Capture the driver's signature before handover.");
        old.setSignature(c.getSignature());
        bookingService.updateBookingStatus(old.getBooking().getId(), "IN_PROGRESS");
      } else if ("COMPLETED".equals(state)) {
        require("ACTIVE".equals(old.getStatus()), "Only active contracts can be completed.");
        bookingService.updateBookingStatus(old.getBooking().getId(), "COMPLETED");
      } else if ("VOIDED".equals(state)) {
        require("DRAFT".equals(old.getStatus()), "Only draft contracts can be voided.");
      } else require(false, "Invalid contract transition.");
    }
    old.setDriverName(c.getDriverName());
    old.setLicenseNumber(c.getLicenseNumber());
    old.setIdentityDocument(c.getIdentityDocument());
    old.setContactDetails(c.getContactDetails());
    old.setSignature(c.getSignature());
    old.setStatus(state);
    return contracts.save(old);
  }

  public void deleteContract(Long id) {
    RentalContract c = found(contracts.findById(id), "Contract");
    require(
        Set.of("DRAFT", "VOIDED").contains(c.getStatus()),
        "Only an unfulfilled draft can be voided.");
    c.setStatus("VOIDED");
    contracts.save(c);
  }

  private void validate(RentalContract c) {
    c.setDriverName(text(c.getDriverName(), 100, "Driver name"));
    c.setLicenseNumber(text(c.getLicenseNumber(), 50, "Licence number"));
    c.setIdentityDocument(text(c.getIdentityDocument(), 50, "Identity document"));
    c.setContactDetails(text(c.getContactDetails(), 50, "Contact details"));
    require(c.getSignature() == null || c.getSignature().length() <= 255, "Signature is too long.");
  }
}
