package com.sliit.vehiclerental.backend.controller;

import com.sliit.vehiclerental.backend.entity.RentalContract;
import com.sliit.vehiclerental.backend.service.RentalContractService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contracts")
public class RentalContractController {

  private final RentalContractService contractService;

  public RentalContractController(RentalContractService contractService) {
    this.contractService = contractService;
  }

  @PostMapping
  public ResponseEntity<RentalContract> createContract(@RequestBody RentalContract contract) {
    return ResponseEntity.ok(contractService.createContract(contract));
  }

  @GetMapping
  public ResponseEntity<List<RentalContract>> getAllContracts() {
    return ResponseEntity.ok(contractService.getAllContracts());
  }

  @PutMapping("/{id}")
  public ResponseEntity<RentalContract> updateContract(
      @PathVariable Long id, @RequestBody RentalContract contract) {
    return ResponseEntity.ok(contractService.updateContract(id, contract));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<String> deleteContract(@PathVariable Long id) {
    contractService.deleteContract(id);
    return ResponseEntity.ok("Contract voided successfully!");
  }
}
