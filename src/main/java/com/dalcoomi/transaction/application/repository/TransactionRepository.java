package com.dalcoomi.transaction.application.repository;

import java.util.List;

import com.dalcoomi.transaction.domain.Transaction;

public interface TransactionRepository {

	Transaction save(Transaction transaction);

	List<Transaction> saveAll(List<Transaction> transaction);

	Transaction findByIdAndCreatorId(Long transactionId, Long creatorId);

	List<Transaction> findByCreatorIdAndYearAndMonth(Long creatorId, int year, int month);

	List<Transaction> findByTeamId(Long teamId);

	void deleteByTeamId(Long groupId);
}
