package com.bazarak.service;

import com.bazarak.entity.City;
import com.bazarak.exception.city.CityHasAdvertisementsException;
import com.bazarak.exception.city.CityNameAlreadyExistsException;
import com.bazarak.exception.city.CityNotFoundException;
import com.bazarak.repository.CityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CityService {
    @Autowired
    private CityRepository cityRepository;

    // Create new city
    public City createCity(String name, String province) {
        // Check for duplicate name
        if (cityRepository.existsByName(name)) {
            throw new CityNameAlreadyExistsException("City with name '" + name + "' already exists");
        }

        City city = new City();
        city.setName(name);
        city.setProvince(province);

        return cityRepository.save(city);
    }

    // Get all cities
    public List<City> getAllCities() {
        return cityRepository.findAllOrderedByName();
    }

    // Get city by ID
    public City getCityById(Long id) {
        return cityRepository.findById(id)
                .orElseThrow(() -> new CityNotFoundException("City not found with id: " + id));
    }

    // Get city by name
    public City getCityByName(String name) {
        return cityRepository.findByName(name)
                .orElseThrow(() -> new CityNotFoundException("City not found with name: " + name));
    }

    // Update city
    public City updateCity(Long id, String name, String province) {
        City city = getCityById(id);

        // Check if name is being changed and is unique
        if (name != null && !name.equals(city.getName())) {
            if (cityRepository.existsByName(name)) {
                throw new CityNameAlreadyExistsException("City with name '" + name + "' already exists");
            }
            city.setName(name);
        }

        if (province != null) {
            city.setProvince(province);
        }

        return cityRepository.save(city);
    }

    // Delete city
    public void deleteCity(Long id) {
        City city = getCityById(id);

        // Check if city has advertisements
        if (!city.getAdvertisements().isEmpty()) {
            throw new CityHasAdvertisementsException("Cannot delete city with advertisements. Re-assign them first.");
        }

        cityRepository.delete(city);
    }

    // Search cities
    public List<City> searchCities(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllCities();
        }
        return cityRepository.searchByName(keyword.trim());
    }

    // Get cities by province
    public List<City> getCitiesByProvince(String province) {
        if (province == null || province.trim().isEmpty()) {
            return getAllCities();
        }
        return cityRepository.findByProvince(province);
    }

}