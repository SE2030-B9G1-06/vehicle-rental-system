package com.sliit.vehiclerental.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "branches")
public class Branch {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String address;

  // Added missing contact number field
  @Column(name = "contact_number", nullable = false, length = 20)
  private String contactNumber;

  // Added missing operating hours field
  @Column(name = "operating_hours", length = 100)
  private String operatingHours;

  @Column(name = "vehicle_capacity", nullable = false)
  private Integer vehicleCapacity;

  @Column(name = "is_active")
  private Boolean isActive = true;

  // Getters and Setters
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getContactNumber() {
    return contactNumber;
  }

  public void setContactNumber(String contactNumber) {
    this.contactNumber = contactNumber;
  }

  public String getOperatingHours() {
    return operatingHours;
  }

  public void setOperatingHours(String operatingHours) {
    this.operatingHours = operatingHours;
  }

  public Integer getVehicleCapacity() {
    return vehicleCapacity;
  }

  public void setVehicleCapacity(Integer vehicleCapacity) {
    this.vehicleCapacity = vehicleCapacity;
  }

  public Boolean getIsActive() {
    return isActive;
  }

  public void setIsActive(Boolean isActive) {
    this.isActive = isActive;
  }
}
