package com.example.BankingAppFB.service;

import com.example.BankingAppFB.model.AccountType;
import com.example.BankingAppFB.model.PendingTransaction;
import com.example.BankingAppFB.model.User;
import com.example.BankingAppFB.repository.PendingTransactionRepository;
import com.example.BankingAppFB.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.Optional;

@Service
public class PendingTransactionService {

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PendingTransactionRepository pendingTransactionRepository;

    /**
     * Stores a pending transaction for a user before verifying 2FA.
     */
    public void storePendingTransaction(String username, AccountType accountType, BigDecimal amount) {
        // Retrieve the user from the database
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        // Create a new pending transaction with status "PENDING"
        PendingTransaction transaction = new PendingTransaction(user, accountType, amount);
        transaction.setStatus(PendingTransaction.TransactionStatus.PENDING);  // Set status to PENDING

        // Save the transaction to the database
        pendingTransactionRepository.save(transaction);
    }

    /**
     * Verifies the 2FA code and processes the withdrawal if valid.
     */
    public PendingTransaction verifyAndProcessWithdrawal(String username, int verificationCode) {
        // Retrieve the first pending transaction for the user with status PENDING
        Optional<PendingTransaction> pendingTransactionOptional = pendingTransactionRepository
                .findFirstByUser_UsernameAndStatus(username, PendingTransaction.TransactionStatus.PENDING);

        if (!pendingTransactionOptional.isPresent()) {
            System.err.println("Warning: No pending transaction found for user: " + username + " during verification.");
            return null; // Indicate failure due to no pending transaction
        }

        PendingTransaction transaction = pendingTransactionOptional.get();

        // Verify the 2FA code
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        boolean isValid = userService.verifyTwoFactorCode(user.getSecretKey(), verificationCode);

        if (isValid) {
            // Process the withdrawal if 2FA is valid
            accountService.withdrawMoney(user, transaction.getAccountType(), transaction.getAmount());

            // Update the transaction status to "COMPLETED"
            transaction.setStatus(PendingTransaction.TransactionStatus.COMPLETED);
            pendingTransactionRepository.save(transaction);   // Save the updated transaction

            return transaction; // Return the completed transaction
        } else {
            // If the 2FA code is invalid, update the status to "FAILED"
            transaction.setStatus(PendingTransaction.TransactionStatus.FAILED);
            pendingTransactionRepository.save(transaction);   // Save the failed transaction

            return transaction; // Return the failed transaction
        }
    }

    public PendingTransaction getPendingTransaction(String username) {

        // Fetch the first pending transaction for this user
        return pendingTransactionRepository.findFirstByUser_UsernameAndStatus(username, PendingTransaction.TransactionStatus.PENDING)
        .orElse(null);
    }

    public boolean cancelPendingTransaction(String username) {
        Optional<PendingTransaction> pendingTransactionOptional = pendingTransactionRepository
                .findFirstByUser_UsernameAndStatus(username, PendingTransaction.TransactionStatus.PENDING);

        if (pendingTransactionOptional.isPresent()) {
            PendingTransaction pendingTransaction = pendingTransactionOptional.get();
            pendingTransaction.setStatus(PendingTransaction.TransactionStatus.CANCELED);
            pendingTransactionRepository.save(pendingTransaction);
            return true;
        }
        return false;
    }
    
}
