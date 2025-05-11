package com.dalcoomi.transaction.dto.request;

import java.time.LocalDateTime;

import com.dalcoomi.transaction.domain.TransactionType;

import jakarta.validation.constraints.NotNull;

public record CreateMyTransactionRequest(
	@NotNull(message = "카테고리 ID는 필수입니다.")
	Long categoryId,

	@NotNull(message = "금액은 필수입니다.")
	Long amount,

	String content,

	LocalDateTime transactionDate,

	@NotNull(message = "거래 타입은 필수입니다.")
	TransactionType transactionType
) {

}
