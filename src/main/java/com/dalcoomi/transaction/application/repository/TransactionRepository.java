package com.dalcoomi.transaction.application.repository;

import java.util.List;

import com.dalcoomi.transaction.domain.Transaction;
import com.dalcoomi.transaction.dto.TransactionSearchCriteria;

public interface TransactionRepository {

	Transaction save(Transaction transaction);

	List<Transaction> saveAll(List<Transaction> transaction);

	Transaction findByIdAndCreatorId(Long transactionId, Long creatorId);

	List<Transaction> findTransactions(TransactionSearchCriteria criteria);

	void deleteByTeamId(Long groupId);
}
