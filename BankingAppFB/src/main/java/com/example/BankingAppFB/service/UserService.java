package com.example.BankingAppFB.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.BankingAppFB.model.User;
import com.example.BankingAppFB.repository.UserRepository;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
// import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;

/**
 * User Service
 * Handles business logic for user-related operations including:
 * - User registration and validation
 * - Password encryption
 * - Database operations through UserRepository
 * - Logging of user-related activities
 * - Generating secret key unique to each user to link their Google Authenticator
 * - Generates QR code to link app with authenticator
 */

@Service
public class UserService {

    /** Logger for tracking service operations */
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    /** Repository for user data persistence operations */
    @Autowired
    private UserRepository userRepository;

    /** Encoder for secure password hashing */
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Registers a new user in the system
     * This method:
     * 1. Validates that email and username are unique
     * 2. Encrypts the user's password
     * 3. Saves the user to the database
     * 4. Logs the registration process
     *
     * @param user the User object containing registration data
     * @return the saved User object with encrypted password
     * @throws RuntimeException if email is already registered or username is taken
     */
    public User registerUser(User user) {
        logger.info("Attempting to register user: {}", user.getUsername());

        // Validate email uniqueness
        if (userRepository.existsByEmail(user.getEmail())) {
            logger.error("Registration failed: Email {} already exists", user.getEmail());
            throw new RuntimeException("Email already registered");
        }

        // Validate username uniqueness
        if (userRepository.existsByUsername(user.getUsername())) {
            logger.error("Registration failed: Username {} already exists", user.getUsername());
            throw new RuntimeException("Username already taken");
        }

        // Encrypt password and save user
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Check if user already exists to avoid regenerating a new secret key
        Optional<User> existingUser = userRepository.findByUsername(user.getUsername());
        if (existingUser.isPresent() && existingUser.get().getSecretKey() != null) {
            logger.info("User already exists, reusing stored secret key.");
            user.setSecretKey(existingUser.get().getSecretKey());
        } else {
            // Generate 2FA credentials only for new users
            GoogleAuthenticator gAuth = new GoogleAuthenticator();
            GoogleAuthenticatorKey key = gAuth.createCredentials();
            user.setSecretKey(key.getKey());
        }
        
        // Save user with either the new or existing secret key
        User savedUser = userRepository.save(user);
        logger.info("User registered successfully: {}", savedUser.getUsername());

        // Retrieve stored user secret key to ensure consistency
        String storedSecretKey = savedUser.getSecretKey();
        String qrCodeUrl = generateQRCodeURL(savedUser.getEmail(), storedSecretKey);
        logger.info("Generated QR Code URL: {}", qrCodeUrl);

        return savedUser;
    }

    /**
     * Finds a user by their username
     * Used for retrieving user details during authentication and account operations
     *
     * @param username the username to search for
     * @return the User object if found
     * @throws RuntimeException if no user is found with the given username
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
    }

    /**
     * Boolean for the 2FA
     * Verifies the 2FA code entered by the user
     *
     * @return authorization based on match
     */
    public boolean verifyTwoFactorCode(String secretKey, int verificationCode) {
        logger.info("Verifying code: {} with secretKey: {}", verificationCode, secretKey);
        GoogleAuthenticator gAuth = new GoogleAuthenticator();
        boolean isValid = gAuth.authorize(secretKey, verificationCode);
        logger.info("Verification result: {}", isValid);
        return isValid;
   }

    /**
     * This method creates a URL that the QR code can represent.
     * 
     * @param email email address
     * @param secretKey secret key of each user on the database
     * @return URL
     */
    public String generateQRCodeURL(String email, String secretKey) {
        if (secretKey == null || secretKey.isEmpty()) {
            logger.error("Secret key is null or empty for email: {}", email);
            throw new RuntimeException("Secret key not found. Please try again.");
        }
        String issuer = "BankingApp";
        String otpauthURL = String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s",
                issuer, email, secretKey, issuer);
        // Return the QR code image URL using the API
    return "https://api.qrserver.com/v1/create-qr-code/?data=" + otpauthURL + "&size=200x200&ecc=M";

    }
} 
