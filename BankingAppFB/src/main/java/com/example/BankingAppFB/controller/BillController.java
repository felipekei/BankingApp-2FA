package com.example.BankingAppFB.controller;

import com.example.BankingAppFB.model.Account;
import com.example.BankingAppFB.model.AccountType;
import com.example.BankingAppFB.model.BillType;
import com.example.BankingAppFB.model.Biller;
import com.example.BankingAppFB.model.Frequency;
import com.example.BankingAppFB.model.User;
import com.example.BankingAppFB.repository.AccountRepository;
import com.example.BankingAppFB.service.BillerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/bill")
public class BillController {

    @Autowired
    private BillerService billerService;

    @Autowired
    private AccountRepository accountRepository;
    // Endpoint to add a new payee
    @PostMapping("/add-payee")
    public String addPayee(@RequestParam BillType billType,
                           @RequestParam String accountNumber,
                           @RequestParam Frequency paymentFrequency,
                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate nextPaymentDate,
                           @RequestParam AccountType paymentAccountType,
                           @RequestParam BigDecimal paymentAmount,
                           Authentication authentication,
                           RedirectAttributes redirectAttributes) {
        try {
            User user = billerService.findUserByUsername(authentication.getName());
            billerService.addPayee(user, billType, accountNumber, paymentFrequency, nextPaymentDate, paymentAccountType, paymentAmount);
            redirectAttributes.addFlashAttribute("successMessage", "Payee added successfully!");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/bill/list";
    }

    // Endpoint to view all billers for a user
    @GetMapping("/list")
    public String viewBillers(Authentication authentication, Model model) {
        User user = billerService.findUserByUsername(authentication.getName());
        model.addAttribute("username", user.getUsername());
        List<Biller> billers = billerService.getBillersForUser(user);
        List<Account> accounts = accountRepository.findAllByUser(user); // Fetch user accounts
        model.addAttribute("billers", billers);
        model.addAttribute("accounts", accounts); // Add accounts to the model
        return "billpayments";
    }

    // Endpoint to process a bill payment
    @PostMapping("/pay-biller")
    public String payBiller(@RequestParam Long billerId,
                            @RequestParam Long accountId,
                            @RequestParam BigDecimal amount,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {
        try {
            User user = billerService.findUserByUsername(authentication.getName());
            Account account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new RuntimeException("Account not found."));
            billerService.payBiller(user, billerId, account, amount); // Pass account to the service
            redirectAttributes.addFlashAttribute("successMessage", "Bill payment completed successfully!");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/bill/list";
    }
    // Trigger to test scheduler
    @ResponseBody
    @GetMapping("/trigger-scheduler")
    public String triggerScheduler() {
        try {
            billerService.processScheduledPayments();
            return "Scheduled payments processed successfully!";
        } catch (Exception e) {
            return "Error while processing scheduled payments: " + e.getMessage();
        }
    }

}