package com.sliit.vehiclerental.backend.service;

import com.sliit.vehiclerental.backend.dto.UserManagementResponse;
import com.sliit.vehiclerental.backend.entity.Role;
import com.sliit.vehiclerental.backend.entity.User;
import com.sliit.vehiclerental.backend.repository.RoleRepository;
import com.sliit.vehiclerental.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class UserManagementService {


    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;


    public UserManagementService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder
    ) {

        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;

    }



    // ===============================
    // GET ACTIVE USERS ONLY
    // ===============================

    public List<UserManagementResponse> getAllUsers() {


        return userRepository.findByIsActive(true)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

    }





    // ===============================
    // ADMIN CREATE STAFF USER
    // ===============================

    public UserManagementResponse createStaff(User userRequest, String roleName) {


        if(userRequest.getEmail() == null ||
                userRequest.getEmail().isBlank()) {

            throw new RuntimeException("Email is required");

        }



        if(userRepository.existsByEmailIgnoreCase(userRequest.getEmail())) {

            throw new RuntimeException("Email already exists");

        }




        Role role = roleRepository.findByName(roleName)

                .orElseThrow(() ->
                        new RuntimeException("Role not found")
                );



        userRequest.setRole(role);



        userRequest.setPassword(
                passwordEncoder.encode(
                        userRequest.getPassword()
                )
        );



        // New accounts active
        userRequest.setIsActive(true);



        User savedUser = userRepository.save(userRequest);



        return convertToResponse(savedUser);

    }






    // ===============================
    // DELETE USER PERMANENTLY
    // ===============================

    public void deleteUser(Long id) {


        User user = userRepository.findById(id)

                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );



        userRepository.delete(user);


    }







    private UserManagementResponse convertToResponse(User user) {


        return new UserManagementResponse(

                user.getId(),

                user.getFirstName(),

                user.getLastName(),

                user.getEmail(),

                user.getRole().getName(),

                user.getIsActive()

        );

    }



}