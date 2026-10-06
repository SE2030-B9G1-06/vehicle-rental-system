package com.sliit.vehiclerental.backend.controller;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {

  @GetMapping("/health")
  public ResponseEntity<Map<String, String>> checkHealth() {
    Map<String, String> response = new HashMap<>();
    response.put("status", "UP");
    response.put("message", "Vehicle Rental System Backend is running and connected to MySQL!");
    return ResponseEntity.ok(response);
  }
}
