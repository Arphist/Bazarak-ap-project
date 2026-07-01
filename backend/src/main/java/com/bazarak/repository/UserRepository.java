package com.bazarak.repository;

import com.bazarak.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // BASIC FIND METHODS (Spring Data JPA generates these automatically)

    /**
     * Find a user by username (unique)
     * Returns Optional (empty if not found)
     */
    Optional<User> findByUsername(String username);

    /**
     * Find a user by email (unique)
     */
    Optional<User> findByEmail(String email);

    /**
     * Find a user by phone number (unique)
     */
    Optional<User> findByPhoneNumber(String phoneNumber);

    /**
     * Check if a username already exists
     */
    boolean existsByUsername(String username);

    /**
     * Check if an email already exists
     */
    boolean existsByEmail(String email);

    /**
     * Check if a phone number already exists
     */
    boolean existsByPhoneNumber(String phoneNumber);

    // CUSTOM QUERIES WITH JPQL

    /**
     * Find all active users
     */
    @Query("SELECT u FROM User u WHERE u.status = 'ACTIVE'")
    List<User> findAllActiveUsers();

    /**
     * Find all banned users
     */
    @Query("SELECT u FROM User u WHERE u.status = 'BANNED'")
    List<User> findAllBannedUsers();

    /**
     * Find all admins
     */
    @Query("SELECT u FROM User u WHERE u.role = 'ADMIN'")
    List<User> findAllAdmins();

    /**
     * Search users by username (partial match, case in-sensitive)
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> searchByUsername(@Param("searchTerm") String searchTerm);

    /**
     * Find users who haven't verified their email
     */
    @Query("SELECT u FROM User u WHERE u.emailVerified = false")
    List<User> findUnverifiedUsers();

    // NATIVE SQL QUERIES (Direct SQL)

    /**
     * Find users by role using native SQL
     */
    @Query(value = "SELECT * FROM users WHERE role = :role", nativeQuery = true)
    List<User> findUsersByRoleNative(@Param("role") String role);

}