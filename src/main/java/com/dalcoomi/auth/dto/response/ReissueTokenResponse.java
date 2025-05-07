package com.dalcoomi.auth.dto.response;

public record ReissueTokenResponse(
	String accessToken,
	String refreshToken
) {

}
