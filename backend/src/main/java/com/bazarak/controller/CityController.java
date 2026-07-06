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
@RequestMapping("/api/cities")
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
    public ResponseEntity<List<City>> getAllCities() {
        return ResponseEntity.ok(cityService.getAllCities());
    }

    /**
     * Retrieves a city by its ID.
     *
     * @param id the ID of the city
     * @return the city with the given ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<City> getCityById(@PathVariable Long id) {
        return ResponseEntity.ok(cityService.getCityById(id));
    }

    /**
     * Searches for cities by keyword in their name.
     *
     * @param keyword the search keyword (optional)
     * @return list of cities matching the search criteria
     */
    @GetMapping("/search")
    public ResponseEntity<List<City>> searchCities(@RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(cityService.searchCities(keyword));
    }

    /**
     * Retrieves all cities in a specific province.
     *
     * @param province the name of the province
     * @return list of cities belonging to the given province
     */
    @GetMapping("/province/{province}")
    public ResponseEntity<List<City>> getCitiesByProvince(@PathVariable String province) {
        return ResponseEntity.ok(cityService.getCitiesByProvince(province));
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