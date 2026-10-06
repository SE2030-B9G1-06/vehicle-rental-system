package com.sliit.vehiclerental.backend.repository;

import com.sliit.vehiclerental.backend.entity.MaintenanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MaintenanceRecordRepository extends JpaRepository<MaintenanceRecord, Long> {
  boolean existsByVehicleIdAndStatusIn(Long vehicleId, java.util.Collection<String> statuses);
}
