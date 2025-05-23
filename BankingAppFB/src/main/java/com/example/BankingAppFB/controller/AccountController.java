package com.example.BankingAppFB.controller;

import com.example.BankingAppFB.model.Account;
import com.example.BankingAppFB.model.AccountType;
import com.example.BankingAppFB.model.PendingTransaction;
import com.example.BankingAppFB.model.User;
import com.example.BankingAppFB.service.AccountService;
import com.example.BankingAppFB.service.PendingTransactionService;
import com.example.BankingAppFB.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


/**
 * Account Controller
 * Handles HTTP requests related to account operations including:
 * - Displaying account dashboard
 * - Processing money deposits
 * - Processing money withdrawals
 * 
 * Security:
 * - All endpoints require authentication
 * - Uses Spring Security's Authentication object to identify users
 * - Redirects to dashboard after operations
 */
@Controller
@RequestMapping("/account")
public class AccountController {

    /** Service for handling account-related business logic */
    @Autowired
    private AccountService accountService;

    /** Service for handling user-related business logic */
    @Autowired
    private UserService userService;

    @Autowired
    private PendingTransactionService pendingTransactionService;

    /**
     * Displays the user's account dashboard
     * Shows current balance and account information
     *
     * @param authentication Spring Security authentication object
     * @param model Spring MVC model for view data
     * @return the dashboard view name
     */
    @GetMapping("/dashboard")
    public String showDashboard(Authentication authentication, Model model) {
        User user = userService.findByUsername(authentication.getName());
        List<Account> accounts = accountService.getAllAccountsForUser(user);
        if (accounts == null) {
            accounts = new ArrayList<>();
        }
        model.addAttribute("accounts", accounts);
        model.addAttribute("accountTypes", AccountType.values());
        model.addAttribute("username", authentication.getName());
        return "dashboard";
    }

    /**
     * Processes a money deposit request
     * Adds the specified amount to the user's account
     * @param accountType the type of account to deposit money into
     * @param amount the amount to deposit
     * @param authentication Spring Security authentication object
     * @return redirect to dashboard after successful deposit
     */
    @PostMapping("/add-money")
    public String addMoney(@RequestParam AccountType accountType, @RequestParam BigDecimal amount, Authentication authentication, RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(authentication.getName());
        try {
            accountService.addMoney(user, accountType, amount);
        } catch (RuntimeException ex) {
            // Check if trying to add balance for the wrong account type
            if ("Account not found for the specified type".equals(ex.getMessage())) {
                redirectAttributes.addFlashAttribute("depositError",
                        "No " + accountType + " account found. Please create the account first.");
            } else {
                redirectAttributes.addFlashAttribute("depositError",
                        "An error occurred: " + ex.getMessage());
            }
            return "redirect:/account/dashboard";
        }
        return "redirect:/account/dashboard";
    }

    /**
     * Processes a money withdrawal request
     * Deducts the specified amount from the user's account
     * @param accountType the type of account to withdraw
     * @param amount the amount to withdraw
     * @param authentication Spring Security authentication object
     * @return redirect to dashboard after successful withdrawal
     */
    @PostMapping("/withdraw-money")
    public String withdrawMoney(@RequestParam AccountType accountType, @RequestParam BigDecimal amount,
                                Authentication authentication, RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(authentication.getName());

        // Check if the user has the specified account type
        try {
            accountService.getAccount(user, accountType); // This will throw an exception if the account doesn't exist
        } catch (RuntimeException ex) {
            if ("Account not found for the specified type".equals(ex.getMessage())) {
                redirectAttributes.addFlashAttribute("withdrawError",
                        "No " + accountType + " account found. Please select an existing account.");
                return "redirect:/account/dashboard";
            } else {
                redirectAttributes.addFlashAttribute("withdrawError",
                        "An error occurred: " + ex.getMessage());
                return "redirect:/account/dashboard";
            }
        }

        if (amount.compareTo(BigDecimal.valueOf(1000)) >= 0) {
            // Store pending withdrawal request only if the account exists
            pendingTransactionService.storePendingTransaction(user.getUsername(), accountType, amount);

            redirectAttributes.addFlashAttribute("2faRequired", true);
            redirectAttributes.addFlashAttribute("username", user.getUsername());

            return "redirect:/account/confirm-withdrawal";
        }

        // Process withdrawals below 1000 directly
        try {
            accountService.withdrawMoney(user, accountType, amount);
        } catch (RuntimeException ex) {
            // ... (rest of the error handling for withdrawals below 1000 remains the same) ...
            if ("Insufficient funds".equals(ex.getMessage())) {
                redirectAttributes.addFlashAttribute("withdrawError",
                        "Insufficient funds: You cannot withdraw more than your current balance.");
            } else {
                redirectAttributes.addFlashAttribute("withdrawError",
                        "An error occurred: " + ex.getMessage());
            }
            return "redirect:/account/dashboard";
        }
        return "redirect:/account/dashboard";
    }

