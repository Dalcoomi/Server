package com.dalcoomi.member.infrastructure;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.dalcoomi.common.encryption.EncryptedLocalDateConverter;
import com.dalcoomi.common.encryption.EncryptedStringConverter;
import com.dalcoomi.common.jpa.BaseTimeEntity;
import com.dalcoomi.member.domain.Member;

import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberJpaEntity extends BaseTimeEntity {

	@Id
	@Tsid
	@Column(name = "id", nullable = false, unique = true)
	private Long id;

	@Convert(converter = EncryptedStringConverter.class)
	@Column(name = "email", nullable = false)
	private String email;

	@Column(name = "email_hash", nullable = false)
	private String emailHash;

	@Convert(converter = EncryptedStringConverter.class)
	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "name_hash", nullable = false)
	private String nameHash;

	@Column(name = "nickname", nullable = false)
	private String nickname;

	@Convert(converter = EncryptedLocalDateConverter.class)
	@Column(name = "birthday", nullable = true)
	private LocalDate birthday;

	@Column(name = "birthday_hash", nullable = true)
	private String birthdayHash;

	@Convert(converter = EncryptedStringConverter.class)
	@Column(name = "gender", nullable = true)
	private String gender;

	@Column(name = "gender_hash", nullable = true)
	private String genderHash;

	@Column(name = "profile_image_url", nullable = false)
	private String profileImageUrl;

	@Column(name = "service_agreement", nullable = false)
	private Boolean serviceAgreement;

	@Column(name = "collection_agreement", nullable = false)
	private Boolean collectionAgreement;

	@Column(name = "ai_learning_agreement", nullable = true)
	private Boolean aiLearningAgreement;

	@Column(name = "last_login_at", nullable = true)
	private LocalDateTime lastLoginAt;

	@Column(name = "deleted_at", nullable = true)
	private LocalDateTime deletedAt;

	@Builder
	public MemberJpaEntity(Long id, String email, String emailHash, String name, String nameHash, String nickname,
		LocalDate birthday, String birthdayHash, String gender, String genderHash, String profileImageUrl,
		Boolean serviceAgreement, Boolean collectionAgreement, Boolean aiLearningAgreement, LocalDateTime lastLoginAt,
		LocalDateTime deletedAt) {
		this.id = id;
		this.email = email;
		this.emailHash = emailHash;
		this.name = name;
		this.nameHash = nameHash;
		this.nickname = nickname;
		this.birthday = birthday;
		this.birthdayHash = birthdayHash;
		this.gender = gender;
		this.genderHash = genderHash;
		this.profileImageUrl = profileImageUrl;
		this.serviceAgreement = serviceAgreement;
		this.collectionAgreement = collectionAgreement;
		this.aiLearningAgreement = aiLearningAgreement;
		this.lastLoginAt = lastLoginAt;
		this.deletedAt = deletedAt;
	}

	public static MemberJpaEntity from(Member member) {
		return MemberJpaEntity.builder()
			.id(member.getId())
			.email(member.getEmail())
			.emailHash("")
			.name(member.getName())
			.nameHash("")
			.nickname(member.getNickname())
			.birthday(member.getBirthday())
			.birthdayHash("")
			.gender(member.getGender())
			.genderHash("")
			.profileImageUrl(member.getProfileImageUrl())
			.serviceAgreement(member.getServiceAgreement())
			.collectionAgreement(member.getCollectionAgreement())
			.aiLearningAgreement(member.getAiLearningAgreement())
			.lastLoginAt(member.getLastLoginAt())
			.deletedAt(member.getDeletedAt())
			.build();
	}

	public Member toModel() {
		Member member = Member.builder()
			.id(this.id)
			.email(this.email)
			.name(this.name)
			.nickname("dummy")
			.birthday(this.birthday)
			.gender(this.gender)
			.profileImageUrl(this.profileImageUrl)
			.serviceAgreement(this.serviceAgreement)
			.collectionAgreement(this.collectionAgreement)
			.aiLearningAgreement(this.aiLearningAgreement)
			.lastLoginAt(this.lastLoginAt)
			.createdAt(getCreatedAt())
			.updatedAt(getUpdatedAt())
			.deletedAt(this.deletedAt)
			.build();

		member.skipValidationNickname(this.nickname);

		return member;
	}
}
