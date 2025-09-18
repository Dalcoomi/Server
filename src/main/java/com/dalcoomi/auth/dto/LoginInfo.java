package com.dalcoomi.auth.dto;

import com.dalcoomi.member.domain.SocialType;

import lombok.Builder;

@Builder
public record LoginInfo(
	boolean sameSocial,
	SocialType existingSocialType,
	String accessToken,
	String refreshToken
) {

}
