package com.sliit.vehiclerental.backend.repository;

import com.sliit.vehiclerental.backend.entity.VehicleCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VehicleCategoryRepository extends JpaRepository<VehicleCategory, Integer> {
    Optional<VehicleCategory> findByNameIgnoreCase(String name);
}
