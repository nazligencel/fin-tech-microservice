package com.transaction_service.transaction_service.repositories.impl;


import com.transaction_service.transaction_service.config.security.JwtUtil;
import com.fintech.fin_tech.dto.CategorySummaryDto;
import com.fintech.fin_tech.dto.DashboardSummaryDto;
import com.transaction_service.transaction_service.services.TransactionService;
import com.transaction_service.transaction_service.model.Transaction;
import com.transaction_service.transaction_service.model.TransactionType;
import com.transaction_service.transaction_service.repositories.TransactionRepository;
import io.jsonwebtoken.Claims;
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
    private final JwtUtil jwtUtil;// JWT parse edilip içinden userId alınacak

    @Autowired
    public TransactionServiceImpl(TransactionRepository transactionRepository, JwtUtil jwtUtil) {
        this.transactionRepository = transactionRepository;
        this.jwtUtil = jwtUtil;
    }
    /**
     * Güvenlik context'inden gelen JWT'yi parse ederek mevcut kullanıcının ID'sini alır.
     * Bu metot, bu servisteki tüm işlemlerin doğru kullanıcıya ait olmasını sağlar.
     * JWT'nin "userId" adında bir claim içermesi beklenir.
     * @return Mevcut kullanıcının Long tipindeki ID'si.
     * @throws IllegalStateException Kullanıcı kimliği doğrulanamazsa veya token'da ID yoksa.
     */
    private Long getCurrentUserIdFromToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User is not authenticated");
        }

        String username = jwtUtil.extractUsername(authentication.getName()); //jwtutil den kullanıcı adı alındı
        Claims claims = jwtUtil.extractAllClaims(authentication.getName()); //jwt den claimleri al

        Integer userIdInt = claims.get("user_id", Integer.class);
        if (userIdInt == null) {
            throw new IllegalStateException("The JWT token does not contain the 'user_id' claim.");
        }
        return userIdInt.longValue();
    }


    @Override
    @Transactional
    public Transaction addTransaction(Transaction transaction) {
        Long currentUserId = getCurrentUserIdFromToken();
        transaction.setUserId(currentUserId);
        return transactionRepository.save(transaction);
    }
    @Override
    @Transactional(readOnly = true)
    public List<Transaction> getAllTransactionsForCurrentUser() {
        Long currentUserId = getCurrentUserIdFromToken();
        // Repository metodu artık 'userId' ile çalışıyor
        return transactionRepository.findByUserId(currentUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Transaction> getTransactionByIdForCurrentUser(Long transactionId) {
        Long currentUserId = getCurrentUserIdFromToken();
        // Önce işlemi bul, sonra o işlemin kullanıcımıza ait olup olmadığını kontrol et.
        return transactionRepository.findById(transactionId)
                .filter(transaction -> transaction.getUserId().equals(currentUserId));
    }

    @Override
    public Transaction updateTransactionForCurrentUser(Long transactionId, Transaction transactionDetails) {
        Long currentUserId = getCurrentUserIdFromToken();
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
        Long currentUserId = getCurrentUserIdFromToken();
        //önce silinecek işlemi bul ve sahibinin mevcut kullanıcı olduğunu doğrula
        Transaction transactionDelete = transactionRepository.findById(transactionId)
                .filter(transaction -> transaction.getUserId().equals(transactionId))
                .orElseThrow(() -> new RuntimeException("Transaction not found with transactionId: " + transactionId + " for current user for deletion."));
        transactionRepository.delete(transactionDelete);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryDto getDashboardSummaryForCurrentUser(Integer year, Integer month) {
        Long currentUserId = getCurrentUserIdFromToken();

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
        Long currentUserId = getCurrentUserIdFromToken();
        if (year != null && month != null) {
            return transactionRepository.findExpenseSumByCategoryAndYearAndMonthForUser( Long currentUserId = getCurrentUserIdFromToken();, year, month);
        } else {
            return transactionRepository.findExpenseSumByCategoryForUser( Long currentUserId = getCurrentUserIdFromToken(););
        }
    }
}
