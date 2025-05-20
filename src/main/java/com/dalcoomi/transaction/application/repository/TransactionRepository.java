package com.dalcoomi.transaction.application.repository;

import java.util.List;

import com.dalcoomi.transaction.domain.Transaction;

public interface TransactionRepository {

	Transaction save(Transaction transaction);

	List<Transaction> saveAll(List<Transaction> transaction);

	Transaction findById(Long transactionId);

	List<Transaction> findByMemberIdAndYearAndMonth(Long memberId, int year, int month);
}
