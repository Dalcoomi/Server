package com.dalcoomi.member.domain;

import static com.dalcoomi.common.error.model.ErrorMessage.MEMBER_INVALID_EMAIL;
import static com.dalcoomi.common.error.model.ErrorMessage.MEMBER_INVALID_GENDER;
import static com.dalcoomi.common.error.model.ErrorMessage.MEMBER_INVALID_NAME;
import static com.dalcoomi.common.error.model.ErrorMessage.MEMBER_INVALID_NICKNAME;
import static com.dalcoomi.common.error.model.ErrorMessage.MEMBER_INVALID_PROFILE_IMAGE_URL;
import static io.micrometer.common.util.StringUtils.isBlank;
import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
public class Member {

	private static final int EMAIL_LENGTH = 100;
	private static final int NAME_LENGTH = 30;
	private static final int NICKNAME_LENGTH = 10;
	private static final int GENDER_LENGTH = 10;
	private static final int PROFILE_IMAGE_URL_LENGTH = 255;

	private final Long id;
	private final String email;
	private final String name;
	private final String nickname;
	private final LocalDate birthday;
	private final String gender;
	private final String profileImageUrl;
	private final Boolean serviceAgreement;
	private final Boolean collectionAgreement;
	private final LocalDateTime deletedAt;

	@Builder
	public Member(Long id, String email, String name, String nickname, LocalDate birthday, String gender,
		String profileImageUrl, Boolean serviceAgreement, Boolean collectionAgreement, LocalDateTime deletedAt) {
		this.id = id;
		this.email = validateEmail(email);
		this.name = validateName(name);
		this.nickname = validateNickname(nickname);
		this.birthday = birthday;
		this.gender = validateGender(gender);
		this.profileImageUrl = validateProfileImageUrl(profileImageUrl);
		this.serviceAgreement = requireNonNull(serviceAgreement);
		this.collectionAgreement = requireNonNull(collectionAgreement);
		this.deletedAt = deletedAt;
	}

	private String validateEmail(String email) {
		if (isBlank(email) || email.length() > EMAIL_LENGTH) {
			throw new IllegalArgumentException(MEMBER_INVALID_EMAIL.getMessage());
		}

		return email;
	}

	private String validateName(String name) {
		if (isBlank(name) || name.length() > NAME_LENGTH) {
			throw new IllegalArgumentException(MEMBER_INVALID_NAME.getMessage());
		}

		return name;
	}

	private String validateNickname(String nickname) {
		if (isBlank(nickname) || nickname.length() > NICKNAME_LENGTH) {
			throw new IllegalArgumentException(MEMBER_INVALID_NICKNAME.getMessage());
		}

		return nickname;
	}

	private String validateGender(String gender) {
		if (isBlank(gender)) {
			gender = "밝히고 싶지 않음";
		}

		if (gender.length() > GENDER_LENGTH) {
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
