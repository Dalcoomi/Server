package com.dalcoomi.member.application.repository;

import com.dalcoomi.member.domain.Withdrawal;

public interface WithdrawalRepository {

	Withdrawal save(Withdrawal withdrawal);

	Withdrawal findByMemberId(Long memberId);
}
