package com.dalcoomi.member.dto.request;

import java.util.List;

import com.dalcoomi.member.domain.WithdrawalType;
import com.dalcoomi.member.dto.LeaderTransferInfo;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record WithdrawRequest(
	@NotNull
	WithdrawalType withdrawalType,

	@Size(max = 50, message = "기타 사유 내용은 최대 50자입니다.")
	String otherReason,

	List<LeaderTransferInfo> leaderTransferInfos,

	@NotNull
	Boolean softDelete,

	Boolean dataRetentionConsent
) {

}
