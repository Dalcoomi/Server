package com.dalcoomi.transaction.application.repository;

import java.util.List;

import com.dalcoomi.transaction.domain.Transaction;

public interface TransactionRepository {

	List<Transaction> saveAll(List<Transaction> transaction);

	List<Transaction> findByMemberIdAndYearAndMonth(Long memberId, int year, int month);
}
