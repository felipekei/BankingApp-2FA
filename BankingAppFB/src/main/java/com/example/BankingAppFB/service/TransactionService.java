package com.example.BankingAppFB.service;

import com.example.BankingAppFB.model.Account;
import com.example.BankingAppFB.model.TransactionLog;
import com.example.BankingAppFB.model.TransactionType;
import com.example.BankingAppFB.model.User;
import com.example.BankingAppFB.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page; 
import org.springframework.data.domain.Pageable; 
import org.springframework.data.domain.Sort;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    // Logs a single transaction (deposits/withdrawals)
    public TransactionLog logTransaction(Account account, BigDecimal amount, TransactionType type) {
        TransactionLog transaction = new TransactionLog();
        transaction.setAccount(account);
        transaction.setAmount(amount);
        transaction.setType(type);
        transaction.setTransactionDate(LocalDateTime.now());
        return transactionRepository.save(transaction);
    }

    // Logs a transfer transaction (Source and Destination accounts)
    public TransactionLog logTransfer(Account sourceAccount, Account destinationAccount, BigDecimal amount) {
        TransactionLog transaction = new TransactionLog();
        transaction.setSourceAccount(sourceAccount);
        transaction.setDestinationAccount(destinationAccount);
        transaction.setAmount(amount);
        transaction.setType(TransactionType.TRANSFER);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setAccount(destinationAccount); 
        return transactionRepository.save(transaction);
    }

    // Logs a bill payment transaction
    public TransactionLog logBillPayment(Account account, BigDecimal amount, String billerDetails) {
        TransactionLog transaction = new TransactionLog();
        transaction.setAccount(account); // The user's account from which the payment is deducted
        transaction.setAmount(amount);  // The bill payment amount
        transaction.setType(TransactionType.BILL_PAYMENT); // Type of transaction
        transaction.setTransactionDate(LocalDateTime.now()); // Log the date/time of the payment
        transaction.setMessage("Bill Payment: " + billerDetails); // Custom message for bill payment
        return transactionRepository.save(transaction);
    }


    // Get all transaction logs for a user
    public List<TransactionLog> getTransactionsForUser(User user) {
        return transactionRepository.findByAccountUser(user);
    }


    // Passes filters and pagination
    public Page<TransactionLog> getFilteredTransactions(User user, LocalDateTime startDate, LocalDateTime endDate, TransactionType type, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending());
        return transactionRepository.findByFilters(user, startDate, endDate, type, pageable);
    }

}