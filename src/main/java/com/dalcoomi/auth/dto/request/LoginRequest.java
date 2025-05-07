package com.dalcoomi.auth.dto.request;

import com.dalcoomi.member.domain.SocialType;

public record LoginRequest(
	String socialId,
	SocialType socialType
) {

}
