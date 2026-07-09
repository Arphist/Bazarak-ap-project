package com.bazarak.controller;

import com.bazarak.entity.User;
import com.bazarak.service.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    // REGISTER ENDPOINT

    /**
     * This method is used after registration is valid, and sends the user into the system.
     * @param user the user who passed the registration
     * @return Returns a success response with HTTP status 201 CREATED and The registered user (without password)
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody User user) {
        try {
            // ️ SPRING AUTOMATICALLY:
            //    user.setUsername
            //    user.setPassword
            //    user.setFullName
            //    user.setEmail
            //    user.setPhoneNumber

            // THEN passes this User object to service

            // Call the UserService to register the user
            User registeredUser = userService.registerUser(user);
            // Remove password before sending response
            // Passwords should NEVER be sent over the network or stored in
            // client-side memory (like JavaScript or JavaFX)
            registeredUser.setPassword(null);
            // Returns a success response with HTTP status 201 CREATED and The registered user (without password)
            return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
        } catch (RuntimeException e) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST,e.getMessage());
        }
    }

    // LOGIN ENDPOINT

    /**
     * Call 'login' method from 'UserService' and find the user attempting to log in.
     * Afterward the user's fields but password.
     * @param request log-in request from frontend
     * @param session http session
     * @return the logged-in user into the system
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpSession session) {
        try {
            User user = userService.login(request.getUsername(), request.getPassword());

            // Store user in session
            userService.setCurrentUser(session, user);

            // Remove password before sending response
            // Passwords should NEVER be sent over the network or stored in
            // client-side memory (like JavaScript or JavaFX)
            user.setPassword(null);

            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("username", user.getUsername());
            response.put("fullName", user.getFullName());
            response.put("email", user.getEmail());
            response.put("phoneNumber", user.getPhoneNumber());
            response.put("role", user.getRole());
            response.put("status", user.getStatus());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return buildErrorResponse(HttpStatus.UNAUTHORIZED,e.getMessage());
        }
    }
    // LOGOUT ENDPOINT

    /**
     * Call 'logout' method from 'UserService' and return a message
     * @param session http session
     * @return reponse from the frontend
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        userService.logout(session);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logged out successfully");
        return ResponseEntity.ok(response);
    }

    // CHECK USERNAME AVAILABILITY
    // todo: I might delete this method
    /**
     * Check if the username already exists -> Real-time Availability Check
     */
    @GetMapping("/check-username/{username}")
    public ResponseEntity<?> checkUsername(@PathVariable String username) {
        boolean exists = userService.usernameExists(username);
        Map<String, Object> response = new HashMap<>();
        response.put("username", username);
        response.put("available", !exists);
        return ResponseEntity.ok(response);
    }

    // HELPER METHOD
    private ResponseEntity<?> buildErrorResponse(HttpStatus status, String message){
        Map<String, String> error = new HashMap<>();
        error.put("error",message);
        error.put("status", String.valueOf(status.value()));
        return ResponseEntity.status(status).body(error);
    }

    // INNER CLASS FOR LOGIN REQUEST

    static class LoginRequest {
        @NotBlank(message = "Username is required")
        private String username;
        @NotBlank(message = "Password is required")
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}