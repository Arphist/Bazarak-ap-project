package com.bazarak.controller;

import com.bazarak.entity.*;
import com.bazarak.service.*;
import jakarta.servlet.http.HttpSession;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/favorites")
public class FavoriteController {
    @Autowired
    private FavoriteService favoriteService;
    @Autowired
    private UserService userService;
    @Autowired
    private AdvertisementService advertisementService;

    // 1. ADD TO FAVORITE
    /**
     * Add an ad to user's favorites
     */
    @PostMapping("/{adId}")
    public ResponseEntity<?> addFavorite(@PathVariable Long adId, HttpSession session){
        Advertisement ad = advertisementService.findById(adId);
        User user = userService.getCurrentUserOrThrow(session);
        userService.isUserBanned(user);

        // CHECK METHODS
        advertisementService.checkAdPending(ad);
        advertisementService.checkAdRejected(ad);
        advertisementService.checkAdDeleted(ad);
        try {
            Favorite favorite = favoriteService.addToFavorite(user,ad);

            Map<String,Object> response = new HashMap<>();
            response.put("id",favorite.getId());
            response.put("adId",ad.getId());
            response.put("ad_title",ad.getTitle());
            response.put("message","Ad added to favorites successfully");

            return ResponseEntity.ok(response);
        }catch(RuntimeException e){
            return buildErrorResponse(HttpStatus.BAD_REQUEST,e.getMessage());
        }
    }

    // 2. REMOVE FROM FAVORITES
    /**
     * Remove an ad from user's favorites
     */
    @DeleteMapping("/{adId}")
    public ResponseEntity<?> removeFavorite(@PathVariable Long adId, HttpSession session){
        Advertisement ad = advertisementService.findById(adId);
        User user = userService.getCurrentUserOrThrow(session);
        userService.isUserBanned(user);
        try{
            favoriteService.removeFromFavorite(user,ad);

            Map<String, Object> response = new HashMap<>();
            response.put("adId", ad.getId());
            response.put("ad_title", ad.getTitle());
            response.put("message", "Ad removed from your favorites successfully");

            return ResponseEntity.ok(response);
        }catch(RuntimeException e){
            return buildErrorResponse(HttpStatus.BAD_REQUEST,e.getMessage());
        }
    }

    // 3. GET MY FAVORITES
    /**
     * Get all favorites for the current user
     */
    @GetMapping
    public ResponseEntity<?> getMyFavorite(HttpSession session){
        User user = userService.getCurrentUserOrThrow(session);
        try{
            List<Favorite> myFavorites = favoriteService.getUserFavorites(user);

            Map<String, Object> response = new HashMap<>();
            response.put("count",myFavorites.size());
            response.put("favorites",myFavorites);

            return ResponseEntity.ok(response);
        }catch (RuntimeException e){
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,e.getMessage());
        }
    }

    // 4. CHECK IF FAVORITED
    /**
     * Check if the current user has favorited a specific ad
     */
    @GetMapping("/check/{adId}")
    public ResponseEntity<?> isFavorited(@PathVariable Long adId, HttpSession session){
        User user = userService.getCurrentUserOrThrow(session);
        Advertisement ad = advertisementService.findById(adId);
        try{
            boolean isFavorited = favoriteService.isFavorited(user,ad);
            Map<String,Object> response = new HashMap<>();
            response.put("adId",ad.getId());
            response.put("ad_title",ad.getTitle());
            response.put("favorited",isFavorited);

            return ResponseEntity.ok(response);
        }catch (RuntimeException e){
            return buildErrorResponse(HttpStatus.BAD_REQUEST,e.getMessage());
        }
    }

    // 5. GET FAVORITE COUNT FOR AD
    /**
     * Get the number of favorites for a specific ad
     */
    @GetMapping("/count/{adId}")
    public ResponseEntity<?> getFavoriteCount (@PathVariable Long adId){
        try{
            Advertisement ad = advertisementService.findById(adId);
            long count = favoriteService.getFavoriteCount(ad);

            Map<String, Object> response = new HashMap<>();
            response.put("adId",ad.getId());
            response.put("ad_title",ad.getTitle());
            response.put("favoriteCount", count);

            return ResponseEntity.ok(response);
        }catch (RuntimeException e){
            return buildErrorResponse(HttpStatus.NOT_FOUND,e.getMessage());
        }
    }

    // HELPER METHOD
    private ResponseEntity<?> buildErrorResponse(HttpStatus status,String message){
        Map<String,String> error = new HashMap<>();
        error.put("error",message);
        error.put("status",String.valueOf(status.value()));
        return ResponseEntity.status(status).body(error);
    }
}
