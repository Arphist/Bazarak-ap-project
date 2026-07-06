package com.bazarak.controller;

import com.bazarak.entity.City;
import com.bazarak.service.CityService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cities")
public class CityController {

    @Autowired
    private CityService cityService;

    // Get all cities (public)
    @GetMapping
    public ResponseEntity<List<City>> getAllCities() {
        return ResponseEntity.ok(cityService.getAllCities());
    }

    // Get city by ID (public)
    @GetMapping("/{id}")
    public ResponseEntity<City> getCityById(@PathVariable Long id) {
        return ResponseEntity.ok(cityService.getCityById(id));
    }

    // Search cities (public)
    @GetMapping("/search")
    public ResponseEntity<List<City>> searchCities(@RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(cityService.searchCities(keyword));
    }

    // Get cities by province (public)
    @GetMapping("/province/{province}")
    public ResponseEntity<List<City>> getCitiesByProvince(@PathVariable String province) {
        return ResponseEntity.ok(cityService.getCitiesByProvince(province));
    }



}