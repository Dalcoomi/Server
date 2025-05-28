package com.dalcoomi.transaction.dto;

import org.springframework.lang.Nullable;

import lombok.Builder;

@Builder
public record TransactionSearchCriteria(
	Long requesterId,
	Long memberId,
	Long teamId,
	Integer year,
	Integer month
) {

	public static TransactionSearchCriteria of(Long memberId, @Nullable Long teamId, Integer year, Integer month) {
		return TransactionSearchCriteria.builder()
			.requesterId(memberId)
			.memberId(teamId == null ? memberId : null)
			.teamId(teamId)
			.year(year)
			.month(month)
			.build();
	}
}
