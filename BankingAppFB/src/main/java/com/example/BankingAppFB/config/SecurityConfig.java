package com.example.BankingAppFB.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Spring Security Configuration
 * Configures security settings for the application including:
 * - Authentication
 * - Authorization
 * - Password encoding
 * - Login/logout behavior
 * - URL access rules
 * 
 * Recent Changes:
 * 1. Added explicit permission for /account/** endpoints
 *    - This allows authenticated users to access all account-related URLs
 *    - Includes dashboard, add-money, and withdraw-money endpoints
 * 
 * 2. Updated default success URL after login
 *    - Changed from /home to /account/dashboard
 *    - Users are now redirected to their account dashboard after successful login
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /** Service to load user-specific data */
    private final UserDetailsService userDetailsService;

    /**
     * Constructor injection of UserDetailsService
     * @param userDetailsService service to load user-specific data
     */
    public SecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * Configures security filter chain
     * Defines URL access rules, login/logout behavior, and CSRF settings
     *
     * @param http HttpSecurity to configure
     * @return configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for development (should be enabled in production)
            .csrf(csrf -> csrf.disable())
            // Configure URL access rules
            .authorizeHttpRequests(auth -> auth
                // Public URLs that don't require authentication
                .requestMatchers("/", "/register", "/css/**", "/js/**", "/images/**","/verify-2fa").permitAll()
                .requestMatchers("/account/**").authenticated()
                // All other URLs require authentication
                .anyRequest().authenticated()
            )
            // Configure login settings
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/account/dashboard", true)
                .permitAll()
            )
            // Configure logout settings
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );
        
        return http.build();
    }

    /**
     * Configures password encoder
     * Uses BCrypt hashing algorithm for password encryption
     *
     * @return configured PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures authentication manager
     * Used by Spring Security for authentication operations
     *
     * @param authConfig authentication configuration
     * @return configured AuthenticationManager
     * @throws Exception if configuration fails
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Configures authentication provider
     * Sets up database-backed user authentication with password encoding
     *
     * @return configured DaoAuthenticationProvider
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
} 