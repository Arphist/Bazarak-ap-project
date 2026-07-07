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
@RequestMapping("users/me")
public class UserProfileController {

    @Autowired
    private UserService userService;

    // 1. GET MY PROFILE
    @GetMapping("/profile")
    public ResponseEntity<?> getMyProfile(HttpSession session){
        User currentUser = userService.getCurrentUserOrThrow(session);
        currentUser.setPassword(null);
        return ResponseEntity.ok(currentUser);
    }

    // 2. UPDATE MY PROFILE
    @PutMapping("/profile")
    public ResponseEntity<?> updateMyProfile()
    //todo: add
    //todo @PutMapping("/change-photo") -> changeProfilePhoto
}
