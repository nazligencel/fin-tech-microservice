package com.transaction_service.transaction_service.repositories.impl;

import com.fintech.fin_tech.config.security.CustomUserDetails;
import com.fintech.fin_tech.dto.CategorySummaryDto;
import com.fintech.fin_tech.dto.DashboardSummaryDto;
import com.fintech.fin_tech.model.Transaction;
import com.fintech.fin_tech.repositories.TransactionRepository;
import com.fintech.fin_tech.services.TransactionService;
import com.transaction_service.transaction_service.model.TransactionType;
import com.transaction_service.transaction_service.repositories.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionServiceImpl implements TransactionService {

    private TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Autowired
    public TransactionServiceImpl(TransactionRepository transactionRepository, UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }
    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            throw new IllegalStateException("User not authenticated");
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in database"));
    }

    @Override
    @Transactional
    public Transaction addTransaction(Transaction transaction) {
        User currentUser = getCurrentAuthenticatedUser();
        transaction.setUser(currentUser);
        return transactionRepository.save(transaction);
    }

    @Override
    public List<Transaction> getAllTransactionsForCurrentUser() {
        User currentUser = getCurrentAuthenticatedUser();
        return transactionRepository.findByUserId(currentUser.getId());
    }

    @Override
    public Optional<Transaction> getTransactionByIdForCurrentUser(Long id) {
        User currentUser = getCurrentAuthenticatedUser();
        return transactionRepository.findById(id) //önce id ile transaction bul sonra kullanıcısını kontrol et
                .filter(transaction -> transaction.getUser().equals(currentUser));
    }

    @Override
    public Transaction updateTransactionForCurrentUser(Long id, Transaction transactionDetails) {
        User currentUser = getCurrentAuthenticatedUser();
        Transaction existingTransaction = transactionRepository.findById(id)
                .filter(transaction -> transaction.getUser().getId().equals(currentUser.getId()))
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id + " for current user"));

        existingTransaction.setType(transactionDetails.getType());
        existingTransaction.setAmount(transactionDetails.getAmount());
        existingTransaction.setTransactionDate(transactionDetails.getTransactionDate());
        existingTransaction.setCategory(transactionDetails.getCategory());
        existingTransaction.setDescription(transactionDetails.getDescription());

        return transactionRepository.save(existingTransaction);
    }

    @Override
    public void deleteTransactionForCurrentUser(Long id) {
        User currentUser = getCurrentAuthenticatedUser();
        Transaction transactionDelete = transactionRepository.findById(id)
                .filter(transaction -> transaction.getUser().getId().equals(currentUser.getId()))
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id + " for current user for deletion."));
        transactionRepository.delete(transactionDelete);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryDto getDashboardSummaryForCurrentUser(Integer year, Integer month) {
        User currentUser = getCurrentAuthenticatedUser();
        Long userId = currentUser.getId();

        BigDecimal totalIncome;
        BigDecimal totalExpense;

        if(year != null && month != null){
            totalIncome = transactionRepository.sumAmountByUserIdAndTypeAndYearAndMonth(userId, TransactionType.INCOME, year, month);
            totalExpense = transactionRepository.sumAmountByUserIdAndTypeAndYearAndMonth(userId, TransactionType.EXPENSE, year, month);
        }else {
            totalIncome = transactionRepository.sumAmountByUserIdAndType(userId, TransactionType.INCOME);
            totalExpense = transactionRepository.sumAmountByUserIdAndType(userId, TransactionType.EXPENSE);
        }

        BigDecimal balance = totalIncome.subtract(totalExpense);
        return new DashboardSummaryDto(totalIncome, totalExpense, balance);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategorySummaryDto> getExpenseSummaryByCategoryForCurrentUser(Integer year, Integer month) {
        User currentUser = getCurrentAuthenticatedUser();
        Long userId = currentUser.getId();
        if (year != null && month != null) {
            return transactionRepository.findExpenseSumByCategoryAndYearAndMonthForUser(userId, year, month);
        } else {
            return transactionRepository.findExpenseSumByCategoryForUser(userId);
        }
    }
}
