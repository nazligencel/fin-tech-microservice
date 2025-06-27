package com.fintech.fin_tech.services;

import com.fintech.fin_tech.dto.CategorySummaryDto;
import com.fintech.fin_tech.dto.DashboardSummaryDto;
import com.fintech.fin_tech.model.Transaction;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface TransactionService {

    Transaction addTransaction(Transaction transaction);

    List<Transaction> getAllTransactionsForCurrentUser();

    Optional<Transaction> getTransactionByIdForCurrentUser(Long id);

    Transaction updateTransactionForCurrentUser(Long id, Transaction transactionDetails);

    void deleteTransactionForCurrentUser(Long id);

    DashboardSummaryDto getDashboardSummaryForCurrentUser(Integer year, Integer month);

    List<CategorySummaryDto> getExpenseSummaryByCategoryForCurrentUser(Integer year, Integer month);
}
