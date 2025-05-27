package com.dalcoomi.transaction.infrastructure;

import static com.dalcoomi.category.infrastructure.QCategoryJpaEntity.categoryJpaEntity;
import static com.dalcoomi.common.error.model.ErrorMessage.TRANSACTION_NOT_FOUND;
import static com.dalcoomi.member.infrastructure.QMemberJpaEntity.memberJpaEntity;
import static com.dalcoomi.transaction.infrastructure.QTransactionJpaEntity.transactionJpaEntity;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.dalcoomi.common.error.exception.NotFoundException;
import com.dalcoomi.transaction.application.repository.TransactionRepository;
import com.dalcoomi.transaction.domain.Transaction;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TransactionRepositoryImpl implements TransactionRepository {

	private final TransactionJpaRepository transactionJpaRepository;
	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public Transaction save(Transaction transaction) {
		return transactionJpaRepository.save(TransactionJpaEntity.from(transaction)).toModel();
	}

	@Override
	public List<Transaction> saveAll(List<Transaction> transactions) {
		List<TransactionJpaEntity> transactionJpaEntities = transactionJpaRepository.saveAll(
			transactions.stream().map(TransactionJpaEntity::from).toList());

		return transactionJpaEntities.stream().map(TransactionJpaEntity::toModel).toList();
	}

	@Override
	public Transaction findByIdAndCreatorId(Long transactionId, Long creatorId) {
		return transactionJpaRepository.findByIdAndCreatorIdAndDeletedAtIsNull(transactionId, creatorId)
			.orElseThrow(() -> new NotFoundException(TRANSACTION_NOT_FOUND)).toModel();
	}

	@Override
	public List<Transaction> findByCreatorIdAndYearAndMonth(Long creatorId, int year, int month) {
		LocalDateTime startDate = LocalDateTime.of(year, month, 1, 0, 0, 0);
		LocalDateTime endDate = startDate.plusMonths(1).minusNanos(1);

		List<TransactionJpaEntity> transactionJpaEntities = jpaQueryFactory
			.select(transactionJpaEntity)
			.from(transactionJpaEntity)
			.join(transactionJpaEntity.category, categoryJpaEntity)
			.join(transactionJpaEntity.creator, memberJpaEntity)
			.where(
				transactionJpaEntity.creator.id.eq(creatorId),
				transactionJpaEntity.transactionDate.between(startDate, endDate),
				transactionJpaEntity.deletedAt.isNull(),
				memberJpaEntity.deletedAt.isNull(),
				categoryJpaEntity.deletedAt.isNull()
			)
			.orderBy(transactionJpaEntity.transactionDate.desc())
			.fetch();

		return transactionJpaEntities.stream().map(TransactionJpaEntity::toModel).toList();
	}

	@Override
	public List<Transaction> findByTeamId(Long teamId) {
		return transactionJpaRepository.findAllByTeamIdAndDeletedAtIsNull(teamId).stream()
			.map(TransactionJpaEntity::toModel).toList();
	}

	@Override
	public void deleteByTeamId(Long groupId) {
		transactionJpaRepository.deleteAllByTeamId(groupId);
	}
}
