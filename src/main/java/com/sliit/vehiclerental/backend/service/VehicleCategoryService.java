package com.sliit.vehiclerental.backend.service;

import com.sliit.vehiclerental.backend.entity.VehicleCategory;
import com.sliit.vehiclerental.backend.repository.VehicleCategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VehicleCategoryService {

    private final VehicleCategoryRepository categoryRepository;

    public VehicleCategoryService(VehicleCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // CREATE
    public VehicleCategory createCategory(VehicleCategory category) {
        return categoryRepository.save(category);
    }

    // READ ALL
    public List<VehicleCategory> getAllCategories() {
        return categoryRepository.findAll();
    }

    // UPDATE
    public VehicleCategory updateCategory(Integer id, VehicleCategory updatedData) {
        VehicleCategory existing = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Category not found."));

        existing.setName(updatedData.getName());
        existing.setDescription(updatedData.getDescription());

        return categoryRepository.save(existing);
    }

    // HARD DELETE (Categories usually don't need soft deletes if no vehicles are attached)
    public void deleteCategory(Integer id) {
        categoryRepository.deleteById(id);
    }
}