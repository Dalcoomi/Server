package com.dalcoomi.category.infrastructure;

import static jakarta.persistence.ConstraintMode.NO_CONSTRAINT;
import static jakarta.persistence.GenerationType.IDENTITY;

import java.time.LocalDateTime;

import com.dalcoomi.category.domain.Category;
import com.dalcoomi.category.domain.OwnerType;
import com.dalcoomi.common.jpa.BaseTimeEntity;
import com.dalcoomi.member.infrastructure.MemberJpaEntity;
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
@Table(name = "category")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoryJpaEntity extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", nullable = false, unique = true)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "creator_id", nullable = false, foreignKey = @ForeignKey(NO_CONSTRAINT))
	private MemberJpaEntity member;

	@Column(name = "team_id", nullable = true)
	private Long teamId;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "icon_url", nullable = true)
	private String iconUrl;

	@Column(name = "is_active", nullable = false)
	private Boolean isActive;

	@Column(name = "deleted_at", nullable = true)
	private LocalDateTime deletedAt;

	@Enumerated(EnumType.STRING)
	@Column(name = "transaction_type", nullable = false)
	private TransactionType transactionType;

	@Enumerated(EnumType.STRING)
	@Column(name = "owner_type", nullable = false)
	private OwnerType ownerType;

	@Builder
	public CategoryJpaEntity(Long id, MemberJpaEntity member, Long teamId, String name, String iconUrl,
		Boolean isActive, TransactionType transactionType, OwnerType ownerType, LocalDateTime deletedAt) {
		this.id = id;
		this.member = member;
		this.teamId = teamId;
		this.name = name;
		this.iconUrl = iconUrl;
		this.isActive = isActive;
		this.transactionType = transactionType;
		this.ownerType = ownerType;
		this.deletedAt = deletedAt;
	}

	public static CategoryJpaEntity from(Category category) {
		return CategoryJpaEntity.builder()
			.id(category.getId())
			.member(MemberJpaEntity.from(category.getMember()))
			.teamId(category.getTeamId())
			.name(category.getName())
			.iconUrl(category.getIconUrl())
			.isActive(category.getIsActive())
			.transactionType(category.getTransactionType())
			.ownerType(category.getOwnerType())
			.deletedAt(category.getDeletedAt())
			.build();
	}

	public Category toModel() {
		return Category.builder()
			.id(this.id)
			.member(this.member.toModel())
			.teamId(this.teamId)
			.name(this.name)
			.iconUrl(this.iconUrl)
			.isActive(this.isActive)
			.transactionType(this.transactionType)
			.ownerType(this.ownerType)
			.deletedAt(this.deletedAt)
			.build();
	}
}
