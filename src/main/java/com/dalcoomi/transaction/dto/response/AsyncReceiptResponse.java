package com.dalcoomi.transaction.dto.response;

import lombok.Builder;

@Builder
public record AsyncReceiptResponse(
	String taskId,
	String status
) {

	public static AsyncReceiptResponse from(String taskId) {
		return AsyncReceiptResponse.builder()
			.taskId(taskId)
			.status("pending")
			.build();
	}
}
