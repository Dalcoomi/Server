package com.dalcoomi.transaction.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;

@Builder
public record ReceiptInfo(
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	LocalDate date,
	String categoryName,
	String content,
	Long amount
) {

}
