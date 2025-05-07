package com.dalcoomi.member.dto.response;

public record SignUpResponse(
	String accessToken,
	String refreshToken
) {

}
