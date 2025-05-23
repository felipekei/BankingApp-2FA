package com.example.BankingAppFB.model;

import jakarta.persistence.*;  // JPA annotations for database mapping
import lombok.Data;          // Lombok annotation for generating getters, setters, etc.
import java.math.BigDecimal; // For precise decimal arithmetic
import java.time.LocalDateTime; // For timestamp handling

/**
 * Account Entity
 * Represents a user's bank account in the system.
 * 
 * Features:
 * - Many-to-one relationship with User entity
 * - Tracks account balance
 * - Records account creation timestamp
 * 
 * Database Table: accounts
 * - id: Primary key
 * - user_id: Foreign key to users table
 * - balance: Current account balance
 * - created_at: Account creation timestamp
 */
@Entity  // Marks this class as a JPA entity
@Table(name = "accounts", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "account_type"})
)// Specifies the database table name and unique constraints
@Data  // Lombok annotation to generate getters, setters, equals, hashCode, and toString
public class Account {
    
    @Id  // Marks this field as the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto-generates ID values
    private Long id;  // Unique identifier for the account
    
    @ManyToOne  // Establishes Many-to-one relationship with User entity
    @JoinColumn(name = "user_id", nullable = false)  // Specifies the foreign key column
    private User user;  // Reference to the associated User entity
    
    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    @Column(nullable = false)  // Marks this field as a required database column
    private BigDecimal balance = BigDecimal.ZERO;  // Current account balance, initialized to zero
    
    @Column(name = "created_at", nullable = false)  // Specifies the column name and makes it required
    private LocalDateTime createdAt = LocalDateTime.now();  // Timestamp of account creation, set to current time
} 