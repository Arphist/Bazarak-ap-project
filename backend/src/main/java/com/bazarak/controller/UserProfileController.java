package com.bazarak.controller;

import com.bazarak.entity.User;
import com.bazarak.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users/me")
public class UserProfileController {

    @Autowired
    private UserService userService;

    // 1. GET MY PROFILE
    /**
     * Get the current user's profile
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getMyProfile(HttpSession session) {
        User currentUser = userService.getCurrentUserOrThrow(session);
        currentUser.setPassword(null);
        return ResponseEntity.ok(currentUser);
    }

    // 2. UPDATE MY PROFILE
    /**
     * Update the current user's profile
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateMyProfile(@Valid @RequestBody UpdateProfileRequest request,
                                             HttpSession session) {
        User currentUser = userService.getCurrentUserOrThrow(session);

        try {
            // Create update object
            User updatedUser = new User();
            updatedUser.setFullName(request.getFullName());
            updatedUser.setEmail(request.getEmail());
            updatedUser.setPhoneNumber(request.getPhoneNumber());

            User savedUser = userService.updateUser(currentUser, updatedUser);
            savedUser.setPassword(null);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Profile updated successfully");
            response.put("user", savedUser);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }


    // INNER CLASSES (DTOs)

    public static class UpdateProfileRequest {
        private String fullName;
        private String email;
        private String phoneNumber;

        // Getters and Setters
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    }


    // HELPER METHOD

    private ResponseEntity<?> buildErrorResponse(HttpStatus status, String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return ResponseEntity.status(status).body(error);
    }
}