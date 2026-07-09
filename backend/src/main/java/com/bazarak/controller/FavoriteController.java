package com.bazarak.controller;

import com.bazarak.entity.*;
import com.bazarak.service.*;
import jakarta.servlet.http.HttpSession;
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

    @PostMapping("/{adId")
    public ResponseEntity<?> addFavorite(@PathVariable Long adId, HttpSession session){
        Advertisement ad = advertisementService.findById(adId);
        User user = userService.getCurrentUserOrThrow(session);

        try {
            Favorite favorite = favoriteService.addToFavorite(user,ad);

            Map<String,Object> response = new HashMap<>();
            response.put("id",favorite.getId());
            response.put("ad_title",ad.getTitle());
            response.put("message","Ad added to favorites successfully");

            return ResponseEntity.ok(response);
        }catch(RuntimeException e){
            return buildErrorResponse(HttpStatus.BAD_REQUEST,e.getMessage());
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
