package com.sliit.vehiclerental.backend.controller;

import com.sliit.vehiclerental.backend.entity.Branch;
import com.sliit.vehiclerental.backend.service.BranchService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/branches")
public class BranchController {

  private final BranchService branchService;

  public BranchController(BranchService branchService) {
    this.branchService = branchService;
  }

  @PostMapping
  public ResponseEntity<Branch> createBranch(@RequestBody Branch branch) {
    Branch savedBranch = branchService.createBranch(branch);
    return ResponseEntity.ok(savedBranch);
  }

  @GetMapping
  public ResponseEntity<List<Branch>> getAllBranches() {
    return ResponseEntity.ok(branchService.getAllBranches());
  }

  @PutMapping("/{id}")
  public ResponseEntity<Branch> updateBranch(@PathVariable Integer id, @RequestBody Branch branch) {
    Branch updatedBranch = branchService.updateBranch(id, branch);
    return ResponseEntity.ok(updatedBranch);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<String> deactivateBranch(@PathVariable Integer id) {
    branchService.deactivateBranch(id);
    return ResponseEntity.ok("Branch deactivated successfully!");
  }
}
