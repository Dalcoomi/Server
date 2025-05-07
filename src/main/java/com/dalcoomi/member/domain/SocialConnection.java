package com.dalcoomi.member.domain;

import static com.dalcoomi.common.error.model.ErrorMessage.MEMBER_INVALID_SOCIAL_ID;
import static io.micrometer.common.util.StringUtils.isBlank;

import lombok.Builder;
import lombok.Getter;

@Getter
public class SocialConnection {

	private static final int SOCIAL_ID_LENGTH = 30;

	private final Long id;
	private final Member member;
	private final String socialId;
	private final SocialType socialType;

	@Builder
	public SocialConnection(Long id, Member member, String socialId, SocialType socialType) {
		this.id = id;
		this.member = member;
		this.socialId = validateSocialId(socialId);
		this.socialType = socialType;
	}

	private String validateSocialId(String socialId) {
		if (isBlank(socialId) || socialId.length() > SOCIAL_ID_LENGTH) {
			throw new IllegalArgumentException(MEMBER_INVALID_SOCIAL_ID.getMessage());
		}

		return socialId;
	}
}
