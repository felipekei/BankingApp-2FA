package com.example.BankingAppFB.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import com.example.BankingAppFB.model.User;
import com.example.BankingAppFB.service.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Authentication Controller
 * Handles all authentication-related requests including login and registration.
 * This controller manages user authentication flows and form submissions.
 */
@Controller
public class AuthController {

    /** Logger for this class */
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    /** Service for handling user-related operations */
    @Autowired
    private UserService userService;

    /**
     * Displays the login page
     * Spring Security handles the actual login processing
     * 
     * @return the login view template name
     */
    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    /**
     * Displays the registration page
     * 
     * @param model Spring MVC model to pass data to the view
     * @return the register view template name
     */
    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    /**
     * Processes user registration
     * Handles the registration form submission, validates user input,
     * and creates a new user account if validation passes
     * sends the user to setup 2FA
     * 
     * @param user   the User object populated from form data
     * @param result binding result for validation errors
     * @param model  Spring MVC model to pass data to the view
     * @return redirect to login page on success, or back to register page on error
     */
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user,
                               BindingResult result,
                               Model model) {
        logger.info("Received registration request for user: {}", user.getUsername());
    
        if (result.hasErrors()) {
            logger.error("Validation errors: {}", result.getAllErrors());
            return "register";
        }
    
        try {
            // Register the user and get the QR code URL
            User registeredUser = userService.registerUser(user);
            String qrCodeUrl = userService.generateQRCodeURL(registeredUser.getEmail(), registeredUser.getSecretKey()); // Retrieve stored key

            logger.info("QR Code generated for user: {}", qrCodeUrl);

            // Pass data to the template
            model.addAttribute("qrCodeUrl", qrCodeUrl);
            model.addAttribute("secretKey", registeredUser.getSecretKey()); // Add secret key for manual entry
            model.addAttribute("username", registeredUser.getUsername());

            return "setup-2fa"; // Redirect to the 2FA setup page

        } catch (RuntimeException e) {
            logger.error("Registration failed: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }
    

    @PostMapping("/verify-2fa")
    public String verifyTwoFactorCode(@RequestParam("code") String code,
            @RequestParam("username") String username,
            @RequestParam("qrCodeUrl") String qrCodeUrl,
            Model model) {
        logger.info("Received 2FA verification request for username: {}", username);

        try {
            // Validate the code format
            if (!code.matches("\\d{6}")) {
                logger.error("Invalid code format: {}", code);
                throw new IllegalArgumentException("The verification code must be a 6-digit number.");
            }

            int verificationCode = Integer.parseInt(code); // Safely parse the input

            // Retrieve the user from the database
            User user = userService.findByUsername(username);
            String secretKey = user.getSecretKey();

            // Verify the code
            boolean isCodeValid = userService.verifyTwoFactorCode(secretKey, verificationCode);

            if (isCodeValid) {
                logger.info("2FA verification successful for user: {}", user.getUsername());

                // Redirect to login
                return "redirect:/login";
            } else {
                logger.error("Invalid 2FA code for user: {}", user.getUsername());

                // Pass the existing QR code URL and error message to the model
                model.addAttribute("qrCodeUrl", qrCodeUrl);
                model.addAttribute("secretKey", user.getSecretKey());
                model.addAttribute("username", user.getUsername());
                model.addAttribute("error", "Invalid verification code. Please try again.");
                return "setup-2fa";
            }
        } catch (IllegalArgumentException e) {
            logger.error("Error during 2FA verification: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("qrCodeUrl", qrCodeUrl); // Reuse the QR code on errors
            return "setup-2fa";
        } catch (RuntimeException e) {
            logger.error("Unexpected error during 2FA verification: {}", e.getMessage());
            model.addAttribute("error", "An unexpected error occurred. Please try again.");
            model.addAttribute("qrCodeUrl", qrCodeUrl); // Reuse the QR code on errors
            return "setup-2fa";
        }
    }
}