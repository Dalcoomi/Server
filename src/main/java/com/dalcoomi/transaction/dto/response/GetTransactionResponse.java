package com.dalcoomi.transaction.dto.response;

import java.time.LocalDateTime;

import com.dalcoomi.transaction.domain.Transaction;
import com.dalcoomi.transaction.domain.TransactionType;

import lombok.Builder;

@Builder
public record GetTransactionResponse(
	Long transactionId,
	String creatorNickname,
	Long amount,
	String content,
	LocalDateTime transactionDate,
	TransactionType transactionType,
	Long categoryId,
	String categoryName,
	String iconUrl
) {

	public static GetTransactionResponse from(Transaction transaction) {
		return GetTransactionResponse.builder()
			.transactionId(transaction.getId())
			.creatorNickname(transaction.getCreator().getNickname())
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
