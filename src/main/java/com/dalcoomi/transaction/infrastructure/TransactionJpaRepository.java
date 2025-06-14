package com.dalcoomi.transaction.infrastructure;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionJpaRepository extends JpaRepository<TransactionJpaEntity, Long> {

	Optional<TransactionJpaEntity> findByIdAndDeletedAtIsNull(Long transactionId);

	void deleteAllByTeamId(Long groupId);
}
