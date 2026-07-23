package com.bazarak.service;

import com.bazarak.entity.Advertisement;
import com.bazarak.entity.User;
import com.bazarak.exception.auth.*;
import com.bazarak.exception.user.*;
import com.bazarak.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
import jakarta.servlet.http.HttpSession;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private static final String USER_SESSION_KEY = "loggedInUser";

    // REGISTER

    /**
     * Register the user by checking their username, password, email, phone-number, and see if they already exist.
     * @param user user attempting to make an account
     * @return the saved-registered user
     */
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

    /**
     * Find user by username and throw exceptions if the user condition has flaw.
     * @param username the entered username
     * @param password the entered password
     * @return the saved-logged in user in the repository
     */
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

    // LOGOUT

    /**
     * Invalidate the user session so every request that they make is invalid
     * @param session the http sessions
     */
    public void logout(HttpSession session) {
        session.invalidate();
    }


    // CHECK METHODS
    /**
     * Check if the username exists in database
     * @param username entered username
     * @return true if the username exists, false if not.
     */
     public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Check if the email exists in database
     * @param email entered email
     * @return true if the email exists, false if not.
     */
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Check if the phone number exists in database
     * @param phone entered phone number
     * @return true if the phone number exists, false if not.
     */
    public boolean phoneExists(String phone) {
        return userRepository.existsByPhoneNumber(phone);
    }


    // FIND METHODS

    /**
     * Find All Banned users
     */
    public List<User> findAllBlockedUsers (){return userRepository.findAllBannedUsers();}

    /**
     * Find All users who are not admin
     */
    public List<User> findAllUsersButAdmins (){return userRepository.findAllUsersButAdmins();}

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

    /**
     *  Update user's fields but the ID
     * @param updatedUser updated-user with new fields
     * @return updated-user
     */
    public User updateUser( User user, User updatedUser) {
        User existingUser = getUserById(user.getId());
        if (updatedUser.getFullName() != null) {
            existingUser.setFullName(updatedUser.getFullName());
        }
        if (updatedUser.getEmail() != null) {
            if (userRepository.existsByEmail(updatedUser.getEmail()) &&
                    !userRepository.findByEmail(updatedUser.getEmail()).get().getId().equals(existingUser.getId())) {
                throw new EmailIsAlreadyUsed("Email already used by another user");
            }
            existingUser.setEmail(updatedUser.getEmail());
        }
        if (updatedUser.getPhoneNumber() != null) {
            if (userRepository.existsByPhoneNumber(updatedUser.getPhoneNumber()) &&
                    !userRepository.findByPhoneNumber(updatedUser.getPhoneNumber()).get().getId().equals(existingUser.getId())) {
                throw new PhoneIsAlreadyUsed("Phone number already used by another user");
            }
            existingUser.setPhoneNumber(updatedUser.getPhoneNumber());
        }

        return userRepository.save(existingUser);
    }

    // GETTER & SETTER

    public User getCurrentUserOrThrow(HttpSession session) {
        User currentUser = (User) session.getAttribute(USER_SESSION_KEY);
        if(currentUser==null) throw new UnauthorizedAccessException("Please log in to the application");
        return currentUser;
    }


    /***
     * Check if user is banned
     */
    public void isUserBanned (User user){
        if (user.isBanned()) throw new UnauthorizedAccessException("Operation failed, Your account is banned");
    }

    /**
     * Check if the User owns the Ad
     */
    public void checkOwnership(User user, Advertisement ad) {
        if (!user.getId().equals(ad.getOwner().getId())) {
            throw new UnauthorizedAccessException("You don't have permission to access this resource");
        }
    }

    /**
     * Check if the user is Admin
     * @param session http session
     */
    public void checkAdmin(HttpSession session) {
        User currentUser = getCurrentUserOrThrow(session);
        if (!currentUser.isAdmin()) {
            throw new UnauthorizedAccessException("Admin access required");
        }
    }

    public void setCurrentUser(HttpSession session, User user) {
        session.setAttribute(USER_SESSION_KEY, user);
    }
}