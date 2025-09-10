package com.dalcoomi.auth.dto.response;

import com.dalcoomi.auth.dto.TokenInfo;

import lombok.Builder;

@Builder
public record ReissueTokenResponse(
	String accessToken,
	String refreshToken
) {

	public static ReissueTokenResponse from(TokenInfo tokenInfo) {
		return ReissueTokenResponse.builder()
			.accessToken(tokenInfo.accessToken())
			.refreshToken(tokenInfo.refreshToken())
			.build();
	}
}
