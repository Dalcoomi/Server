package com.dalcoomi.member.infrastructure;

import static jakarta.persistence.ConstraintMode.NO_CONSTRAINT;

import java.time.LocalDateTime;

import com.dalcoomi.common.encryption.EncryptedStringConverter;
import com.dalcoomi.common.jpa.BaseTimeEntity;
import com.dalcoomi.member.domain.SocialConnection;
import com.dalcoomi.member.domain.SocialType;

import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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

	@Convert(converter = EncryptedStringConverter.class)
	@Column(name = "social_email", nullable = false)
	private String socialEmail;

	@Column(name = "social_email_hash", nullable = false)
	private String socialEmailHash;

	@Convert(converter = EncryptedStringConverter.class)
	@Column(name = "social_id", nullable = false)
	private String socialId;

	@Column(name = "social_id_hash", nullable = false)
	private String socialIdHash;

	@Convert(converter = EncryptedStringConverter.class)
	@Column(name = "social_refresh_token", nullable = false)
	private String socialRefreshToken;

	@Enumerated(EnumType.STRING)
	@Column(name = "social_type", nullable = false)
	private SocialType socialType;

	@Column(name = "deleted_at", nullable = true)
	private LocalDateTime deletedAt;

	@Builder
	public SocialConnectionJpaEntity(Long id, MemberJpaEntity member, String socialEmail, String socialEmailHash,
		String socialId, String socialIdHash, String socialRefreshToken, SocialType socialType,
		LocalDateTime deletedAt) {
		this.id = id;
		this.member = member;
		this.socialEmail = socialEmail;
		this.socialEmailHash = socialEmailHash;
		this.socialId = socialId;
		this.socialIdHash = socialIdHash;
		this.socialRefreshToken = socialRefreshToken;
		this.socialType = socialType;
		this.deletedAt = deletedAt;
	}

	public static SocialConnectionJpaEntity from(SocialConnection socialConnection) {
		return SocialConnectionJpaEntity.builder()
			.id(socialConnection.getId())
			.member(MemberJpaEntity.from(socialConnection.getMember()))
			.socialEmail(socialConnection.getSocialEmail())
			.socialEmailHash(socialConnection.getSocialEmailHash())
			.socialId(socialConnection.getSocialId())
			.socialIdHash(socialConnection.getSocialIdHash())
			.socialRefreshToken(socialConnection.getSocialRefreshToken())
			.socialType(socialConnection.getSocialType())
			.deletedAt(socialConnection.getDeletedAt())
			.build();
	}

	public SocialConnection toModel() {
		return SocialConnection.builder()
			.id(this.id)
			.member(this.member.toModel())
			.socialEmail(this.socialEmail)
			.socialEmailHash(this.socialEmailHash)
			.socialId(this.socialId)
			.socialIdHash(this.socialIdHash)
			.socialRefreshToken(this.socialRefreshToken)
			.socialType(this.socialType)
			.deletedAt(this.deletedAt)
			.build();
	}
}
