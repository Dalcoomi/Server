package com.dalcoomi.member.domain;

import static com.dalcoomi.common.error.model.ErrorMessage.MEMBER_INVALID_EMAIL;
import static com.dalcoomi.common.error.model.ErrorMessage.MEMBER_INVALID_GENDER;
import static com.dalcoomi.common.error.model.ErrorMessage.MEMBER_INVALID_NAME;
import static com.dalcoomi.common.error.model.ErrorMessage.MEMBER_INVALID_NICKNAME;
import static com.dalcoomi.common.error.model.ErrorMessage.MEMBER_INVALID_PROFILE_IMAGE_URL;
import static com.dalcoomi.member.constant.MemberConstants.EMAIL_LENGTH;
import static com.dalcoomi.member.constant.MemberConstants.GENDER_LENGTH;
import static com.dalcoomi.member.constant.MemberConstants.NAME_MAX_LENGTH;
import static com.dalcoomi.member.constant.MemberConstants.NAME_MIN_LENGTH;
import static com.dalcoomi.member.constant.MemberConstants.NICKNAME_MAX_LENGTH;
import static com.dalcoomi.member.constant.MemberConstants.NICKNAME_MIN_LENGTH;
import static com.dalcoomi.member.constant.MemberConstants.PROFILE_IMAGE_URL_LENGTH;
import static io.micrometer.common.util.StringUtils.isBlank;
import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
public class Member {

	private final Long id;
	private final Boolean serviceAgreement;
	private final Boolean collectionAgreement;
	private final LocalDateTime createdAt;
	private final LocalDateTime updatedAt;
	private String email;
	private String emailHash;
	private String name;
	private String nameHash;
	private String nickname;
	private LocalDate birthday;
	private String birthdayHash;
	private String gender;
	private String genderHash;
	private String profileImageUrl;
	private Boolean aiLearningAgreement;
	private LocalDateTime lastLoginAt;
	private LocalDateTime deletedAt;

	@Builder
	public Member(Long id, String email, String emailHash, String name, String nameHash, String nickname,
		LocalDate birthday, String birthdayHash, String gender, String genderHash, String profileImageUrl,
		Boolean serviceAgreement, Boolean collectionAgreement, Boolean aiLearningAgreement,
		LocalDateTime lastLoginAt, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt) {
		this.id = id;
		this.email = validateEmail(email);
		this.emailHash = emailHash;
		this.name = validateName(name);
		this.nameHash = nameHash;
		this.nickname = validateNickname(nickname);
		this.birthday = birthday;
		this.birthdayHash = birthdayHash;
		this.gender = validateGender(gender);
		this.genderHash = genderHash;
		this.profileImageUrl = validateProfileImageUrl(profileImageUrl);
		this.serviceAgreement = requireNonNull(serviceAgreement);
		this.collectionAgreement = requireNonNull(collectionAgreement);
		this.aiLearningAgreement = aiLearningAgreement;
		this.lastLoginAt = lastLoginAt;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.deletedAt = deletedAt;
	}

	public void skipValidationNickname(String nickname) {
		this.nickname = nickname;
	}

	public void updateEmail(String email) {
		this.email = email;
	}

	public void updateProfile(String name, String nickname, LocalDate birthday, String gender) {
		this.name = validateName(name);
		this.birthday = birthday;
		this.gender = validateGender(gender);

		skipValidationNickname(nickname);
	}

	public void updateProfileImageUrl(String profileImageUrl) {
		this.profileImageUrl = validateProfileImageUrl(profileImageUrl);
	}

	public void updateAiLearningAgreement(Boolean aiLearningAgreement) {
		this.aiLearningAgreement = aiLearningAgreement;
	}

	public void updateLoginTime(LocalDateTime loginTime) {
		this.lastLoginAt = loginTime;
	}

	public void softDelete() {
		this.deletedAt = LocalDateTime.now();
	}

	public void updateEmailForEncryption(String encryptedEmail, String emailHash) {
		this.email = encryptedEmail;
		this.emailHash = emailHash;
	}

	public void updateNameForEncryption(String encryptedName, String nameHash) {
		this.name = encryptedName;
		this.nameHash = nameHash;
	}

	public void updateBirthdayHashForEncryption(String birthdayHash) {
		this.birthdayHash = birthdayHash;
	}

	public void updateGenderForEncryption(String encryptedGender, String genderHash) {
		this.gender = encryptedGender;
		this.genderHash = genderHash;
	}

	private String validateEmail(String email) {
		if (isBlank(email) || email.length() > EMAIL_LENGTH) {
			throw new IllegalArgumentException(MEMBER_INVALID_EMAIL.getMessage());
		}

		return email;
	}

	private String validateName(String name) {
		if (isBlank(name) || name.length() < NAME_MIN_LENGTH || name.length() > NAME_MAX_LENGTH) {
			throw new IllegalArgumentException(MEMBER_INVALID_NAME.getMessage());
		}

		return name;
	}

	private String validateNickname(String nickname) {
		if (isBlank(nickname) || nickname.length() < NICKNAME_MIN_LENGTH || nickname.length() > NICKNAME_MAX_LENGTH) {
			throw new IllegalArgumentException(MEMBER_INVALID_NICKNAME.getMessage());
		}

		return nickname;
	}

	private String validateGender(String gender) {
		if (gender != null && gender.length() > GENDER_LENGTH) {
			throw new IllegalArgumentException(MEMBER_INVALID_GENDER.getMessage());
		}

		return gender;
	}

	private String validateProfileImageUrl(String profileImageUrl) {
		if (isBlank(profileImageUrl) || profileImageUrl.length() > PROFILE_IMAGE_URL_LENGTH) {
			throw new IllegalArgumentException(MEMBER_INVALID_PROFILE_IMAGE_URL.getMessage());
		}

		return profileImageUrl;
	}
}
