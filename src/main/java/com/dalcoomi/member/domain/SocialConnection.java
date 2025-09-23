package com.dalcoomi.member.domain;

import static com.dalcoomi.common.error.model.ErrorMessage.MEMBER_INVALID_SOCIAL_ID;
import static io.micrometer.common.util.StringUtils.isBlank;
import static java.time.LocalDateTime.now;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
public class SocialConnection {

	private static final int SOCIAL_ID_LENGTH = 50;

	private final Long id;
	private final Member member;
	private final String socialId;
	private final SocialType socialType;
	private String socialEmail;
	private String socialRefreshToken;
	private LocalDateTime deletedAt;

	@Builder
	public SocialConnection(Long id, Member member, String socialEmail, String socialId, String socialRefreshToken,
		SocialType socialType, LocalDateTime deletedAt) {
		this.id = id;
		this.member = member;
		this.socialEmail = socialEmail;
		this.socialId = validateSocialId(socialId);
		this.socialRefreshToken = socialRefreshToken;
		this.socialType = socialType;
		this.deletedAt = deletedAt;
	}

	public void updateSocialEmail(String socialEmail) {
		this.socialEmail = socialEmail;
	}

	public void updateSocialRefreshToken(String socialRefreshToken) {
		this.socialRefreshToken = socialRefreshToken;
	}

	public void softDelete() {
		this.deletedAt = now();
	}

	private String validateSocialId(String socialId) {
		if (isBlank(socialId) || socialId.length() > SOCIAL_ID_LENGTH) {
			throw new IllegalArgumentException(MEMBER_INVALID_SOCIAL_ID.getMessage());
		}

		return socialId;
	}
}
