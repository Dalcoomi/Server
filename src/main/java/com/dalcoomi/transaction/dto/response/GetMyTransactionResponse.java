package com.dalcoomi.transaction.dto.response;

import java.time.LocalDateTime;

import com.dalcoomi.transaction.domain.Transaction;
import com.dalcoomi.transaction.domain.TransactionType;

import lombok.Builder;

@Builder
public record GetMyTransactionResponse(
	Long transactionId,
	Long amount,
	String content,
	LocalDateTime transactionDate,
	TransactionType transactionType,
	Long categoryId,
	String categoryName,
	String iconUrl
) {

	public static GetMyTransactionResponse from(Transaction transaction) {
		return GetMyTransactionResponse.builder()
			.transactionId(transaction.getId())
			.amount(transaction.getAmount())
			.content(transaction.getContent())
			.transactionDate(transaction.getTransactionDate())
			.transactionType(transaction.getTransactionType())
			.categoryId(transaction.getCategory().getId())
			.categoryName(transaction.getCategory().getName())
			.iconUrl(transaction.getCategory().getIconUrl())
			.build();
	}
}
