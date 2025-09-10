package com.dalcoomi.auth.dto.response;

import com.dalcoomi.auth.dto.LoginInfo;

import lombok.Builder;

@Builder
public record LoginResponse(
	boolean sameSocial,
	String accessToken,
	String refreshToken
) {

	public static LoginResponse from(LoginInfo loginInfo) {
		return LoginResponse.builder()
			.sameSocial(loginInfo.sameSocial())
			.accessToken(loginInfo.accessToken())
			.refreshToken(loginInfo.refreshToken())
			.build();
	}
}
