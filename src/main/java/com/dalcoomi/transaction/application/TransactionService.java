package com.dalcoomi.transaction.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dalcoomi.transaction.application.repository.TransactionRepository;
import com.dalcoomi.transaction.domain.Transaction;
import com.dalcoomi.transaction.dto.TransactionInfo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService {

	private final TransactionRepository transactionRepository;

	@Transactional
	public List<TransactionInfo> getMyTransactionsWithYearAndMonth(Long memberId, int year, int month) {
		List<Transaction> transactions = transactionRepository.findByMemberIdAndYearAndMonth(memberId, year, month);

		return transactions.stream().map(TransactionInfo::from).toList();
	}
}
