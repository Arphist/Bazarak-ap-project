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
    public ResponseEntity<?> getAllUsers (HttpSession session){
        // Check if actor is Admin
        userService.checkAdmin(session);

        try{
            List<User> allUsers = userService.findAllUsers();
            List<User> onlyUsers = allUsers.stream().
                    filter(u -> u.getRole()==User.Role.USER).
                    collect(Collectors.toList());

            Map<String,Object> response = new HashMap<>();
            response.put("count", onlyUsers.size());
            response.put("users",onlyUsers);

            return ResponseEntity.ok(response);
        }catch(RuntimeException e){
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    // 2. GET USER DETAILS (Admin Only)
    /**
     * Get a specific user's details (by ID)
     */
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserDetails(@PathVariable Long userId, HttpSession session){
        // Check if actor is Admin
        userService.checkAdmin(session);

        try{
            User user = userService.getUserById(userId);

            // Remove password before sending response
            user.setPassword(null);

            return ResponseEntity.ok(user);
        }catch(RuntimeException e){
            return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
    // 3. BLOCK USER (Admin Only)
    /**
     * Block a user (set status to BANNED)
     */
    @GetMapping("/{userId}/block")
    public ResponseEntity<?> blockUser(@PathVariable Long userId, HttpSession session){
        userService.checkAdmin(session);

        try{
            User blockedUser = adminService.blockUser(userId);
            blockedUser.setPassword(null);

            Map<String, Object> response = new HashMap<>();
            response.put("id", blockedUser.getId());
            response.put("username", blockedUser.getUsername());
            response.put("status", blockedUser.getStatus());
            response.put("message", "User blocked successfully");

            return ResponseEntity.ok(response);
        }catch (RuntimeException e) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
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
