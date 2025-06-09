package com.dalcoomi.auth.dto;

public record TokenInfo(
	String accessToken,
	String refreshToken
) {

}
