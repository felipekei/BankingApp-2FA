package com.example.BankingAppFB.service;

import com.example.BankingAppFB.model.Account;
import com.example.BankingAppFB.model.AccountType;
import com.example.BankingAppFB.model.TransactionType;
import com.example.BankingAppFB.model.User;
import com.example.BankingAppFB.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
/**
 * Account Service
 * Handles business logic for account-related operations including:
 * - Account creation and retrieval
 * - Money deposits and withdrawals
 * - Balance management
 * - Transaction log
 * 
 * Features:
 * 1. Automatic account creation for new users
 * 2. Transaction management with @Transactional annotation
 * 3. Balance validation for withdrawals
 * 4. Many-to-one relationship with User entity
 * 
 * Security:
 * - All operations are transactional
 * - Prevents negative balance withdrawals
 * - Throws RuntimeException for insufficient funds
 */
@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;  // Repository for database operations

    @Autowired
    private TransactionService transactionService;  // Service for transaction log

    @Autowired
    private EmailService emailService; // For the emails

    @Transactional
    public void transferFunds(User user, AccountType fromAccountType, AccountType toAccountType, BigDecimal amount) {
        if (fromAccountType.equals(toAccountType)) {
            throw new IllegalArgumentException("Source and destination accounts must be different.");
        }

        // In case the user puts a negative value
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Invalid transfer amount. Please enter a positive value.");
        }    

        // Fetch source and destination accounts
        Account sourceAccount = accountRepository.findByUserAndAccountType(user, fromAccountType)
                .orElseThrow(() -> new RuntimeException("Source account not found."));
        Account destinationAccount = accountRepository.findByUserAndAccountType(user, toAccountType)
                .orElseThrow(() -> new RuntimeException("Destination account not found."));

        // Validate sufficient funds in the source account
        if (sourceAccount.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds in the source account.");
        }

        // Perform the fund transfer
        sourceAccount.setBalance(sourceAccount.getBalance().subtract(amount));
        destinationAccount.setBalance(destinationAccount.getBalance().add(amount));

        // Save updated account balances
        accountRepository.save(sourceAccount);
        accountRepository.save(destinationAccount);

        // Log the transfer transaction
        transactionService.logTransfer(sourceAccount, destinationAccount, amount);
    }

    public Account createAccount(User user, AccountType accountType) {  // Creates a new account for a user
        if (accountRepository.findByUserAndAccountType(user, accountType).isPresent()) {
            throw new RuntimeException("This Type of account already exists");
        }
        Account account = new Account();  // Initialize new account instance
        account.setUser(user);  // Set the user for the account
        account.setAccountType(accountType);
        return accountRepository.save(account);  // Save and return the account
    }

    public Account getAccount(User user, AccountType accountType) {  // Retrieves existing account or creates new one
        return accountRepository.findByUserAndAccountType(user, accountType)  // Try to find existing account
        .orElseThrow(() -> new RuntimeException("Account not found for the specified type"));  // Create new if not found
    }

    public List<Account> getAllAccountsForUser(User user) {
        return accountRepository.findAllByUser(user);
    }

    @Transactional
    public Account addMoney(User user, AccountType accountType, BigDecimal amount) {  // Adds money to user's account
        Account account = getAccount(user, accountType);  // Fetch the specific account
        account.setBalance(account.getBalance().add(amount));  // Add amount to balance
        Account updatedAccount = accountRepository.save(account); // Saves the update
        transactionService.logTransaction(updatedAccount, amount, TransactionType.DEPOSIT); // Log the deposit transaction
        return updatedAccount;
    }

    @Transactional
    public Account withdrawMoney(User user, AccountType accountType, BigDecimal amount) {  // Withdraws money from account
        Account account = getAccount(user, accountType);  // Fetch the specific account
        if (account.getBalance().compareTo(amount) < 0) {  // Check if sufficient funds
            throw new RuntimeException("Insufficient funds");  // Throw exception if insufficient
        }
        account.setBalance(account.getBalance().subtract(amount));  // Subtract amount from balance
        Account updatedAccount = accountRepository.save(account); // Saves updated account

        transactionService.logTransaction(updatedAccount, amount, TransactionType.WITHDRAWAL); // Log the withdrawn transaction

        // Check for suspicious withdrawal
        if (amount.compareTo(BigDecimal.valueOf(1000)) >= 0) { // Threshold of $1000 set for alerts
            String subject = "Suspicious Withdrawal Alert";
            String body = "Dear " + user.getUsername() + ",\n\n"
                    + "A withdrawal of $" + amount + " was made from your " + accountType + " account.\n"
                    + "Date: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + "\n\n"
                    + "If this was not authorized, please contact support immediately.\n\n"
                    + "Best regards,\nYour Banking App Team";
    
            emailService.sendEmail(user.getEmail(), subject, body); // Send email alert
        }    

        return updatedAccount;
    }
} 