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


}