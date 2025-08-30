package com.dalcoomi.transaction.dto.request;

import java.time.LocalDateTime;

import com.dalcoomi.transaction.domain.TransactionType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record TransactionRequest(
	Long teamId,

	@NotNull(message = "금액은 필수입니다.")
	@Positive(message = "자연수를 입력해주세요.")
	Long amount,

	@Size(max = 20, message = "거래 내역 내용은 최대 20자입니다.")
	String content,

	LocalDateTime transactionDate,

	@NotNull(message = "거래 타입은 필수입니다.")
	TransactionType transactionType,

	@NotNull(message = "카테고리 ID는 필수입니다.")
	Long categoryId,

	Boolean synchronizeTransaction
) {

}
