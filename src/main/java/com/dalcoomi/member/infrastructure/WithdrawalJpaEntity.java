package com.dalcoomi.member.infrastructure;

import static jakarta.persistence.ConstraintMode.NO_CONSTRAINT;

import java.time.LocalDateTime;

import com.dalcoomi.common.jpa.BaseTimeEntity;
import com.dalcoomi.member.domain.Withdrawal;
import com.dalcoomi.member.domain.WithdrawalType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
@Table(name = "withdrawal")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WithdrawalJpaEntity extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, unique = true)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false, foreignKey = @ForeignKey(NO_CONSTRAINT))
	private MemberJpaEntity member;

	@Enumerated(EnumType.STRING)
	@Column(name = "withdrawal_type", nullable = false)
	private WithdrawalType withdrawalType;

	@Column(name = "withdrawal_reason", nullable = true)
	private String otherReason;

	@Column(name = "withdrawal_date", nullable = false)
	private LocalDateTime withdrawalDate;

	@Builder
	public WithdrawalJpaEntity(Long id, MemberJpaEntity member, WithdrawalType withdrawalType, String otherReason,
		LocalDateTime withdrawalDate) {
		this.id = id;
		this.member = member;
		this.withdrawalType = withdrawalType;
		this.otherReason = otherReason;
		this.withdrawalDate = withdrawalDate;
	}

	public static WithdrawalJpaEntity from(Withdrawal withdrawal) {
		return WithdrawalJpaEntity.builder()
			.id(withdrawal.getId())
			.member(MemberJpaEntity.from(withdrawal.getMember()))
			.withdrawalType(withdrawal.getWithdrawalType())
			.otherReason(withdrawal.getOtherReason())
			.withdrawalDate(withdrawal.getWithdrawalDate())
			.build();
	}

	public Withdrawal toModel() {
		return Withdrawal.builder()
			.id(this.id)
			.member(this.member.toModel())
			.withdrawalType(this.withdrawalType)
			.otherReason(this.otherReason)
			.withdrawalDate(this.withdrawalDate)
			.build();
	}
}
