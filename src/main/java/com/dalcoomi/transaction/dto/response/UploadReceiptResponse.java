package com.dalcoomi.transaction.dto.response;

import java.time.LocalDate;
import java.util.List;

import com.dalcoomi.transaction.dto.ReceiptInfo;

import lombok.Builder;

@Builder
public record UploadReceiptResponse(
	List<UploadReceiptResponseItem> transactions
) {

	public static UploadReceiptResponse from(List<ReceiptInfo> receiptInfos) {
		List<UploadReceiptResponseItem> transactions = receiptInfos.stream()
			.map(UploadReceiptResponseItem::from)
			.toList();

		return UploadReceiptResponse.builder()
			.transactions(transactions)
			.build();
	}

	@Builder
	public record UploadReceiptResponseItem(
		LocalDate transactionDate,
		String categoryName,
		String content,
		Long amount
	) {

		public static UploadReceiptResponseItem from(ReceiptInfo receiptInfo) {
			return UploadReceiptResponseItem.builder()
				.transactionDate(receiptInfo.date())
				.categoryName(receiptInfo.categoryName())
				.content(receiptInfo.content())
				.amount(receiptInfo.amount())
				.build();
		}
	}
}
