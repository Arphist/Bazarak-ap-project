package com.bazarak.repository;

import com.bazarak.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for {@link City} entity.
 * Provides CRUD operations and custom queries for city management.
 */
@Repository
public interface CityRepository extends JpaRepository<City, Long> {
    /**
     * Finds a city by its name.
     *
     * @param name the name of the city
     * @return an Optional containing the found city, or empty if not found
     */
    Optional<City> findByName(String name);

    /**
     * Checks whether a city with the given name exists.
     *
     * @param name the name of the city to check
     * @return true if a city with the given name exists, false otherwise
     */
    boolean existsByName(String name);

    /**
     * Searches for cities by a keyword in their name (case-insensitive).
     * Performs a partial match using SQL LIKE operator.
     *
     * @param keyword the search keyword (case-insensitive)
     * @return a list of cities whose names contain the keyword
     */
    @Query("SELECT c FROM City c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<City> searchByName(@Param("keyword") String keyword);

    /**
     * Finds all cities in a specific province.
     *
     * @param province the name of the province
     * @return a list of cities belonging to the given province
     */
    List<City> findByProvince(String province);

    /**
     * Retrieves all cities ordered alphabetically by name.
     *
     * @return a list of all cities sorted by name in ascending order
     */
    @Query("SELECT c FROM City c ORDER BY c.name ASC")
    List<City> findAllOrderedByName();


}