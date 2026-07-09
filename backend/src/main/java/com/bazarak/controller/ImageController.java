package com.bazarak.controller;

import com.bazarak.entity.Advertisement;
import com.bazarak.entity.Image;
import com.bazarak.entity.User;
import com.bazarak.service.AdvertisementService;
import com.bazarak.service.ImageService;
import com.bazarak.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/images")
public class ImageController {
    @Autowired
    private ImageService imageService;
    @Autowired
    private UserService userService;
    @Autowired
    private AdvertisementService advertisementService;

    // UPLOAD IMAGE

    /**
     * Upload an image for an ad
     */
    @PostMapping("/upload/{adId}")
    public ResponseEntity<?> uploadImage(@PathVariable Long adId, @RequestParam("file") MultipartFile file,
                                         @RequestParam(required = false, defaultValue = "false") boolean isPrimary, HttpSession session) {
        // Check if user is logged in
        User currentUser = userService.getCurrentUserOrThrow(session);

        try {
            Advertisement ad = advertisementService.findById(adId);
            userService.checkOwnership(currentUser, ad);

            Image image = imageService.uploadImage(file, adId, isPrimary);

            Map<String, Object> response = new HashMap<>();
            response.put("id", image.getId());
            response.put("file_name", image.getFileName());
            response.put("file_path", image.getFilePath());
            response.put("isPrimary", image.isPrimary());
            response.put("message", "Image uploaded successfully");

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload image: " + e.getMessage());
        } catch (RuntimeException e) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // GET IMAGES FOR AN AD

    /**
     * Get all images for an ad
     */
    @GetMapping("ad/{adId}")
    public ResponseEntity<?> getImagesByAd(@PathVariable Long adId) {
        try {
            List<Image> images = imageService.getImagesByAdId(adId);

            Map<String, Object> response = new HashMap<>();
            response.put("count", images.size());
            response.put("images", images);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    // GET SINGLE IMAGE (as file)

    /**
     * Get an image file by ID
     */
    @GetMapping("/{imageId}")
    public ResponseEntity<?> getImage(@PathVariable Long imageId) {
        try {
            Image image = imageService.getImageById(imageId);
            Path filePath = Paths.get(image.getFilePath());

            if (!Files.exists(filePath)) {
                return buildErrorResponse(HttpStatus.NOT_FOUND, "File not found");
            }

            Resource resource = new FileSystemResource(filePath.toFile());
            String contentType = Files.probeContentType(filePath);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType != null ? contentType : "image/jpeg"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + image.getFileName() + "\"")
                    .body(resource);
        } catch (IOException e) {
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (RuntimeException e) {
            return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    // DELETE IMAGE

    /**
     * Delete an image by ID
     */
    @DeleteMapping("/{imageId}")
    public ResponseEntity<?> deleteImage(@PathVariable Long imageId, HttpSession session) {
        User currentUser = userService.getCurrentUserOrThrow(session);

        try {
            Image image = imageService.getImageById(imageId);
            Advertisement ad = advertisementService.findById(image.getAdvertisement().getId());
            userService.checkOwnership(currentUser, ad);

            imageService.deleteImage(imageId);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Image deleted successfully");

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete image: " + e.getMessage());
        } catch (RuntimeException e) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // SET PRIMARY IMAGE

    /**
     * Set an image as primary for its ad
     */
    @PutMapping("/{imageId}/primary")
    public ResponseEntity<?> setPrimaryImage(@PathVariable Long imageId, HttpSession session) {
        User currentUser = userService.getCurrentUserOrThrow(session);
        try {
            Image image = imageService.getImageById(imageId);

            Map<String, Object> response = new HashMap<>();
            response.put("id", image.getId());
            response.put("file_name", image.getFileName());
            response.put("isPrimary", image.isPrimary());
            response.put("message", "Image set as primary successfully");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
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
