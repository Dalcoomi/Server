package com.dalcoomi.transaction.infrastructure;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TransactionJpaRepository extends JpaRepository<TransactionJpaEntity, Long> {

	void deleteAllByTeamId(Long groupId);

	@Query("""
		SELECT t
		FROM TransactionJpaEntity t
		WHERE (t.content IS NOT NULL AND LENGTH(t.content) < 100 AND t.content NOT LIKE '%==%')
		OR (t.amount IS NOT NULL AND LENGTH(t.amount) < 20 AND t.amount NOT LIKE '%==%')
		""")
	List<TransactionJpaEntity> findPlainTextTransactions(Pageable pageable);
}
