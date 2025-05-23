package com.example.BankingAppFB.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller responsible for handling navigation and page rendering
 * This controller manages the main pages of the application including
 * home, map, and about pages. It ensures users are authenticated before
 * accessing any page and provides user information to the views.
 */
@Controller
public class HomeController {

    /**
     * Handles the root URL ("/") request
     * Redirects users to the home page
     */
    @GetMapping("/")
    public String index() {
        return "redirect:/account/dashboard";
    }

    /**
     * Handles requests to the home page
     * @param authentication Spring Security authentication object containing user details
     * @param model Spring MVC model for passing data to the view
     * @return home view if authenticated, login redirect if not
     */
    @GetMapping("/home")
    public String home(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("username", authentication.getName());
            return "home";
        }
        return "redirect:/login";
    }

    /**
     * Handles requests to the map page
     * @param authentication Spring Security authentication object containing user details
     * @param model Spring MVC model for passing data to the view
     * @return map view if authenticated, login redirect if not
     */
    @GetMapping("/map")
    public String map(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("username", authentication.getName());
            return "map";
        }
        return "redirect:/login";
    }

    /**
     * Handles requests to the about page
     * @param authentication Spring Security authentication object containing user details
     * @param model Spring MVC model for passing data to the view
     * @return about view if authenticated, login redirect if not
     */
    @GetMapping("/about")
    public String about(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("username", authentication.getName());
            return "about";
        }
        return "redirect:/login";
    }
} 