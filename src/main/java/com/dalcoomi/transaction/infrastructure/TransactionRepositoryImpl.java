package com.dalcoomi.transaction.infrastructure;

import static com.dalcoomi.category.infrastructure.QCategoryJpaEntity.categoryJpaEntity;
import static com.dalcoomi.common.error.model.ErrorMessage.TRANSACTION_NOT_FOUND;
import static com.dalcoomi.common.jpa.DynamicQuery.generateEq;
import static com.dalcoomi.common.jpa.DynamicQuery.generateEqOrIsNull;
import static com.dalcoomi.member.infrastructure.QMemberJpaEntity.memberJpaEntity;
import static com.dalcoomi.transaction.infrastructure.QTransactionJpaEntity.transactionJpaEntity;
import static java.util.Objects.requireNonNull;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.dalcoomi.category.infrastructure.CategoryJpaEntity;
import com.dalcoomi.common.error.exception.NotFoundException;
import com.dalcoomi.member.infrastructure.MemberJpaEntity;
import com.dalcoomi.transaction.application.repository.TransactionRepository;
import com.dalcoomi.transaction.domain.Transaction;
import com.dalcoomi.transaction.dto.TransactionSearchCriteria;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TransactionRepositoryImpl implements TransactionRepository {

	private final TransactionJpaRepository transactionJpaRepository;
	private final JPAQueryFactory jpaQueryFactory;
	private final EntityManager entityManager;

	@Override
	public Transaction save(Transaction transaction) {
		TransactionJpaEntity transactionJpaEntity = convertToJpaEntity(transaction);

		return transactionJpaRepository.save(transactionJpaEntity).toModel();
	}

	@Override
	public List<Transaction> saveAll(List<Transaction> transactions) {
		List<TransactionJpaEntity> transactionJpaEntities = transactions.stream()
			.map(this::convertToJpaEntity)
			.toList();

		List<TransactionJpaEntity> savedEntities = transactionJpaRepository.saveAll(transactionJpaEntities);

		return savedEntities.stream().map(TransactionJpaEntity::toModel).toList();
	}

	private TransactionJpaEntity convertToJpaEntity(Transaction transaction) {
		System.out.println("=== convertToJpaEntity START ===");
		System.out.println("Transaction ID: " + transaction.getId());
		System.out.println("Creator: " + (transaction.getCreator() != null ? transaction.getCreator().getId() : "null"));
		System.out.println("Category ID: " + transaction.getCategory().getId());

		if (transaction.getId() != null) {
			System.out.println("Existing entity - using getReference()");
			// 기존 엔티티 업데이트: 연관 엔티티를 getReference()로 설정
			MemberJpaEntity creatorReference = transaction.getCreator() != null
				? entityManager.getReference(MemberJpaEntity.class, transaction.getCreator().getId())
				: null;

			CategoryJpaEntity categoryReference = entityManager.getReference(CategoryJpaEntity.class,
				transaction.getCategory().getId());

			System.out.println("Created references - Creator: " + creatorReference + ", Category: " + categoryReference);

			return TransactionJpaEntity.builder()
				.id(transaction.getId())
				.creator(creatorReference)
				.category(categoryReference)
				.teamId(transaction.getTeamId())
				.transactionDate(transaction.getTransactionDate())
				.content(transaction.getContent())
				.amount(transaction.getAmount() != null ? transaction.getAmount().toString() : null)
				.transactionType(transaction.getTransactionType())
				.deletedAt(transaction.getDeletedAt())
				.dataRetentionConsent(transaction.getDataRetentionConsent())
				.build();
		} else {
			System.out.println("New entity - using from()");
			// 새로운 엔티티 생성
			return TransactionJpaEntity.from(transaction);
		}
	}

	@Override
	public Transaction findById(Long transactionId) {
		return transactionJpaRepository.findById(transactionId)
			.orElseThrow(() -> new NotFoundException(TRANSACTION_NOT_FOUND))
			.toModel();
	}

	@Override
	public Page<Transaction> findAll(Pageable pageable) {
		List<TransactionJpaEntity> content = jpaQueryFactory
			.selectFrom(transactionJpaEntity)
			.join(transactionJpaEntity.creator, memberJpaEntity).fetchJoin()
			.join(transactionJpaEntity.category, categoryJpaEntity).fetchJoin()
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		Long total = jpaQueryFactory
			.select(transactionJpaEntity.count())
			.from(transactionJpaEntity)
			.fetchOne();

		List<Transaction> transactions = content.stream().map(TransactionJpaEntity::toModel).toList();

		return new PageImpl<>(transactions, pageable, requireNonNull(total));
	}

	@Override
	public List<Transaction> findTransactions(TransactionSearchCriteria criteria) {
		return jpaQueryFactory
			.selectFrom(transactionJpaEntity)
			.join(transactionJpaEntity.creator, memberJpaEntity).fetchJoin()
			.join(transactionJpaEntity.category, categoryJpaEntity).fetchJoin()
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
	public List<Transaction> findExpiredPersonalTransactions(LocalDateTime cutoffDate) {
		return jpaQueryFactory
			.selectFrom(transactionJpaEntity)
			.where(
				transactionJpaEntity.deletedAt.isNotNull(),
				transactionJpaEntity.deletedAt.lt(cutoffDate),
				transactionJpaEntity.teamId.isNull(),
				transactionJpaEntity.dataRetentionConsent.isNotNull()
			)
			.fetch()
			.stream()
			.map(TransactionJpaEntity::toModel)
			.toList();
	}

	@Override
	public List<Transaction> findExpiredAnonymizedPersonalTransactions(LocalDateTime cutoffDate) {
		return jpaQueryFactory
			.selectFrom(transactionJpaEntity)
			.where(
				transactionJpaEntity.creator.isNull(),
				transactionJpaEntity.teamId.isNull(),
				transactionJpaEntity.dataRetentionConsent.isTrue(),
				transactionJpaEntity.updatedAt.lt(cutoffDate)
			)
			.fetch()
			.stream()
			.map(TransactionJpaEntity::toModel)
			.toList();
	}

	@Override
	public void deleteByTeamId(Long groupId) {
		transactionJpaRepository.deleteAllByTeamId(groupId);
	}

	@Override
	public void deleteAll(List<Transaction> transactions) {
		List<Long> ids = transactions.stream().map(Transaction::getId).toList();

		transactionJpaRepository.deleteAllById(ids);
	}
}
