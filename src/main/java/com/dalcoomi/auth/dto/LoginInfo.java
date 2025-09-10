package com.dalcoomi.auth.dto;

import lombok.Builder;

@Builder
public record LoginInfo(
	boolean sameSocial,
	String accessToken,
	String refreshToken
) {

}
