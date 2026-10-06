package com.sliit.vehiclerental.backend.service;

import com.sliit.vehiclerental.backend.entity.Vehicle;
import com.sliit.vehiclerental.backend.entity.Branch;
import com.sliit.vehiclerental.backend.entity.VehicleCategory;
import com.sliit.vehiclerental.backend.repository.BranchRepository;
import com.sliit.vehiclerental.backend.repository.VehicleCategoryRepository;
import com.sliit.vehiclerental.backend.repository.VehicleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final BranchRepository branchRepository;
    private final VehicleCategoryRepository categoryRepository;

    public VehicleService(VehicleRepository vehicleRepository, BranchRepository branchRepository,
                          VehicleCategoryRepository categoryRepository) {
        this.vehicleRepository = vehicleRepository;
        this.branchRepository = branchRepository;
        this.categoryRepository = categoryRepository;
    }

    // CREATE
    public Vehicle createVehicle(Vehicle vehicle) {
        attachBranchAndCategory(vehicle);
        vehicle.setStatus("AVAILABLE");
        return vehicleRepository.save(vehicle);
    }

    // READ ALL
    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    // UPDATE
    public Vehicle updateVehicle(Long id, Vehicle updatedData) {
        Vehicle existing = vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Vehicle not found."));

        existing.setRegistrationNumber(updatedData.getRegistrationNumber());
        existing.setMake(updatedData.getMake());
        existing.setModel(updatedData.getModel());
        existing.setYear(updatedData.getYear()); // Added year here!
        existing.setDailyRate(updatedData.getDailyRate());
        attachBranchAndCategory(updatedData);
        existing.setCategory(updatedData.getCategory());
        existing.setBranch(updatedData.getBranch());
        existing.setStatus(updatedData.getStatus());

        return vehicleRepository.save(existing);
    }

    // SOFT DELETE
    public void deactivateVehicle(Long id) {
        Vehicle existing = vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Vehicle not found."));
        existing.setStatus("INACTIVE");
        vehicleRepository.save(existing);
    }

    private void attachBranchAndCategory(Vehicle vehicle) {
        if (vehicle.getBranch() == null || vehicle.getBranch().getId() == null) {
            throw new RuntimeException("A valid branch is required.");
        }
        if (vehicle.getCategory() == null || vehicle.getCategory().getId() == null) {
            throw new RuntimeException("A valid vehicle category is required.");
        }
        Branch branch = branchRepository.findById(vehicle.getBranch().getId())
                .orElseThrow(() -> new RuntimeException("Branch not found."));
        VehicleCategory category = categoryRepository.findById(vehicle.getCategory().getId())
                .orElseThrow(() -> new RuntimeException("Vehicle category not found."));
        vehicle.setBranch(branch);
        vehicle.setCategory(category);
    }
}
