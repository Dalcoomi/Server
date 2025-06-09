package com.dalcoomi.transaction.infrastructure;

import static jakarta.persistence.ConstraintMode.NO_CONSTRAINT;
import static jakarta.persistence.GenerationType.IDENTITY;

import java.time.LocalDateTime;

import com.dalcoomi.category.infrastructure.CategoryJpaEntity;
import com.dalcoomi.common.jpa.BaseTimeEntity;
import com.dalcoomi.member.infrastructure.MemberJpaEntity;
import com.dalcoomi.transaction.domain.Transaction;
import com.dalcoomi.transaction.domain.TransactionType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
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
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", nullable = false, unique = true)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "creator_id", nullable = false, foreignKey = @ForeignKey(NO_CONSTRAINT))
	private MemberJpaEntity creator;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id", nullable = false, foreignKey = @ForeignKey(NO_CONSTRAINT))
	private CategoryJpaEntity category;

	@Column(name = "team_id", nullable = true)
	private Long teamId;

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
	public TransactionJpaEntity(Long id, MemberJpaEntity creator, CategoryJpaEntity category, Long teamId,
		LocalDateTime transactionDate, String content, Long amount, TransactionType transactionType,
		LocalDateTime deletedAt) {
		this.id = id;
		this.creator = creator;
		this.category = category;
		this.teamId = teamId;
		this.transactionDate = transactionDate;
		this.content = content;
		this.amount = amount;
		this.transactionType = transactionType;
		this.deletedAt = deletedAt;
	}

	public static TransactionJpaEntity from(Transaction transaction) {
		return TransactionJpaEntity.builder()
			.id(transaction.getId())
			.creator(MemberJpaEntity.from(transaction.getCreator()))
			.category(CategoryJpaEntity.from(transaction.getCategory()))
			.teamId(transaction.getTeamId())
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
			.creator(this.creator.toModel())
			.category(this.category.toModel())
			.teamId(this.teamId)
			.transactionDate(this.transactionDate)
			.content(this.content)
			.amount(this.amount)
			.transactionType(this.transactionType)
			.deletedAt(this.deletedAt)
			.build();
	}
}
