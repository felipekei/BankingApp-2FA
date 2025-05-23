package com.example.BankingAppFB.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.BankingAppFB.model.User;
import java.util.Optional;

/**
 * User Repository Interface
 * Provides database operations for User entities
 * Extends JpaRepository to inherit basic CRUD operations
 * 
 * This interface defines custom query methods in addition to
 * the standard JPA repository operations
 */
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Finds a user by their email address
     * @param email the email address to search for
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Finds a user by their username
     * @param username the username to search for
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Checks if a user exists with the given email
     * @param email the email address to check
     * @return true if a user exists with this email, false otherwise
     */
    boolean existsByEmail(String email);
    
    /**
     * Checks if a user exists with the given username
     * @param username the username to check
     * @return true if a user exists with this username, false otherwise
     */
    boolean existsByUsername(String username);
} 