package com.dalcoomi.transaction.application.repository;

import java.time.LocalDateTime;
import java.util.List;

import com.dalcoomi.transaction.domain.Transaction;
import com.dalcoomi.transaction.dto.TransactionSearchCriteria;

public interface TransactionRepository {

	Transaction save(Transaction transaction);

	List<Transaction> saveAll(List<Transaction> transaction);

	Transaction findById(Long transactionId);

	List<Transaction> findAll();

	List<Transaction> findTransactions(TransactionSearchCriteria criteria);

	List<Transaction> findExpiredPersonalTransactions(LocalDateTime cutoffDate);

	List<Transaction> findExpiredAnonymizedPersonalTransactions(LocalDateTime cutoffDate);

	void deleteByTeamId(Long groupId);

	void deleteAll(List<Transaction> personalTransactions);
}
