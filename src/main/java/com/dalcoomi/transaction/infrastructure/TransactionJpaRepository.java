package com.dalcoomi.transaction.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionJpaRepository extends JpaRepository<TransactionJpaEntity, Long> {

	Optional<TransactionJpaEntity> findByIdAndCreatorIdAndDeletedAtIsNull(Long transactionId, Long creatorId);

	void deleteAllByTeamId(Long groupId);

	List<TransactionJpaEntity> findAllByTeamIdAndDeletedAtIsNull(Long teamId);
}
