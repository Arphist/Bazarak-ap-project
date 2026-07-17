package com.bazarak.controller;

import com.bazarak.entity.City;
import com.bazarak.service.CityService;
import com.bazarak.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cities")
public class CityController {

    @Autowired
    private CityService cityService;

    @Autowired
    private UserService userService;

    /**
     * Retrieves all cities.
     *
     * @return list of all cities
     */
    @GetMapping
    public ResponseEntity<?> getAllCities() {
        try {
            List<City> cities = cityService.getAllCities();

            Map<String, Object> response = new HashMap<>();
            response.put("count", cities.size());
            response.put("cities", cities);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * Retrieves a city by its ID.
     *
     * @param id the ID of the city
     * @return the city with the given ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getCityById(@PathVariable Long id) {
        try {
            City city = cityService.getCityById(id);
            return ResponseEntity.ok(city);
        } catch (RuntimeException e) {
            return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }


    /**
     * Searches for cities by keyword in their name.
     *
     * @param keyword the search keyword (optional)
     * @return list of cities matching the search criteria
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchCities(@RequestParam String keyword) {
        try {
            List<City> cities = cityService.searchCities(keyword);

            Map<String, Object> response = new HashMap<>();
            response.put("count", cities.size());
            response.put("cities", cities);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Retrieves all cities in a specific province.
     *
     * @param province the name of the province
     * @return list of cities belonging to the given province
     */
    @GetMapping("/province/{province}")
    public ResponseEntity<?> getCitiesByProvince(@PathVariable String province) {
        try {
            List<City> cities = cityService.getCitiesByProvince(province);

            Map<String, Object> response = new HashMap<>();
            response.put("count", cities.size());
            response.put("cities", cities);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Creates a new city. Admin only.
     *
     * @param request the city creation request DTO
     * @param session the HTTP session for admin validation
     * @return the created city
     */
    @PostMapping
    public ResponseEntity<City> createCity(
            @Valid @RequestBody CityRequest request,
            HttpSession session) {

        userService.checkAdmin(session);

        City city = cityService.createCity(
                request.getName(),
                request.getProvince()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(city);
    }

    // HELPER METHOD

    private ResponseEntity<?> buildErrorResponse(HttpStatus status, String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        error.put("status", String.valueOf(status.value()));
        return ResponseEntity.status(status).body(error);
    }

    /**
     * Updates an existing city. Admin only.
     *
     * @param id      the ID of the city to update
     * @param request the city update request DTO
     * @param session the HTTP session for admin validation
     * @return the updated city
     */
    @PutMapping("/{id}")
    public ResponseEntity<City> updateCity(
            @PathVariable Long id,
            @Valid @RequestBody CityRequest request,
            HttpSession session) {

        userService.checkAdmin(session);

        City city = cityService.updateCity(
                id,
                request.getName(),
                request.getProvince()
        );
        return ResponseEntity.ok(city);
    }

    /**
     * Deletes a city by its ID. Admin only.
     *
     * @param id      the ID of the city to delete
     * @param session the HTTP session for admin validation
     * @return a success message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteCity(
            @PathVariable Long id,
            HttpSession session) {

        userService.checkAdmin(session);

        cityService.deleteCity(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "City deleted successfully");
        return ResponseEntity.ok(response);
    }


    /**
     * DTO for city creation and update requests.
     */
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