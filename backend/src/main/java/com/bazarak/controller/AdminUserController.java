package com.bazarak.controller;

import com.bazarak.entity.User;
import com.bazarak.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/users")
public class AdminUserController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserService userService;

    // 1. GET ALL USERS (Admin Only)

    /**
     * Get all regular users (excluding admins)
     */
    @GetMapping("/all-users")
    public ResponseEntity<?> getAllUsers(HttpSession session) {
        // Check if actor is Admin
        userService.checkAdmin(session);

        try {
            List<User> onlyUsers = userService.findAllUsersButAdmins();

            Map<String, Object> response = new HashMap<>();
            response.put("count", onlyUsers.size());
            response.put("users", onlyUsers);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    // 2. GET USER DETAILS (Admin Only)

    /**
     * Get a specific user's details (by ID)
     */
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserDetails(@PathVariable Long userId, HttpSession session) {
        // Check if actor is Admin
        userService.checkAdmin(session);

        try {
            User user = userService.getUserById(userId);

            // Remove password before sending response
            user.setPassword(null);

            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
    // 3. BLOCK USER (Admin Only)

    /**
     * Block a user (set status to BANNED)
     */
    @PutMapping("/{userId}/block")
    public ResponseEntity<?> blockUser(@PathVariable Long userId, HttpSession session) {
        userService.checkAdmin(session);

        try {
            User blockedUser = adminService.blockUser(userId);
            blockedUser.setPassword(null);

            Map<String, Object> response = new HashMap<>();
            response.put("id", blockedUser.getId());
            response.put("username", blockedUser.getUsername());
            response.put("status", blockedUser.getStatus());
            response.put("message", "User blocked successfully");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // 4. UNBLOCK USER (Admin Only)

    /**
     * Unblock a user (set status to ACTIVE)
     */
    @PutMapping("/{userId}/unblock")
    public ResponseEntity<?> unblockUser(@PathVariable Long userId, HttpSession session) {
        userService.checkAdmin(session);

        try {
            User unblockedUser = adminService.unblockUser(userId);

            unblockedUser.setPassword(null);
            Map<String, Object> response = new HashMap<>();
            response.put("id", unblockedUser.getId());
            response.put("username", unblockedUser.getUsername());
            response.put("status", unblockedUser.getStatus());
            response.put("message", "User unblocked successfully");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // 5. GET BLOCKED USERS (Admin Only)

    /**
     * Get all blocked users
     */
    @GetMapping("/blocked")
    public ResponseEntity<?> getBlockedUsers(HttpSession session) {
        userService.checkAdmin(session);

        try {

            List<User> blockedUsers = userService.findAllBlockedUsers();

            Map<String, Object> response = new HashMap<>();
            response.put("count", blockedUsers.size());
            response.put("users", blockedUsers);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    // 6. GET ACTIVE USERS (Admin Only)

    /**
     * Get all active users
     */
    @GetMapping("/active")
    public ResponseEntity<?> getActiveUsers(HttpSession session) {
        userService.checkAdmin(session);
        try {
            List<User> activeUsers = userService.findAllActiveUsers();

            Map<String, Object> response = new HashMap<>();
            response.put("count", activeUsers.size());
            response.put("users", activeUsers);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    // HELPER METHOD
    private ResponseEntity<?> buildErrorResponse(HttpStatus status, String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        error.put("status", String.valueOf(status.value()));
        return ResponseEntity.status(status).body(error);
    }
}
