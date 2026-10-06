package com.sliit.vehiclerental.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "rental_contracts")
public class RentalContract {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne
  @JoinColumn(name = "booking_id", nullable = false)
  private Booking booking;

  @Column(name = "driver_name", length = 100)
  private String driverName;

  @Column(name = "license_number", length = 50)
  private String licenseNumber;

  @Column(name = "identity_document", length = 50)
  private String identityDocument;

  // ADDED MISSING FIELD HERE
  @Column(name = "contact_details", length = 50)
  private String contactDetails;

  @Column(name = "pickup_timestamp")
  private LocalDateTime pickupTimestamp;

  @Column(name = "return_timestamp")
  private LocalDateTime returnTimestamp;

  @Column(name = "signature")
  private String signature;

  @Column(name = "status")
  private String status;

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Booking getBooking() {
    return booking;
  }

  public void setBooking(Booking booking) {
    this.booking = booking;
  }

  public String getDriverName() {
    return driverName;
  }

  public void setDriverName(String driverName) {
    this.driverName = driverName;
  }

  public String getLicenseNumber() {
    return licenseNumber;
  }

  public void setLicenseNumber(String licenseNumber) {
    this.licenseNumber = licenseNumber;
  }

  public String getIdentityDocument() {
    return identityDocument;
  }

  public void setIdentityDocument(String identityDocument) {
    this.identityDocument = identityDocument;
  }

  public String getContactDetails() {
    return contactDetails;
  }

  public void setContactDetails(String contactDetails) {
    this.contactDetails = contactDetails;
  }

  public LocalDateTime getPickupTimestamp() {
    return pickupTimestamp;
  }

  public void setPickupTimestamp(LocalDateTime pickupTimestamp) {
    this.pickupTimestamp = pickupTimestamp;
  }

  public LocalDateTime getReturnTimestamp() {
    return returnTimestamp;
  }

  public void setReturnTimestamp(LocalDateTime returnTimestamp) {
    this.returnTimestamp = returnTimestamp;
  }

  public String getSignature() {
    return signature;
  }

  public void setSignature(String signature) {
    this.signature = signature;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
