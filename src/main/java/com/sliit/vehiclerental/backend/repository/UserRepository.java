package com.sliit.vehiclerental.backend.repository;

import com.sliit.vehiclerental.backend.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  // Login - case insensitive email search
  Optional<User> findByEmailIgnoreCase(String email);

  // Registration duplicate email check
  Boolean existsByEmailIgnoreCase(String email);

  // Admin user management - search by active status
  List<User> findByIsActive(Boolean isActive);

  // Admin user management - search users by name/email
  List<User>
      findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
          String firstName, String lastName, String email);
}
