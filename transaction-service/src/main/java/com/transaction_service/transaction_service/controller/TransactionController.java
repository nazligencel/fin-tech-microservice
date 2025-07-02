package com.transaction_service.transaction_service.controller;


import com.transaction_service.transaction_service.dto.CategorySummaryDto;
import com.transaction_service.transaction_service.dto.DashboardSummaryDto;
import com.transaction_service.transaction_service.model.Transaction;
import com.transaction_service.transaction_service.services.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@PreAuthorize("isAuthenticated()") //controllerdaki tüm metodlar kimlik doğrulaması gerektirir
public class TransactionController {

    private TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
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
