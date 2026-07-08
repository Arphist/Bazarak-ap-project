package com.bazarak.controller;

import com.bazarak.entity.Favorite;
import com.bazarak.entity.User;
import com.bazarak.service.FavoriteService;
import com.bazarak.service.UserService;
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
}
