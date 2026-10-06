package com.sliit.vehiclerental.backend.service;

import com.sliit.vehiclerental.backend.dto.RegisterRequest;
import com.sliit.vehiclerental.backend.dto.AuthResponse;
import com.sliit.vehiclerental.backend.dto.LoginRequest;
import com.sliit.vehiclerental.backend.entity.Role;
import com.sliit.vehiclerental.backend.entity.User;
import com.sliit.vehiclerental.backend.repository.RoleRepository;
import com.sliit.vehiclerental.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse registerCustomer(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail().trim())) {
            throw new RuntimeException("Error: Email is already in use!");
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPhone(request.getContactNumber());
        user.setDrivingLicenceNumber(request.getDrivingLicenceNumber());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        Role customerRole = roleRepository.findByName("ROLE_CUSTOMER")
                .orElseThrow(() -> new RuntimeException("Error: Role ROLE_CUSTOMER is not found in database."));

        user.setRole(customerRole);
        userRepository.save(user);

        return toResponse("User registered successfully!", user);
    }

    public AuthResponse loginCustomer(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.getEmail().trim())
                .orElseThrow(() -> new RuntimeException("Error: Invalid email or password."));

        if (!Boolean.TRUE.equals(user.getIsActive()) || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Error: Invalid email or password.");
        }

        return toResponse("Login successful!", user);
    }

    public AuthResponse getUserResponse(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Error: User account was not found."));
        return toResponse("Authenticated", user);
    }

    private AuthResponse toResponse(String message, User user) {
        return new AuthResponse(message, user.getId(), user.getRole().getName(),
                user.getFirstName(), user.getLastName(), user.getEmail());
    }
}
