package com.sliit.vehiclerental.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
public class Booking {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "customer_id", nullable = false)
  private User customer;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "vehicle_id", nullable = false)
  private Vehicle vehicle;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "pickup_branch_id", nullable = false)
  private Branch pickupBranch;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "return_branch_id")
  private Branch returnBranch;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "promotion_id")
  private Promotion promotion;

  @Column(name = "pickup_datetime", nullable = false)
  private LocalDateTime pickupDatetime;

  @Column(name = "return_datetime", nullable = false)
  private LocalDateTime returnDatetime;

  @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
  private BigDecimal totalAmount;

  @Column(nullable = false)
  private String status = "PENDING"; // PENDING, CONFIRMED, COMPLETED, CANCELLED

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public User getCustomer() {
    return customer;
  }

  public void setCustomer(User customer) {
    this.customer = customer;
  }

  public Vehicle getVehicle() {
    return vehicle;
  }

  public void setVehicle(Vehicle vehicle) {
    this.vehicle = vehicle;
  }

  public Branch getPickupBranch() {
    return pickupBranch;
  }

  public void setPickupBranch(Branch pickupBranch) {
    this.pickupBranch = pickupBranch;
  }

  public Branch getReturnBranch() {
    return returnBranch;
  }

  public void setReturnBranch(Branch returnBranch) {
    this.returnBranch = returnBranch;
  }

  public Promotion getPromotion() {
    return promotion;
  }

  public void setPromotion(Promotion promotion) {
    this.promotion = promotion;
  }

  public LocalDateTime getPickupDatetime() {
    return pickupDatetime;
  }

  public void setPickupDatetime(LocalDateTime pickupDatetime) {
    this.pickupDatetime = pickupDatetime;
  }

  public LocalDateTime getReturnDatetime() {
    return returnDatetime;
  }

  public void setReturnDatetime(LocalDateTime returnDatetime) {
    this.returnDatetime = returnDatetime;
  }

  public BigDecimal getTotalAmount() {
    return totalAmount;
  }

  public void setTotalAmount(BigDecimal totalAmount) {
    this.totalAmount = totalAmount;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
