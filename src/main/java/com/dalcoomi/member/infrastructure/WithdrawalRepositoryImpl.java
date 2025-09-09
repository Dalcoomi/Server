package com.dalcoomi.member.infrastructure;

import static com.dalcoomi.common.error.model.ErrorMessage.WITHDRAWAL_NOT_FOUND;

import org.springframework.stereotype.Repository;

import com.dalcoomi.common.error.exception.NotFoundException;
import com.dalcoomi.member.application.repository.WithdrawalRepository;
import com.dalcoomi.member.domain.Withdrawal;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class WithdrawalRepositoryImpl implements WithdrawalRepository {

	private final WithdrawalJpaRepository withdrawalJpaRepository;

	@Override
	public Withdrawal save(Withdrawal withdrawal) {
		return withdrawalJpaRepository.save(WithdrawalJpaEntity.from(withdrawal)).toModel();
	}

	@Override
	public Withdrawal findById(Long withdrawalId) {
		return withdrawalJpaRepository.findById(withdrawalId)
			.orElseThrow(() -> new NotFoundException(WITHDRAWAL_NOT_FOUND))
			.toModel();
	}
}
