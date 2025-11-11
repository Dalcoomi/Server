package com.dalcoomi.transaction.dto.request;

import java.util.List;

import com.dalcoomi.transaction.dto.ReceiptInfo;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record ReceiptCallbackRequest(
	@NotBlank(message = "taskId는 필수입니다.")
	String taskId,
	List<ReceiptInfo> transactions
) {

}
