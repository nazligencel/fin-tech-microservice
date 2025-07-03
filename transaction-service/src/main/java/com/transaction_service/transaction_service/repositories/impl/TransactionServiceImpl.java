package com.transaction_service.transaction_service.repositories.impl;


import com.transaction_service.transaction_service.dto.CategorySummaryDto;
import com.transaction_service.transaction_service.dto.DashboardSummaryDto;
import com.transaction_service.transaction_service.services.TransactionService;
import com.transaction_service.transaction_service.model.Transaction;
import com.transaction_service.transaction_service.model.TransactionType;
import com.transaction_service.transaction_service.repositories.TransactionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionServiceImpl implements TransactionService {

    private TransactionRepository transactionRepository;

    @Autowired
    public TransactionServiceImpl(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;

    }

    private String getCurrentUserIdFromToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User identity verification failed");
        }
        if (authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            // Keycloak'un standart kullanıcı ID'si 'sub' (subject) claim'indedir
            String userIdString = jwt.getSubject();
            if (userIdString != null) {
                throw new IllegalStateException("JWT token does not contain user id claim");
            }
            return jwt.getSubject();
        }
        /**
         *Keycloak'un ID'si UUID formatında bir string'dir
         *
         */
        throw new IllegalStateException("Authentication principal is of an unexpected type " + authentication.getPrincipal().getClass().getName());
    }


    @Override
    @Transactional
    public Transaction addTransaction(Transaction transaction) {
        String currentUserId = getCurrentUserIdFromToken();
        transaction.setUserId(currentUserId);
        return transactionRepository.save(transaction);
    }
    @Override
    @Transactional(readOnly = true)
    public List<Transaction> getAllTransactionsForCurrentUser() {
        String  currentUserId = getCurrentUserIdFromToken();
        // Repository metodu artık 'userId' ile çalışıyor
        return transactionRepository.findByUserId(currentUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Transaction> getTransactionByIdForCurrentUser(Long transactionId) {
        String currentUserId = getCurrentUserIdFromToken();
        // Önce işlemi bul, sonra o işlemin kullanıcımıza ait olup olmadığını kontrol et.
        return transactionRepository.findById(transactionId)
                .filter(transaction -> transaction.getUserId().equals(currentUserId));
    }

    @Override
    public Transaction updateTransactionForCurrentUser(Long transactionId, Transaction transactionDetails) {
        String currentUserId = getCurrentUserIdFromToken();
        Transaction existingTransaction = transactionRepository.findById(transactionId)
                .filter(transaction -> transaction.getUserId().equals(currentUserId))
                .orElseThrow(() -> new RuntimeException("Transaction not found with transactionId: " + transactionId + " for current user"));

        existingTransaction.setType(transactionDetails.getType());
        existingTransaction.setAmount(transactionDetails.getAmount());
        existingTransaction.setTransactionDate(transactionDetails.getTransactionDate());
        existingTransaction.setCategory(transactionDetails.getCategory());
        existingTransaction.setDescription(transactionDetails.getDescription());

        return transactionRepository.save(existingTransaction);
    }

    @Override
    public void deleteTransactionForCurrentUser(Long transactionId) {
        //önce silinecek işlemi bul ve sahibinin mevcut kullanıcı olduğunu doğrula
        Transaction transactionDelete = transactionRepository.findById(transactionId)
                .filter(transaction -> transaction.getUserId().equals(transactionId))
                .orElseThrow(() -> new RuntimeException("Transaction not found with transactionId: " + transactionId + " for current user for deletion."));
        transactionRepository.delete(transactionDelete);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryDto getDashboardSummaryForCurrentUser(Integer year, Integer month) {
        String currentUserId = getCurrentUserIdFromToken();

        BigDecimal totalIncome;
        BigDecimal totalExpense;

        if(year != null && month != null){
            totalIncome = transactionRepository.sumAmountByUserIdAndTypeAndYearAndMonth(currentUserId, TransactionType.INCOME, year, month);
            totalExpense = transactionRepository.sumAmountByUserIdAndTypeAndYearAndMonth(currentUserId, TransactionType.EXPENSE, year, month);
        }else {
            totalIncome = transactionRepository.sumAmountByUserIdAndType(currentUserId, TransactionType.INCOME);
            totalExpense = transactionRepository.sumAmountByUserIdAndType(currentUserId, TransactionType.EXPENSE);
        }

        BigDecimal balance = totalIncome.subtract(totalExpense);
        return new DashboardSummaryDto(totalIncome, totalExpense, balance);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategorySummaryDto> getExpenseSummaryByCategoryForCurrentUser(Integer year, Integer month) {
        String currentUserId = getCurrentUserIdFromToken();
        if (year != null && month != null) {
            return transactionRepository.findExpenseSumByCategoryAndYearAndMonthForUser(currentUserId, year, month);
        } else {
            return transactionRepository.findExpenseSumByCategoryForUser(currentUserId);
        }
    }
}
