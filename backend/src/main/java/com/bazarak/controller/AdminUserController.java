package com.bazarak.controller;

import com.bazarak.entity.*;
import com.bazarak.service.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
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
            Map<String, String> error = new HashMap<>();
            error.put("error",e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

}
