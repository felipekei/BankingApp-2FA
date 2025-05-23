package com.example.BankingAppFB.service;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.example.BankingAppFB.repository.UserRepository;
import java.util.Collections;

/**
 * Custom User Details Service
 * Implements Spring Security's UserDetailsService to provide user authentication
 * Loads user-specific data and creates a UserDetails object that Spring Security
 * can use for authentication and validation.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    /** Repository for accessing user data */
    private final UserRepository userRepository;

    /**
     * Constructor injection of UserRepository
     * @param userRepository repository for user data access
     */
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads a user's details by their username
     * Required by UserDetailsService interface
     *
     * @param username the username to search for
     * @return UserDetails object containing user's security information
     * @throws UsernameNotFoundException if user is not found
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Find user in database by username
        com.example.BankingAppFB.model.User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // Create and return Spring Security User object with username for display
        return new User(
            user.getUsername(), // Use username for display
            user.getPassword(),
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
} 