package com.transaction_service.transaction_service.repositories;

import com.fintech.fin_tech.dto.CategorySummaryDto;
import com.transaction_service.transaction_service.model.Transaction;
import com.transaction_service.transaction_service.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserId(Long id);

    // Belirli bir kullanıcı ve tür için toplam tutar (Tüm zamanlar)
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.userId = :userId AND t.type = :type") //COALESCE burada, sorgunun sonucunun NULL değil her zaman olmasını garanti eder
    BigDecimal sumAmountByUserIdAndType(@Param("userId") Long userId, @Param("type") TransactionType type);

    // Belirli bir kullanıcı, tür, yıl ve ay için toplam tutar
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.userId = :userId AND t.type = :type AND YEAR(t.transactionDate) = :year AND MONTH(t.transactionDate) = :month")
    BigDecimal sumAmountByUserIdAndTypeAndYearAndMonth(
            @Param("userId") Long userId,
            @Param("type") TransactionType type,
            @Param("year") int year,
            @Param("month") int month
    );

    // Belirli bir kullanıcı için kategorilere göre gider toplamları
    @Query("SELECT new com.fintech.fin_tech.dto.CategorySummaryDto(t.category, COALESCE(SUM(t.amount), 0)) " +
            "FROM Transaction t " +
            "WHERE t.userId = :userId AND t.type = com.transaction_service.transaction_service.model.TransactionType.EXPENSE " +
            "GROUP BY t.category")
    List<CategorySummaryDto> findExpenseSumByCategoryForUser(@Param("userId") Long userId);

    // Belirli bir kullanıcı, yıl ve ay için kategorilere göre gider toplamları
    @Query("SELECT new com.fintech.fin_tech.dto.CategorySummaryDto(t.category, COALESCE(SUM(t.amount), 0)) " +
            "FROM Transaction t " +
            "WHERE t.userId = :userId AND t.type = com.transaction_service.transaction_service.model.TransactionType.EXPENSE " +
            "AND YEAR(t.transactionDate) = :year AND MONTH(t.transactionDate) = :month " +
            "GROUP BY t.category")
    List<CategorySummaryDto> findExpenseSumByCategoryAndYearAndMonthForUser(
            @Param("userId") Long userId,
            @Param("year") int year,
            @Param("month") int month
    );
}
