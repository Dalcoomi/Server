package com.dalcoomi.transaction.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record BulkTransactionRequest(
	@NotNull(message = "작업 ID는 필수입니다.")
	String taskId,

	@NotNull(message = "거래 내역은 필수입니다.")
	@NotEmpty(message = "최소 하나의 거래 내역이 필요합니다.")
	@Valid
	List<TransactionRequest> transactions
) {

}
