package com.dalcoomi.transaction.infrastructure;

import static com.dalcoomi.category.infrastructure.QCategoryJpaEntity.categoryJpaEntity;
import static com.dalcoomi.common.error.model.ErrorMessage.TRANSACTION_NOT_FOUND;
import static com.dalcoomi.common.jpa.DynamicQuery.generateEq;
import static com.dalcoomi.common.jpa.DynamicQuery.generateEqOrIsNull;
import static com.dalcoomi.member.infrastructure.QMemberJpaEntity.memberJpaEntity;
import static com.dalcoomi.transaction.infrastructure.QTransactionJpaEntity.transactionJpaEntity;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.dalcoomi.common.error.exception.NotFoundException;
import com.dalcoomi.transaction.application.repository.TransactionRepository;
import com.dalcoomi.transaction.domain.Transaction;
import com.dalcoomi.transaction.dto.TransactionSearchCriteria;
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
	public Transaction findById(Long transactionId) {
		return transactionJpaRepository.findByIdAndDeletedAtIsNull(transactionId)
			.orElseThrow(() -> new NotFoundException(TRANSACTION_NOT_FOUND)).toModel();
	}

	@Override
	public List<Transaction> findTransactions(TransactionSearchCriteria criteria) {
		return jpaQueryFactory
			.selectFrom(transactionJpaEntity)
			.join(transactionJpaEntity.creator, memberJpaEntity)
			.join(transactionJpaEntity.category, categoryJpaEntity)
			.where(
				generateEqOrIsNull(criteria.teamId(), transactionJpaEntity.teamId::eq,
					transactionJpaEntity.teamId.isNull()),
				generateEq(criteria.memberId(), transactionJpaEntity.creator.id::eq),
				generateEq(criteria.year(), transactionJpaEntity.transactionDate.year()::eq),
				generateEq(criteria.month(), transactionJpaEntity.transactionDate.month()::eq),
				generateEq(criteria.categoryName(), transactionJpaEntity.category.name::eq),
				generateEq(criteria.creatorNickname(), transactionJpaEntity.creator.nickname::eq),
				transactionJpaEntity.deletedAt.isNull(),
				memberJpaEntity.deletedAt.isNull(),
				categoryJpaEntity.deletedAt.isNull()
			)
			.orderBy(transactionJpaEntity.transactionDate.desc())
			.fetch()
			.stream().map(TransactionJpaEntity::toModel).toList();
	}

	@Override
	public void deleteByTeamId(Long groupId) {
		transactionJpaRepository.deleteAllByTeamId(groupId);
	}
}
