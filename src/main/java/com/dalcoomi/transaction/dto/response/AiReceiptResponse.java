package com.dalcoomi.transaction.dto.response;

import java.util.List;

import com.dalcoomi.transaction.dto.ReceiptInfo;

import lombok.Builder;

@Builder
public record AiReceiptResponse(
	String taskId,
	List<ReceiptInfo> transactions
) {

}
