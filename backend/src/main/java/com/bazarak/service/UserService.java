package com.bazarak.service;

import com.bazarak.entity.User;
import com.bazarak.exception.user.*;
import com.bazarak.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // REGISTER

    public User registerUser(User user) {
        // THROW CUSTOM EXCEPTIONS if the request is invalid:
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new UsernameAlreadyExistsException("Username '" + user.getUsername() + "' already exists");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new EmailAlreadyExistsException("Email '" + user.getEmail() + "' already registered");
        }

        if (userRepository.existsByPhoneNumber(user.getPhoneNumber())) {
            throw new PhoneAlreadyExistsException("Phone number already registered");
        }
        // The other fields are set by this method automatically:
        // Spring maps from JSON → @RequestBody

        // Set defaults
        user.setRole(User.Role.USER);
        user.setStatus(User.UserStatus.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    // LOGIN

    public User login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));

        if (user.getStatus() == User.UserStatus.BANNED) {
            throw new UserBlockedException("Your account has been blocked. Please contact support.");
        }

        if (!user.getPassword().equals(password)) {
            throw new InvalidCredentialsException("Invalid password");
        }

        return userRepository.save(user);
    }

    // ============================================
    // CHECK METHODS
    // ============================================
    /**
     * Check if the username exists in database
     * @param username entered username
     * @return true if the username exists, false if not.
     */
     public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean phoneExists(String phone) {
        return userRepository.existsByPhoneNumber(phone);
    }



    // FIND METHODS

    /**
     * Find user by ID (returns Optional)
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Find user by ID (throws exception if not found)
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    /**
     * Find user by username
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Find user by email
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Get all users
     */
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Get all active users
     */
    public List<User> findAllActiveUsers() {
        return userRepository.findAllActiveUsers();
    }

    // UPDATE

    public User updateUser(Long id, User updatedUser) {
        User existingUser = getUserById(id);

        if (updatedUser.getFullName() != null) {
            existingUser.setFullName(updatedUser.getFullName());
        }
        if (updatedUser.getEmail() != null) {
            if (userRepository.existsByEmail(updatedUser.getEmail()) &&
                    !userRepository.findByEmail(updatedUser.getEmail()).get().getId().equals(id)) {
                throw new RuntimeException("Email already used by another user");
            }
            existingUser.setEmail(updatedUser.getEmail());
        }
        if (updatedUser.getPhoneNumber() != null) {
            if (userRepository.existsByPhoneNumber(updatedUser.getPhoneNumber()) &&
                    !userRepository.findByPhoneNumber(updatedUser.getPhoneNumber()).get().getId().equals(id)) {
                throw new RuntimeException("Phone number already used by another user");
            }
            existingUser.setPhoneNumber(updatedUser.getPhoneNumber());
        }

        return userRepository.save(existingUser);
    }

    // ADMIN OPERATIONS

    public User blockUser(Long id) {
        User user = getUserById(id);
        user.setStatus(User.UserStatus.BANNED);
        return userRepository.save(user);
    }

    public User unblockUser(Long id) {
        User user = getUserById(id);
        user.setStatus(User.UserStatus.ACTIVE);
        return userRepository.save(user);
    }

    public User makeAdmin(Long id) {
        User user = getUserById(id);
        user.setRole(User.Role.ADMIN);
        return userRepository.save(user);
    }

    // DELETE

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}