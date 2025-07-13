package com.dalcoomi.transaction.dto.request;

import java.util.List;

import com.dalcoomi.transaction.dto.ReceiptInfo;

import lombok.Builder;

@Builder
public record SendReceiptTransactions(
	String taskId,
	List<ReceiptInfo> transactions
) {

}
