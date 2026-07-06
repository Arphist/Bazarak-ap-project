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

    // Create new city (admin only - TODO: add role check after JWT)
    @PostMapping
    public ResponseEntity<City> createCity(@Valid @RequestBody CityRequest request) {
        // TODO: Check if current user has ADMIN role (will be implemented with JWT)
        City city = cityService.createCity(
                request.getName(),
                request.getProvince()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(city);
    }

    // Update city (admin only - TODO: add role check after JWT)
    @PutMapping("/{id}")
    public ResponseEntity<City> updateCity(
            @PathVariable Long id,
            @Valid @RequestBody CityRequest request) {
        // TODO: Check if current user has ADMIN role (will be implemented with JWT)
        City city = cityService.updateCity(
                id,
                request.getName(),
                request.getProvince()
        );
        return ResponseEntity.ok(city);
    }

    // Delete city (admin only - TODO: add role check after JWT)
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteCity(@PathVariable Long id) {
        // TODO: Check if current user has ADMIN role (will be implemented with JWT)
        cityService.deleteCity(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "City deleted successfully");
        return ResponseEntity.ok(response);
    }

    public static class CityRequest {
        private String name;
        private String province;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getProvince() {
            return province;
        }

        public void setProvince(String province) {
            this.province = province;
        }
    }



}