package com.sliit.vehiclerental.backend.service;

import static com.sliit.vehiclerental.backend.service.Checks.*;

import com.sliit.vehiclerental.backend.entity.Branch;
import com.sliit.vehiclerental.backend.repository.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
public class BranchService {
  private final BranchRepository branches;
  private final VehicleRepository vehicles;
  private final BookingRepository bookings;

  public BranchService(BranchRepository b, VehicleRepository v, BookingRepository k) {
    branches = b;
    vehicles = v;
    bookings = k;
  }

  public List<Branch> getAllBranches() {
    return branches.findAll();
  }

  public Branch createBranch(Branch b) {
    b.setId(null);
    validate(b);
    b.setIsActive(true);
    return branches.save(b);
  }

  public Branch updateBranch(Integer id, Branch b) {
    Branch old = found(branches.lockById(id), "Branch");
    b.setId(id);
    validate(b);
    if (Boolean.FALSE.equals(b.getIsActive())) ensureClosable(id);
    old.setName(b.getName());
    old.setAddress(b.getAddress());
    old.setContactNumber(b.getContactNumber());
    old.setOperatingHours(b.getOperatingHours());
    old.setVehicleCapacity(b.getVehicleCapacity());
    old.setIsActive(!Boolean.FALSE.equals(b.getIsActive()));
    return branches.save(old);
  }

  public void deactivateBranch(Integer id) {
    Branch b = found(branches.lockById(id), "Branch");
    ensureClosable(id);
    b.setIsActive(false);
    branches.save(b);
  }

  private void ensureClosable(Integer id) {
    require(
        vehicles.countByBranchIdAndStatusNot(id, "INACTIVE") == 0,
        "Move or deactivate this branch's vehicles before closing it.");
    var active = List.of("PENDING", "CONFIRMED", "IN_PROGRESS");
    require(
        !bookings.existsByPickupBranchIdAndStatusInOrReturnBranchIdAndStatusIn(
            id, active, id, active),
        "Finish or cancel open bookings before closing this branch.");
  }

  private void validate(Branch b) {
    b.setName(text(b.getName(), 100, "Branch name"));
    b.setAddress(text(b.getAddress(), 2000, "Address"));
    phone(b.getContactNumber());
    b.setOperatingHours(text(b.getOperatingHours(), 100, "Operating hours"));
    require(
        b.getVehicleCapacity() != null && b.getVehicleCapacity() > 0, "Capacity must be positive.");
    branches
        .findByNameIgnoreCase(b.getName())
        .ifPresent(x -> require(x.getId().equals(b.getId()), "Branch name already exists."));
    if (b.getId() != null)
      require(
          b.getVehicleCapacity() >= vehicles.countByBranchIdAndStatusNot(b.getId(), "INACTIVE"),
          "Capacity is less than the active fleet at this branch.");
  }
}
