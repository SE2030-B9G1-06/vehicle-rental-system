package com.sliit.vehiclerental.backend.repository;

import com.sliit.vehiclerental.backend.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    boolean existsByRegistrationNumberIgnoreCase(String registrationNumber);
}
