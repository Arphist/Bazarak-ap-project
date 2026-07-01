package com.bazarak.service;

import com.bazarak.entity.User;
import com.bazarak.exception.user.*;
import com.bazarak.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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

    // USERNAME EXISTS

    /**
     * Check if the username exists in database
     * @param username entered username
     * @return true if the username exists, false if not.
     */
    public boolean usernameExists (String username){
        return userRepository.existsByUsername(username);
    }
    // TODO: rest of the methods (findById, updateUser, etc.) with proper exceptions
}