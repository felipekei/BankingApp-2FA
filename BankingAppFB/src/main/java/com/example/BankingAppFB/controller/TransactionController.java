package com.example.BankingAppFB.controller;

import com.example.BankingAppFB.model.TransactionLog;
import com.example.BankingAppFB.model.TransactionType;
import com.example.BankingAppFB.model.User;
import com.example.BankingAppFB.service.TransactionService;
import com.example.BankingAppFB.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.data.domain.Page; 

@Controller
@RequestMapping("/account")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserService userService;

    @GetMapping("/transactions")
    public String showTransactionHistory(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        Authentication authentication,
        Model model
    ) {
        User user = userService.findByUsername(authentication.getName());
        Page<TransactionLog> transactions = transactionService.getFilteredTransactions(user, null, null, null, page, size);
    
        model.addAttribute("transactions", transactions);
        model.addAttribute("transactionTypes", TransactionType.values());
        model.addAttribute("username", user.getUsername());
        return "transactionHistory";
    }

    @GetMapping("/filter")
    public String filterTransactions(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        @RequestParam(required = false) TransactionType type,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        Authentication authentication,
        Model model
    ) {
        User user = userService.findByUsername(authentication.getName()); // Fetch the logged-in user
        model.addAttribute("username", user.getUsername());
        LocalDateTime startDateTime = (startDate != null) ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = (endDate != null) ? endDate.atTime(LocalTime.MAX) : null;

        if (type == null) {
            System.out.println("Transaction Type is null, fetching all types...");
            
        }
    
        Page<TransactionLog> transactions = transactionService.getFilteredTransactions(
            user, startDateTime, endDateTime, type, page, size
        );

        model.addAttribute("transactions", transactions); // Paginated transactions
        model.addAttribute("transactionTypes", TransactionType.values()); // List of transaction types for the dropdown
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("type", type);
        return "transactionhistory"; // Template to render the results
    }

}
