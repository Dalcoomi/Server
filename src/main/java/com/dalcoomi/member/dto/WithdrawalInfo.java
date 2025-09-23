package com.dalcoomi.member.dto;

import java.util.Map;

import com.dalcoomi.member.domain.WithdrawalType;

import lombok.Builder;

@Builder
public record WithdrawalInfo(
	WithdrawalType withdrawalType,
	String otherReason,
	Map<Long, String> teamToNextLeaderMap,
	boolean softDelete,
	Boolean dataRetentionConsent
) {

}
