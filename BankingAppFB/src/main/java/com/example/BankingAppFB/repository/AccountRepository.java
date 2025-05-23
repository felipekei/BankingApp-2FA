package com.example.BankingAppFB.repository;

import com.example.BankingAppFB.model.Account;
import com.example.BankingAppFB.model.AccountType;
import com.example.BankingAppFB.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/**
 * Account Repository Interface
 * Provides database operations for Account entities
 * Extends JpaRepository to inherit basic CRUD operations
 * 
 * This interface defines custom query methods for account-related
 * database operations in addition to standard JPA repository operations
 */
public interface AccountRepository extends JpaRepository<Account, Long> {
    
    /**
     * Finds an account associated with a specific user
     * @param user the user whose account is to be found
     * @return Optional containing the account if found, empty otherwise
     */
    Optional<Account> findByUserAndAccountType(User user, AccountType accountType);
    Optional<Account> findByUser(User user);
    List<Account> findAllByUser(User user);
} 

