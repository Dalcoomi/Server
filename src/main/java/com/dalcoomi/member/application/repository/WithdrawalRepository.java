package com.dalcoomi.member.application.repository;

import java.util.List;

import com.dalcoomi.member.domain.Withdrawal;

public interface WithdrawalRepository {

	Withdrawal save(Withdrawal withdrawal);

	Withdrawal findById(Long withdrawalId);

	List<Withdrawal> findAll();
}
