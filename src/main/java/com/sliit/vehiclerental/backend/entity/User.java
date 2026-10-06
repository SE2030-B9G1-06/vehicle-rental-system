package com.sliit.vehiclerental.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;


    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;


    @Column(nullable = false, unique = true, length = 100)
    private String email;


    @Column(nullable = false, length = 255)
    private String password;


    @Column(length = 20)
    private String phone;


    @Column(name = "driving_licence_number", length = 50)
    private String drivingLicenceNumber;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;


    @Column(name = "is_active")
    private Boolean isActive = true;


    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;



    // GETTERS AND SETTERS


    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


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


    public String getPhone() {
        return phone;
    }


    public void setPhone(String phone) {
        this.phone = phone;
    }


    public String getDrivingLicenceNumber() {
        return drivingLicenceNumber;
    }


    public void setDrivingLicenceNumber(String drivingLicenceNumber) {
        this.drivingLicenceNumber = drivingLicenceNumber;
    }


    public Role getRole() {
        return role;
    }


    public void setRole(Role role) {
        this.role = role;
    }


    public Boolean getIsActive() {
        return isActive;
    }


    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }


    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

}