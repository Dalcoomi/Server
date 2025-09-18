package com.dalcoomi.member.domain;

import static com.dalcoomi.common.error.model.ErrorMessage.WITHDRAWAL_INVALID_OTHER_REASON;
import static com.dalcoomi.member.constant.MemberConstants.OTHER_REASON_LENGTH;
import static com.dalcoomi.member.domain.WithdrawalType.OTHER;
import static io.micrometer.common.util.StringUtils.isBlank;
import static java.util.Objects.requireNonNull;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
public class Withdrawal {

	private final Long id;
	private final WithdrawalType withdrawalType;
	private final String otherReason;
	private final LocalDateTime withdrawalDate;

	@Builder
	public Withdrawal(Long id, WithdrawalType withdrawalType, String otherReason,
		LocalDateTime withdrawalDate) {
		this.id = id;
		this.withdrawalType = requireNonNull(withdrawalType);
		this.otherReason = validateOtherReason(otherReason);
		this.withdrawalDate = requireNonNull(withdrawalDate);
	}

	private String validateOtherReason(String otherReason) {
		if (withdrawalType == OTHER) {
			if (isBlank(otherReason) || otherReason.length() > OTHER_REASON_LENGTH) {
				throw new IllegalArgumentException(WITHDRAWAL_INVALID_OTHER_REASON.getMessage());
			}

			return otherReason;
		}

		return null;
	}
}