    /**
     * Displays the withdrawal confirmation page where the user enters their 2FA
     * code.
     *
     * @param model          Spring MVC model to pass data to the view
     * @param authentication Authentication object for retrieving the user
     * @return the confirm withdrawal view
     */
    @GetMapping("/confirm-withdrawal")
    public String showConfirmWithdrawalPage(Model model, Authentication authentication) {
        // Retrieve the user and the pending transaction
        User user = userService.findByUsername(authentication.getName());
        PendingTransaction pendingTransaction = pendingTransactionService.getPendingTransaction(user.getUsername());

        if (pendingTransaction == null && !model.containsAttribute("error")) {
            return "redirect:/account/dashboard"; // No pending transaction, go back to dashboard
        }

        // Add transaction details to the model if not already there (from a failed attempt)
        if (!model.containsAttribute("accountType") && pendingTransaction != null) {
            model.addAttribute("username", user.getUsername());
            model.addAttribute("accountType", pendingTransaction.getAccountType());
            model.addAttribute("amount", pendingTransaction.getAmount());
        }

        return "confirm-withdrawal";
    }

    /**
     * Cancels the pending withdrawal transaction and redirects the user to the dashboard.
     *
     * @param authentication Spring Security authentication object to get the current user.
     * @param redirectAttributes Attributes for adding flash messages.
     * @return Redirects to the dashboard.
     */
    @GetMapping("/cancel-withdrawal")
    public String cancelPendingWithdrawal(Authentication authentication, RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(authentication.getName());
        boolean isCancelled = pendingTransactionService.cancelPendingTransaction(user.getUsername());

        if (isCancelled) {
            redirectAttributes.addFlashAttribute("errorMessage", "Withdrawal request cancelled.");
        }
        return "redirect:/account/dashboard";
    }
    

    /**
     * Processes the withdrawal confirmation after verifying the 2FA code.
     *
     * @param username           the user's username
     * @param code               the 6-digit 2FA code
     * @param redirectAttributes attributes for flash messages
     * @return redirect to the dashboard after confirmation
     */

     @PostMapping("/confirm-withdrawal")
     public String confirmWithdrawal(
             @RequestParam("username") String username,
             @RequestParam("code") String code,
             Model model, // Inject the Model
             RedirectAttributes redirectAttributes) {
 
         // Validate the 2FA code to ensure it's 6 digits
         if (!code.matches("\\d{6}")) {
             redirectAttributes.addFlashAttribute("error", "Invalid code. Please enter a 6-digit number.");
             return "redirect:/account/confirm-withdrawal";
         }
 
         // Convert code to integer for verification
         int verificationCode = Integer.parseInt(code);
 
         // Verify and process the withdrawal
         PendingTransaction processedTransaction = pendingTransactionService.verifyAndProcessWithdrawal(username, verificationCode);
 
         if (processedTransaction != null && processedTransaction.getStatus() == PendingTransaction.TransactionStatus.COMPLETED) {
             // If the transaction is successful, display a success message
             redirectAttributes.addFlashAttribute("successMessage", "Withdrawal completed successfully!");
             return "redirect:/account/dashboard";
         } else {
             // If the transaction failed (incorrect 2FA or no pending transaction),
             // display an error message and stay on the confirmation page
             model.addAttribute("error", "Invalid 2FA code. Please try again.");
             if (processedTransaction != null) {
                 model.addAttribute("accountType", processedTransaction.getAccountType());
                 model.addAttribute("amount", processedTransaction.getAmount());
                 model.addAttribute("username", username); // Ensure username is still available
             } else {
                 // Handle the case where no pending transaction was found (shouldn't happen normally here)
                 return "redirect:/account/dashboard?error=noPendingTransaction";
             }
             return "confirm-withdrawal"; // Return the view name directly, not a redirect
         }
     }



    /**
     * Processes the creation of a new type of account
     * Based on the option selected it adds the balance on the dashboard
     * @param accountType the type of account
     * @param authentication Spring Security authentication object
     * @return redirect to dashboard after successful withdrawal
     */
    @PostMapping("/create")
    public String createAccount(@RequestParam AccountType accountType,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(authentication.getName());
        try {
            accountService.createAccount(user, accountType);
        } catch (RuntimeException ex) {
            // Use flash attribute to pass the error message back to the dashboard.
            redirectAttributes.addFlashAttribute("accountCreationError", ex.getMessage());
        }
        return "redirect:/account/dashboard";
    }

    /**
     * Processes a fund transfer request
     * Transfers money from one account to another for the logged-in user.
     *
     * @param fromAccountType Source account type
     * @param toAccountType Destination account type
     * @param amount Transfer amount
     * @param authentication Spring Security authentication object
     * @param redirectAttributes Spring MVC mechanism for passing flash attributes
     * @return Redirects to the dashboard after successful transfer
     */
    @PostMapping("/transfer-funds")
    public String transferFunds(
            @RequestParam AccountType fromAccountType,
            @RequestParam AccountType toAccountType,
            @RequestParam BigDecimal amount,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {


        try {
            // Get the authenticated user
            User user = userService.findByUsername(authentication.getName());
            // Perform the transfer
            accountService.transferFunds(user, fromAccountType, toAccountType, amount);
            // Add success message
            redirectAttributes.addFlashAttribute("successMessage", "Transfer completed successfully!");

        } catch (RuntimeException ex) {
            // Add error message in case of failure
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        // Redirect back to dashboard
        return "redirect:/account/dashboard";
    }

} 