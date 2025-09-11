package com.dalcoomi.member.infrastructure;

import static jakarta.persistence.ConstraintMode.NO_CONSTRAINT;

import java.time.LocalDateTime;

import com.dalcoomi.common.jpa.BaseTimeEntity;
import com.dalcoomi.member.domain.SocialConnection;
import com.dalcoomi.member.domain.SocialType;

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
@Table(name = "social_connection")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SocialConnectionJpaEntity extends BaseTimeEntity {

	@Id
	@Tsid
	@Column(name = "id", nullable = false, unique = true)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false, foreignKey = @ForeignKey(NO_CONSTRAINT))
	private MemberJpaEntity member;

	@Column(name = "social_email", nullable = false)
	private String socialEmail;

	@Column(name = "social_id", nullable = false)
	private String socialId;

	@Enumerated(EnumType.STRING)
	@Column(name = "social_type", nullable = false)
	private SocialType socialType;

	@Column(name = "deleted_at", nullable = true)
	private LocalDateTime deletedAt;

	@Builder
	public SocialConnectionJpaEntity(Long id, MemberJpaEntity member, String socialEmail, String socialId,
		SocialType socialType, LocalDateTime deletedAt) {
		this.id = id;
		this.member = member;
		this.socialEmail = socialEmail;
		this.socialId = socialId;
		this.socialType = socialType;
		this.deletedAt = deletedAt;
	}

	public static SocialConnectionJpaEntity from(SocialConnection socialConnection) {
		return SocialConnectionJpaEntity.builder()
			.id(socialConnection.getId())
			.member(MemberJpaEntity.from(socialConnection.getMember()))
			.socialEmail(socialConnection.getSocialEmail())
			.socialId(socialConnection.getSocialId())
			.socialType(socialConnection.getSocialType())
			.deletedAt(socialConnection.getDeletedAt())
			.build();
	}

	public SocialConnection toModel() {
		return SocialConnection.builder()
			.id(this.id)
			.member(this.member.toModel())
			.socialEmail(this.socialEmail)
			.socialId(this.socialId)
			.socialType(this.socialType)
			.deletedAt(this.deletedAt)
			.build();
	}
}
