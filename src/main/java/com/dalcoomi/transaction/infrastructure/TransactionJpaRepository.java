package com.dalcoomi.transaction.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionJpaRepository extends JpaRepository<TransactionJpaEntity, Long> {

}
