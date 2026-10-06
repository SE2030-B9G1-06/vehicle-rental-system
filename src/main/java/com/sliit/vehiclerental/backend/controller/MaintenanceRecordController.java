package com.sliit.vehiclerental.backend.controller;

import com.sliit.vehiclerental.backend.entity.MaintenanceRecord;
import com.sliit.vehiclerental.backend.service.MaintenanceRecordService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/maintenance")
public class MaintenanceRecordController {

  private final MaintenanceRecordService maintenanceService;

  public MaintenanceRecordController(MaintenanceRecordService maintenanceService) {
    this.maintenanceService = maintenanceService;
  }

  @PostMapping
  public ResponseEntity<MaintenanceRecord> createRecord(@RequestBody MaintenanceRecord record) {
    return ResponseEntity.ok(maintenanceService.createRecord(record));
  }

  @GetMapping
  public ResponseEntity<List<MaintenanceRecord>> getAllRecords() {
    return ResponseEntity.ok(maintenanceService.getAllRecords());
  }

  @PutMapping("/{id}")
  public ResponseEntity<MaintenanceRecord> updateRecord(
      @PathVariable Long id, @RequestBody MaintenanceRecord record) {
    return ResponseEntity.ok(maintenanceService.updateRecord(id, record));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<String> deleteRecord(@PathVariable Long id) {
    maintenanceService.deleteRecord(id);
    return ResponseEntity.ok(
        "Service cancelled, or historical duplicate removed. Vehicle availability recalculated.");
  }
}
