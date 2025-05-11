package com.dalcoomi.transaction.dto;

import java.time.LocalDateTime;

import com.dalcoomi.transaction.domain.Transaction;
import com.dalcoomi.transaction.domain.TransactionType;
import com.dalcoomi.transaction.dto.request.CreateMyTransactionRequest;

import lombok.Builder;

@Builder
public record TransactionInfo(
	String creatorNickname,
	Long categoryId,
	String categoryName,
	Long groupId,
	LocalDateTime transactionDate,
	String content,
	Long amount,
	TransactionType transactionType,
	LocalDateTime deletedAt
) {

	public static TransactionInfo from(Transaction transaction) {
		return TransactionInfo.builder()
			.creatorNickname(transaction.getMember().getNickname())
			.categoryName(transaction.getCategory().getName())
			.groupId(transaction.getGroupId())
			.transactionDate(transaction.getTransactionDate())
			.content(transaction.getContent())
			.amount(transaction.getAmount())
			.transactionType(transaction.getTransactionType())
			.deletedAt(transaction.getDeletedAt())
			.build();
	}

	public static TransactionInfo from(CreateMyTransactionRequest request) {
		return TransactionInfo.builder()
			.categoryId(request.categoryId())
			.amount(request.amount())
			.content(request.content())
			.transactionDate(request.transactionDate())
			.transactionType(request.transactionType())
			.build();
	}
}
