package com.dalcoomi.transaction.dto;

import static com.dalcoomi.transaction.domain.TransactionType.EXPENSE;
import static com.dalcoomi.transaction.domain.TransactionType.INCOME;

import java.util.List;

import com.dalcoomi.transaction.domain.Transaction;

import lombok.Builder;

@Builder
public record TransactionsInfo(
	Long income,
	Long expense,
	Long total,
	List<Transaction> transactions
) {

	public static TransactionsInfo from(List<Transaction> transactions) {
		Long income = transactions.stream()
			.filter(transaction -> transaction.getTransactionType() == INCOME)
			.map(Transaction::getAmount)
			.reduce(0L, Long::sum);

		Long expense = transactions.stream()
			.filter(transaction -> transaction.getTransactionType() == EXPENSE)
			.map(Transaction::getAmount)
			.reduce(0L, Long::sum);

		Long total = income - expense;

		return TransactionsInfo.builder()
			.income(income)
			.expense(expense)
			.total(total)
			.transactions(transactions)
			.build();
	}
}
