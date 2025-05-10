package com.dalcoomi.transaction.dto;

import java.time.LocalDateTime;

import com.dalcoomi.transaction.domain.OwnerType;
import com.dalcoomi.transaction.domain.Transaction;
import com.dalcoomi.transaction.domain.TransactionType;

import lombok.Builder;

@Builder
public record TransactionInfo(
	String creatorNickname,
	String categoryName,
	Long groupId,
	LocalDateTime transactionDate,
	String content,
	Long amount,
	TransactionType transactionType,
	OwnerType ownerType,
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
			.ownerType(transaction.getOwnerType())
			.deletedAt(transaction.getDeletedAt())
			.build();
	}
}
