package com.dalcoomi.transaction.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.dalcoomi.transaction.domain.Transaction;
import com.dalcoomi.transaction.domain.TransactionType;
import com.dalcoomi.transaction.dto.TransactionsInfo;

import lombok.Builder;

@Builder
public record GetTransactionsResponse(
	Long income,
	Long expense,
	Long total,
	List<GetMyTransactionResponseItem> transactions
) {

	public static GetTransactionsResponse from(TransactionsInfo transactionsInfo) {
		List<GetMyTransactionResponseItem> transactions = transactionsInfo.transactions().stream()
			.map(GetMyTransactionResponseItem::from)
			.toList();

		return GetTransactionsResponse.builder()
			.income(transactionsInfo.income())
			.expense(transactionsInfo.expense())
			.total(transactionsInfo.total())
			.transactions(transactions)
			.build();
	}

	@Builder
	public record GetMyTransactionResponseItem(
		Long transactionId,
		String creatorNickname,
		String creatorProfileImageUrl,
		String categoryName,
		LocalDateTime transactionDate,
		String content,
		Long amount,
		TransactionType transactionType
	) {

		public static GetMyTransactionResponseItem from(Transaction transaction) {
			return GetMyTransactionResponseItem.builder()
				.transactionId(transaction.getId())
				.creatorNickname(transaction.getCreator().getNickname())
				.creatorProfileImageUrl(transaction.getCreator().getProfileImageUrl())
				.categoryName(transaction.getCategory().getName())
				.transactionDate(transaction.getTransactionDate())
				.content(transaction.getContent())
				.amount(transaction.getAmount())
				.transactionType(transaction.getTransactionType())
				.build();
		}
	}
}
