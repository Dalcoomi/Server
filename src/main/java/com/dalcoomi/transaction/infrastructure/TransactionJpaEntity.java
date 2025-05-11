package com.dalcoomi.transaction.infrastructure;

import static jakarta.persistence.ConstraintMode.NO_CONSTRAINT;

import java.time.LocalDateTime;

import com.dalcoomi.category.infrastructure.CategoryJpaEntity;
import com.dalcoomi.common.jpa.BaseTimeEntity;
import com.dalcoomi.member.infrastructure.MemberJpaEntity;
import com.dalcoomi.transaction.domain.Transaction;
import com.dalcoomi.transaction.domain.TransactionType;

import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "transaction")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TransactionJpaEntity extends BaseTimeEntity {

	@Id
	@Tsid
	@Column(name = "id", nullable = false, unique = true)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "creator_id", nullable = false, foreignKey = @ForeignKey(NO_CONSTRAINT))
	private MemberJpaEntity member;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id", nullable = false, foreignKey = @ForeignKey(NO_CONSTRAINT))
	private CategoryJpaEntity category;

	@Column(name = "group_id", nullable = true)
	private Long groupId;

	@Column(name = "transaction_date", nullable = false)
	private LocalDateTime transactionDate;

	@Column(name = "content", nullable = true)
	private String content;

	@Column(name = "amount", nullable = false)
	private Long amount;

	@Column(name = "deleted_at", nullable = true)
	private LocalDateTime deletedAt;

	@Enumerated(EnumType.STRING)
	@Column(name = "transaction_type", nullable = false)
	private TransactionType transactionType;

	@Builder
	public TransactionJpaEntity(Long id, MemberJpaEntity member, CategoryJpaEntity category, Long groupId,
		LocalDateTime transactionDate, String content, Long amount, TransactionType transactionType,
		LocalDateTime deletedAt) {
		this.id = id;
		this.member = member;
		this.category = category;
		this.groupId = groupId;
		this.transactionDate = transactionDate;
		this.content = content;
		this.amount = amount;
		this.transactionType = transactionType;
		this.deletedAt = deletedAt;
	}

	public static TransactionJpaEntity from(Transaction transaction) {
		return TransactionJpaEntity.builder()
			.id(transaction.getId())
			.member(MemberJpaEntity.from(transaction.getMember()))
			.category(CategoryJpaEntity.from(transaction.getCategory()))
			.groupId(transaction.getGroupId())
			.transactionDate(transaction.getTransactionDate())
			.content(transaction.getContent())
			.amount(transaction.getAmount())
			.transactionType(transaction.getTransactionType())
			.deletedAt(transaction.getDeletedAt())
			.build();
	}

	public Transaction toModel() {
		return Transaction.builder()
			.id(this.id)
			.member(this.member.toModel())
			.category(this.category.toModel())
			.groupId(this.groupId)
			.transactionDate(this.transactionDate)
			.content(this.content)
			.amount(this.amount)
			.transactionType(this.transactionType)
			.deletedAt(this.deletedAt)
			.build();
	}
}
