package com.dalcoomi.member.domain;

import static com.dalcoomi.common.error.model.ErrorMessage.MEMBER_INVALID_SOCIAL_ID;
import static io.micrometer.common.util.StringUtils.isBlank;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
public class SocialConnection {

	private static final int SOCIAL_ID_LENGTH = 50;

	private final Long id;
	private final Member member;
	private final String socialEmail;
	private final String socialId;
	private final SocialType socialType;
	private LocalDateTime deletedAt;

	@Builder
	public SocialConnection(Long id, Member member, String socialEmail, String socialId, SocialType socialType,
		LocalDateTime deletedAt) {
		this.id = id;
		this.member = member;
		this.socialEmail = socialEmail;
		this.socialId = validateSocialId(socialId);
		this.socialType = socialType;
		this.deletedAt = deletedAt;
	}

	public void softDelete() {
		this.deletedAt = LocalDateTime.now();
	}

	private String validateSocialId(String socialId) {
		if (isBlank(socialId) || socialId.length() > SOCIAL_ID_LENGTH) {
			throw new IllegalArgumentException(MEMBER_INVALID_SOCIAL_ID.getMessage());
		}

		return socialId;
	}
}
