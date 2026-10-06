package com.sliit.vehiclerental.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "maintenance_records")
public class MaintenanceRecord {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "vehicle_id", nullable = false)
  private Vehicle vehicle;

  @Column(name = "service_date")
  private LocalDate serviceDate;

  @Column(precision = 10, scale = 2)
  private BigDecimal cost;

  @Column(length = 20)
  private String status; // e.g., SCHEDULED, IN_PROGRESS, COMPLETED

  // ADDED MISSING FIELD HERE
  @Column(name = "odometer_reading")
  private Integer odometerReading;

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Vehicle getVehicle() {
    return vehicle;
  }

  public void setVehicle(Vehicle vehicle) {
    this.vehicle = vehicle;
  }

  public LocalDate getServiceDate() {
    return serviceDate;
  }

  public void setServiceDate(LocalDate serviceDate) {
    this.serviceDate = serviceDate;
  }

  public BigDecimal getCost() {
    return cost;
  }

  public void setCost(BigDecimal cost) {
    this.cost = cost;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Integer getOdometerReading() {
    return odometerReading;
  }

  public void setOdometerReading(Integer odometerReading) {
    this.odometerReading = odometerReading;
  }
}
