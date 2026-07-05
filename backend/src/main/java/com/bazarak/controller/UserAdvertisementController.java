package com.bazarak.controller;

import com.bazarak.entity.Advertisement;
import com.bazarak.entity.User;
import com.bazarak.service.AdvertisementService;
import com.bazarak.service.UserService;
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
@RequestMapping("/user/ads")
public class UserAdvertisementController {
    @Autowired
    private AdvertisementService advertisementService;
    @Autowired
    private UserService userService;

    // 1. GET ALL MY ADS
    /**
     * Get all advertisements posted by the current user
     */
    @GetMapping("/ads")
    public ResponseEntity<?> getPendingAds(HttpSession session) {
        // Check if user is logged in
        User currentUser = userService.getCurrentUserOrThrow(session);

        try {
            List<Advertisement> myAds = advertisementService.getAdsByOwner(currentUser.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("count", myAds.size());
            response.put("ads", myAds);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
