package com.dalcoomi.auth.dto.response;

public record LoginResponse(
	String accessToken,
	String refreshToken
) {

}
