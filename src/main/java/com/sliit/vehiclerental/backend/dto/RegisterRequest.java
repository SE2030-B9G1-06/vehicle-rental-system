package com.sliit.vehiclerental.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

  @NotBlank(message = "First name is required.")
  private String firstName;

  @NotBlank(message = "Last name is required.")
  private String lastName;

  @NotBlank(message = "Email is required.")
  @Email(message = "Enter a valid email address.")
  private String email;

  @NotBlank(message = "Password is required.")
  @Size(min = 8, message = "Password must contain at least 8 characters.")
  private String password;

  // We must use these exact names to match the JavaScript JSON!
  @NotBlank(message = "Contact number is required.")
  @Pattern(regexp = "^[0-9+() -]{7,20}$", message = "Enter a valid contact number.")
  private String contactNumber;

  @NotBlank(message = "Driving licence number is required.")
  private String drivingLicenceNumber;

  private String role;

  // --- Getters and Setters ---

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getContactNumber() {
    return contactNumber;
  }

  public void setContactNumber(String contactNumber) {
    this.contactNumber = contactNumber;
  }

  public String getDrivingLicenceNumber() {
    return drivingLicenceNumber;
  }

  public void setDrivingLicenceNumber(String drivingLicenceNumber) {
    this.drivingLicenceNumber = drivingLicenceNumber;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }
}
