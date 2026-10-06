package com.sliit.vehiclerental.backend.controller;


import com.sliit.vehiclerental.backend.dto.UserManagementResponse;
import com.sliit.vehiclerental.backend.entity.User;
import com.sliit.vehiclerental.backend.service.UserManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;



@RestController
@RequestMapping("/api/users")
public class UserManagementController {



    private final UserManagementService userManagementService;



    public UserManagementController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }





    // ===============================
    // GET ALL USERS
    // Admin view users page
    // ===============================

    @GetMapping
    public ResponseEntity<?> getAllUsers() {

        try {

            List<UserManagementResponse> users =
                    userManagementService.getAllUsers();


            return ResponseEntity.ok(users);


        } catch (Exception e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());

        }

    }







    // ===============================
    // CREATE STAFF USER
    // Admin add staff account
    // ===============================

    @PostMapping("/staff")
    public ResponseEntity<?> createStaff(
            @RequestBody User user,
            @RequestParam String role
    ) {


        try {


            UserManagementResponse response =
                    userManagementService.createStaff(user, role);



            return ResponseEntity.ok(response);



        } catch (Exception e) {


            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());

        }


    }







    // ===============================
    // DELETE USER
    // Admin remove account
    // ===============================

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(
            @PathVariable Long id
    ) {


        try {


            userManagementService.deleteUser(id);


            return ResponseEntity.ok(
                    "User deleted successfully"
            );



        } catch (Exception e) {


            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());


        }


    }



}