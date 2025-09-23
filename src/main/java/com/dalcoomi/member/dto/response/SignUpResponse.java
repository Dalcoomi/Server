package com.dalcoomi.member.dto.response;

import com.dalcoomi.auth.dto.TokenInfo;

import lombok.Builder;

@Builder
public record SignUpResponse(
	String accessToken,
	String refreshToken
) {

	public static SignUpResponse from(TokenInfo tokenInfo) {
		return SignUpResponse.builder()
			.accessToken(tokenInfo.accessToken())
			.refreshToken(tokenInfo.refreshToken())
			.build();
	}
}
