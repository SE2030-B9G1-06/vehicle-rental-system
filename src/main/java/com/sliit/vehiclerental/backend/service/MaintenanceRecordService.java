package com.sliit.vehiclerental.backend.service;

import static com.sliit.vehiclerental.backend.service.Checks.*;

import com.sliit.vehiclerental.backend.entity.*;
import com.sliit.vehiclerental.backend.repository.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
public class MaintenanceRecordService {
  private final MaintenanceRecordRepository records;
  private final VehicleRepository vehicles;
  private final BookingRepository bookings;

  public MaintenanceRecordService(
      MaintenanceRecordRepository r, VehicleRepository v, BookingRepository b) {
    records = r;
    vehicles = v;
    bookings = b;
  }

  public List<MaintenanceRecord> getAllRecords() {
    return records.findAll();
  }

  public MaintenanceRecord createRecord(MaintenanceRecord r) {
    r.setId(null);
    validate(r);
    Vehicle v = lock(r);
    ensureCanBlock(v, r.getStatus());
    r.setVehicle(v);
    records.saveAndFlush(r);
    sync(v);
    return r;
  }

  public MaintenanceRecord updateRecord(Long id, MaintenanceRecord r) {
    MaintenanceRecord old = found(records.findById(id), "Maintenance record");
    validate(r);
    require(
        r.getVehicle() != null
            && r.getVehicle().getId() != null
            && r.getVehicle().getId().equals(old.getVehicle().getId()),
        "Vehicle cannot be changed on an existing maintenance record.");
    Vehicle v = lock(old);
    ensureCanBlock(v, r.getStatus());
    old.setServiceDate(r.getServiceDate());
    old.setStatus(r.getStatus());
    old.setCost(r.getCost());
    old.setOdometerReading(r.getOdometerReading());
    records.saveAndFlush(old);
    sync(v);
    return old;
  }

  public void deleteRecord(Long id) {
    MaintenanceRecord r = found(records.findById(id), "Maintenance record");
    Vehicle v = lock(r);
    if (Set.of("SCHEDULED", "IN_PROGRESS").contains(r.getStatus())) {
      r.setStatus("CANCELLED");
      records.saveAndFlush(r);
    } else {
      records.delete(r);
      records.flush();
    }
    sync(v);
  }

  private Vehicle lock(MaintenanceRecord r) {
    require(r.getVehicle() != null && r.getVehicle().getId() != null, "Select a vehicle.");
    return found(vehicles.lockById(r.getVehicle().getId()), "Vehicle");
  }

  private void validate(MaintenanceRecord r) {
    require(r.getServiceDate() != null, "Service date is required.");
    money(r.getCost(), true, "Cost");
    require(
        r.getOdometerReading() != null && r.getOdometerReading() >= 0,
        "Odometer cannot be negative.");
    require(
        r.getStatus() != null
            && Set.of("SCHEDULED", "IN_PROGRESS", "COMPLETED", "CANCELLED").contains(r.getStatus()),
        "Invalid maintenance status.");
  }

  private void ensureCanBlock(Vehicle v, String status) {
    if (Set.of("SCHEDULED", "IN_PROGRESS").contains(status)) {
      require(
          !"INACTIVE".equals(v.getStatus()), "Reactivate the vehicle before scheduling service.");
      require(
          !bookings.existsByVehicleIdAndStatusIn(
              v.getId(), List.of("PENDING", "CONFIRMED", "IN_PROGRESS")),
          "Finish or cancel open bookings before scheduling maintenance.");
    }
  }

  private void sync(Vehicle v) {
    if (!"INACTIVE".equals(v.getStatus())) {
      boolean open =
          records.existsByVehicleIdAndStatusIn(v.getId(), List.of("SCHEDULED", "IN_PROGRESS"));
      if (open) v.setStatus("MAINTENANCE");
      else if ("MAINTENANCE".equals(v.getStatus())) v.setStatus("AVAILABLE");
    }
    int max =
        records.findAll().stream()
            .filter(
                r -> r.getVehicle().getId().equals(v.getId()) && "COMPLETED".equals(r.getStatus()))
            .map(MaintenanceRecord::getOdometerReading)
            .filter(Objects::nonNull)
            .max(Integer::compareTo)
            .orElse(0);
    v.setMileage(Math.max(Optional.ofNullable(v.getMileage()).orElse(0), max));
    vehicles.save(v);
  }
}
