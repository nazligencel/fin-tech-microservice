package com.fintech.fin_tech.controller;

import com.fintech.fin_tech.config.security.CustomUserDetails;
import com.fintech.fin_tech.dto.CategorySummaryDto;
import com.fintech.fin_tech.dto.DashboardSummaryDto;
import com.fintech.fin_tech.model.Transaction;
import com.fintech.fin_tech.services.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@PreAuthorize("isAuthenticated()") //controllerdaki tüm metodlar kimlik doğrulaması gerektirir
public class TransactionController {

    private TransactionService transactionService;
    private final UserRepository userRepository;

    @Autowired
    public TransactionController(TransactionService transactionService, UserRepository userRepository) {
        this.transactionService = transactionService;
        this.userRepository = userRepository;
    }

    private User getCurrentUserEntity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            // Bu durum @PreAuthorize("isAuthenticated()") tarafından yakalanmalı
            throw new IllegalStateException("Kullanıcı kimliği doğrulanmamış.");
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Kimliği doğrulanmış kullanıcı veritabanında bulunamadı."));
    }

    @PostMapping
    public ResponseEntity<Transaction> addTransaction(@RequestBody Transaction transaction) {
        Transaction newTransaction = transactionService.addTransaction(transaction);
        return new ResponseEntity<>(newTransaction, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Transaction>> getAllTransactionsForCurrentUser() {
        List<Transaction> transactions = transactionService.getAllTransactionsForCurrentUser();
        return ResponseEntity.ok(transactions);
    }
    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransactionByIdForCurrentUser(@PathVariable Long id) {
        return transactionService.getTransactionByIdForCurrentUser(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @PutMapping("/{id}")
    public ResponseEntity<Transaction> updateTransactionForCurrentUser(@PathVariable Long id, @RequestBody Transaction transaction) {
        try {
            Transaction updateTransaction = transactionService.updateTransactionForCurrentUser(id, transaction);
            return ResponseEntity.ok(updateTransaction);
        }catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransactionForCurrentUser(@PathVariable Long id){
        try {
            transactionService.deleteTransactionForCurrentUser(id);
            return ResponseEntity.noContent().build();// Başarılı silme için 204 No Content
        }catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDto> getDashboardSummary(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        DashboardSummaryDto summary = transactionService.getDashboardSummaryForCurrentUser(year, month);
        return ResponseEntity.ok(summary);
    }
    @GetMapping("/expenses/by-category")
    public ResponseEntity<List<CategorySummaryDto>> getExpenseSummaryByCategory(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        List<CategorySummaryDto> categorySummaries = transactionService.getExpenseSummaryByCategoryForCurrentUser(year, month);
        return ResponseEntity.ok(categorySummaries);
    }
}
