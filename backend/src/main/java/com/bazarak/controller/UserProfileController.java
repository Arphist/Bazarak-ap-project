package com.bazarak.controller;

import com.bazarak.entity.User;
import com.bazarak.repository.UserRepository;
import com.bazarak.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users/me")
public class UserProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

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
        userService.isUserBanned(currentUser);

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

    // 3. CHANGE PASSWORD

    /**
     * Change the current user's password
     */
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request,
                                            HttpSession session) {
        User currentUser = userService.getCurrentUserOrThrow(session);
        userService.isUserBanned(currentUser);

        try {
            // Verify old password
            if (!currentUser.getPassword().equals(request.getOldPassword())) {
                return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Current password is incorrect");
            }

            // Update password
            currentUser.setPassword(request.getNewPassword());
            userService.updateUser(currentUser, currentUser);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Password changed successfully");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // UPDATE PROFILE PHOTO

    /**
     * Update the current user's profile photo
     */
    @PutMapping("/change-photo")
    public ResponseEntity<?> updateProfilePhoto(@RequestParam("file") MultipartFile file,
                                                HttpSession session) {
        // 1. Check if user is logged in
        User currentUser = userService.getCurrentUserOrThrow(session);
        userService.isUserBanned(currentUser);

        try {
            // 2. Validate file
            if (file.isEmpty()) {
                return buildErrorResponse(HttpStatus.BAD_REQUEST, "File is empty");
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return buildErrorResponse(HttpStatus.BAD_REQUEST, "File must be an image");
            }

            long maxSize = 5 * 1024 * 1024; // 5MB
            if (file.getSize() > maxSize) {
                return buildErrorResponse(HttpStatus.BAD_REQUEST, "File size exceeds 5MB limit");
            }

            // 3. Delete old profile photo if exists
            if (currentUser.getProfilePhoto() != null) {
                // Delete old photo file
                Path oldPhotoPath = Paths.get(currentUser.getProfilePhoto());
                if (Files.exists(oldPhotoPath)) {
                    Files.delete(oldPhotoPath);
                }
            }

            // 4. Save new photo
            String uploadDir = "profile-photos";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFileName = file.getOriginalFilename();
            String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            String uniqueFileName = "profile_" + currentUser.getId() + "_" + System.currentTimeMillis() + fileExtension;
            String filePath = uploadDir + File.separator + uniqueFileName;

            Path filePathObj = uploadPath.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), filePathObj, StandardCopyOption.REPLACE_EXISTING);

            // 5. Update user
            String photoUrl = "/uploads/profile/" + uniqueFileName; // URL path
            currentUser.setProfilePhoto(photoUrl);
            userRepository.save(currentUser);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Profile photo updated successfully");
            response.put("photoUrl", photoUrl);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload photo: " + e.getMessage());
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
        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }
    }

    public static class ChangePasswordRequest {
        private String oldPassword;
        private String newPassword;

        // Getters and Setters
        public String getOldPassword() {
            return oldPassword;
        }

        public void setOldPassword(String oldPassword) {
            this.oldPassword = oldPassword;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }

    // HELPER METHOD

    private ResponseEntity<?> buildErrorResponse(HttpStatus status, String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return ResponseEntity.status(status).body(error);
    }
}