package com.dalcoomi.member.infrastructure;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WithdrawalJpaRepository extends JpaRepository<WithdrawalJpaEntity, Long> {

	Optional<WithdrawalJpaEntity> findByMemberId(Long memberId);
}
