package com.dalcoomi.member.infrastructure;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.dalcoomi.common.jpa.BaseTimeEntity;
import com.dalcoomi.member.domain.Member;

import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.Column;
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

	@Column(name = "email", nullable = false)
	private String email;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "nickname", nullable = false)
	private String nickname;

	@Column(name = "birthday", nullable = true)
	private LocalDate birthday;

	@Column(name = "gender", nullable = true)
	private String gender;

	@Column(name = "profile_image_url", nullable = false)
	private String profileImageUrl;

	@Column(name = "service_agreement", nullable = false)
	private Boolean serviceAgreement;

	@Column(name = "collection_agreement", nullable = false)
	private Boolean collectionAgreement;

	@Column(name = "last_login_at", nullable = true)
	private LocalDateTime lastLoginAt;

	@Column(name = "deleted_at", nullable = true)
	private LocalDateTime deletedAt;

	@Builder
	public MemberJpaEntity(Long id, String email, String name, String nickname, LocalDate birthday, String gender,
		String profileImageUrl, Boolean serviceAgreement, Boolean collectionAgreement, LocalDateTime lastLoginAt,
		LocalDateTime deletedAt) {
		this.id = id;
		this.email = email;
		this.name = name;
		this.nickname = nickname;
		this.birthday = birthday;
		this.gender = gender;
		this.profileImageUrl = profileImageUrl;
		this.serviceAgreement = serviceAgreement;
		this.collectionAgreement = collectionAgreement;
		this.lastLoginAt = lastLoginAt;
		this.deletedAt = deletedAt;
	}

	public static MemberJpaEntity from(Member member) {
		return MemberJpaEntity.builder()
			.id(member.getId())
			.email(member.getEmail())
			.name(member.getName())
			.nickname(member.getNickname())
			.birthday(member.getBirthday())
			.gender(member.getGender())
			.profileImageUrl(member.getProfileImageUrl())
			.serviceAgreement(member.getServiceAgreement())
			.collectionAgreement(member.getCollectionAgreement())
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
			.lastLoginAt(this.lastLoginAt)
			.createdAt(getCreatedAt())
			.updatedAt(getUpdatedAt())
			.deletedAt(this.deletedAt)
			.build();

		member.skipValidationNickname(this.nickname);

		return member;
	}
}
