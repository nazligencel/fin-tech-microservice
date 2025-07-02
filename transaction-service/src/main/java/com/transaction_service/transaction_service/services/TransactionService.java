package com.transaction_service.transaction_service.services;

import com.transaction_service.transaction_service.dto.DashboardSummaryDto;
import com.transaction_service.transaction_service.dto.CategorySummaryDto;
import com.transaction_service.transaction_service.model.Transaction;
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
