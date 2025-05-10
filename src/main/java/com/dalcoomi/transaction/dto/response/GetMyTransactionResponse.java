package com.dalcoomi.transaction.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.dalcoomi.transaction.domain.TransactionType;
import com.dalcoomi.transaction.dto.TransactionInfo;

import lombok.Builder;

@Builder
public record GetMyTransactionResponse(
	List<GetMyTransactionResponseItem> transactions
) {

	public static GetMyTransactionResponse from(List<TransactionInfo> transactionInfos) {
		List<GetMyTransactionResponseItem> transactions = transactionInfos.stream()
			.map(GetMyTransactionResponseItem::from)
			.toList();

		return GetMyTransactionResponse.builder()
			.transactions(transactions)
			.build();
	}

	@Builder
	public record GetMyTransactionResponseItem(
		String creatorNickname,
		String categoryName,
		LocalDateTime transactionDate,
		String content,
		Long amount,
		TransactionType transactionType
	) {

		public static GetMyTransactionResponseItem from(TransactionInfo transactionInfo) {
			return GetMyTransactionResponseItem.builder()
				.creatorNickname(transactionInfo.creatorNickname())
				.categoryName(transactionInfo.categoryName())
				.transactionDate(transactionInfo.transactionDate())
				.content(transactionInfo.content())
				.amount(transactionInfo.amount())
				.transactionType(transactionInfo.transactionType())
				.build();
		}
	}
}