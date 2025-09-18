package com.dalcoomi.auth.dto.response;

import com.dalcoomi.auth.dto.TokenInfo;

import lombok.Builder;

@Builder
public record TestTokenResponse(
	String accessToken,
	String refreshToken
) {

	public static TestTokenResponse from(TokenInfo tokenInfo) {
		return TestTokenResponse.builder()
			.accessToken(tokenInfo.accessToken())
			.refreshToken(tokenInfo.refreshToken())
			.build();
	}
}
