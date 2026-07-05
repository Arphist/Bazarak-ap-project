package com.bazarak.service;

import com.bazarak.entity.User;
import com.bazarak.exception.user.*;
import com.bazarak.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminService {
    @Autowired
    private UserRepository userRepository;

    // ADMIN OPERATIONS

    /**
     * Block a user by their ID
     * @param id the User ID
     * @return blocked user
     */
    public User blockUser(Long id) {
        User user = getUserById(id);
        user.setStatus(User.UserStatus.BANNED);
        return userRepository.save(user);
    }

    /**
     * Unblocked a user by their ID
     * @param id the User ID
     * @return unblocked user
     */
    public User unblockUser(Long id) {
        User user = getUserById(id);
        user.setStatus(User.UserStatus.ACTIVE);
        return userRepository.save(user);
    }

    /**
     * Make a user an admin by their ID
     * @param id the User ID
     * @return the user whom turned into admin
     */
    public User makeAdmin(Long id) {
        User user = getUserById(id);
        user.setRole(User.Role.ADMIN);
        return userRepository.save(user);
    }

    // DELETE

    /**
     * Delete a user by their ID
     * @param id the User ID
     */
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    /**
     * Find user by ID (throws exception if not found)
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }
}
