package com.sliit.vehiclerental.backend.controller;

import com.sliit.vehiclerental.backend.entity.VehicleCategory;
import com.sliit.vehiclerental.backend.service.VehicleCategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class VehicleCategoryController {

    private final VehicleCategoryService categoryService;

    public VehicleCategoryController(VehicleCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<VehicleCategory> createCategory(@RequestBody VehicleCategory category) {
        return ResponseEntity.ok(categoryService.createCategory(category));
    }

    @GetMapping
    public ResponseEntity<List<VehicleCategory>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehicleCategory> updateCategory(@PathVariable Integer id, @RequestBody VehicleCategory category) {
        return ResponseEntity.ok(categoryService.updateCategory(id, category));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCategory(@PathVariable Integer id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok("Category deleted successfully!");
    }
}
