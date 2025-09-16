package com.dalcoomi.auth.dto.response;

import com.dalcoomi.auth.dto.LoginInfo;
import com.dalcoomi.member.domain.SocialType;

import lombok.Builder;

@Builder
public record LoginResponse(
	boolean sameSocial,
	SocialType existingSocialType,
	String accessToken,
	String refreshToken
) {

	public static LoginResponse from(LoginInfo loginInfo) {
		return LoginResponse.builder()
			.sameSocial(loginInfo.sameSocial())
			.existingSocialType(loginInfo.existingSocialType())
			.accessToken(loginInfo.accessToken())
			.refreshToken(loginInfo.refreshToken())
			.build();
	}
}
