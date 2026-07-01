package com.bazarak.controller;

import com.bazarak.entity.User;
import com.bazarak.service.UserService;
import jakarta.validation.Valid;
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
            // Call the UserService to register the user
            User registeredUser = userService.registerUser(user);
            // Remove password before sending response
            // Passwords should NEVER be sent over the network or stored in
            // client-side memory (like JavaScript or JavaFX)
            registeredUser.setPassword(null);
            // Returns a success response with HTTP status 201 CREATED and The registered user (without password)
            return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // LOGIN ENDPOINT

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            User user = userService.login(loginRequest.getUsername(), loginRequest.getPassword());
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
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    // CHECK USERNAME AVAILABILITY

    @GetMapping("/check-username/{username}")
    public ResponseEntity<?> checkUsername(@PathVariable String username) {
        boolean exists = userService.usernameExists(username);
        Map<String, Object> response = new HashMap<>();
        response.put("username", username);
        response.put("available", !exists);
        return ResponseEntity.ok(response);
    }

    // INNER CLASS FOR LOGIN REQUEST

    static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}