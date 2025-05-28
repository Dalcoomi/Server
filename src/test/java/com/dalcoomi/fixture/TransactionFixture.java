package com.dalcoomi.fixture;

import static com.dalcoomi.transaction.domain.TransactionType.EXPENSE;

import java.time.LocalDateTime;

import com.dalcoomi.category.domain.Category;
import com.dalcoomi.member.domain.Member;
import com.dalcoomi.transaction.domain.Transaction;

public final class TransactionFixture {

	public static Transaction getTransactionWithExpense1(Member member, Category category) {
		LocalDateTime transactionDate = LocalDateTime.of(2025, 3, 1, 10, 0);
		String content = "아침 식사";
		Long amount = 10000L;

		return Transaction.builder()
			.creator(member)
			.category(category)
			.transactionDate(transactionDate)
			.content(content)
			.amount(amount)
			.transactionType(EXPENSE)
			.build();
	}

	public static Transaction getTransactionWithExpense2(Member member, Category category) {
		LocalDateTime transactionDate = LocalDateTime.of(2025, 3, 11, 12, 30);
		String content = "점심 식사";
		Long amount = 15000L;

		return Transaction.builder()
			.creator(member)
			.category(category)
			.transactionDate(transactionDate)
			.content(content)
			.amount(amount)
			.transactionType(EXPENSE)
			.build();
	}

	public static Transaction getTransactionWithExpense3(Member member, Category category) {
		LocalDateTime transactionDate = LocalDateTime.of(2025, 3, 12, 18, 0);
		String content = "저녁 식사";
		Long amount = 20000L;

		return Transaction.builder()
			.creator(member)
			.category(category)
			.transactionDate(transactionDate)
			.content(content)
			.amount(amount)
			.transactionType(EXPENSE)
			.build();
	}

	public static Transaction getTransactionWithExpense4(Member member, Category category) {
		LocalDateTime transactionDate = LocalDateTime.of(2025, 5, 5, 12, 0);
		String content = "5월 식사";
		Long amount = 12000L;

		return Transaction.builder()
			.creator(member)
			.category(category)
			.transactionDate(transactionDate)
			.content(content)
			.amount(amount)
			.transactionType(EXPENSE)
			.build();
	}

	public static Transaction getTeamTransactionWithExpense1(Member member, Long teamId, Category category) {
		LocalDateTime transactionDate = LocalDateTime.of(2025, 3, 15, 14, 0);
		String content = "5월 식사";
		Long amount = 22000L;

		return Transaction.builder()
			.creator(member)
			.category(category)
			.teamId(teamId)
			.transactionDate(transactionDate)
			.content(content)
			.amount(amount)
			.transactionType(EXPENSE)
			.build();
	}

	public static Transaction getTeamTransactionWithExpense2(Member member, Long teamId, Category category) {
		LocalDateTime transactionDate = LocalDateTime.of(2025, 3, 15, 16, 0);
		String content = "5월 식사";
		Long amount = 20000L;

		return Transaction.builder()
			.creator(member)
			.category(category)
			.teamId(teamId)
			.transactionDate(transactionDate)
			.content(content)
			.amount(amount)
			.transactionType(EXPENSE)
			.build();
	}

	public static Transaction getTeamTransactionWithExpense3(Member member, Long teamId, Category category) {
		LocalDateTime transactionDate = LocalDateTime.of(2025, 3, 15, 18, 0);
		String content = "5월 식사";
		Long amount = 32000L;

		return Transaction.builder()
			.creator(member)
			.category(category)
			.teamId(teamId)
			.transactionDate(transactionDate)
			.content(content)
			.amount(amount)
			.transactionType(EXPENSE)
			.build();
	}

	public static Transaction getTeamTransactionWithExpense4(Member member, Long teamId, Category category) {
		LocalDateTime transactionDate = LocalDateTime.of(2025, 5, 15, 20, 0);
		String content = "5월 식사";
		Long amount = 52000L;

		return Transaction.builder()
			.creator(member)
			.category(category)
			.teamId(teamId)
			.transactionDate(transactionDate)
			.content(content)
			.amount(amount)
			.transactionType(EXPENSE)
			.build();
	}
}
