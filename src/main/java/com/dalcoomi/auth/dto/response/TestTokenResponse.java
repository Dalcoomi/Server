package com.dalcoomi.auth.dto.response;

public record TestTokenResponse(
	String accessToken,
	String refreshToken
) {

}
