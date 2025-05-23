package com.example.BankingAppFB.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * User Entity
 * Represents a user in the system with their authentication and profile information.
 * This class is mapped to the 'users' table in the database and includes
 * validation constraints for data integrity.
 */
@Entity
@Table(name = "users")
@Data  // Lombok annotation to generate getters, setters, equals, hashCode, and toString
public class User {
    
    /**
     * Unique identifier for the user
     * Auto-generated using database identity strategy
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * User's username
     * Must be unique and between 3-50 characters
     * Used for display and identification purposes
     */
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(unique = true)
    private String username;
    
    /**
     * User's email address
     * Must be unique and in valid email format
     * Used for authentication and communication
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Column(unique = true)
    private String email;
    
    /**
     * User's password
     * Must be at least 6 characters long
     * Stored in encrypted format (handled by Spring Security)
     */
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;

    /**
     * User's secret key 
     * Stored in encrypted format (handled by TOTP)
     */
    @Column(name = "secret_key")
    private String secretKey;

} 