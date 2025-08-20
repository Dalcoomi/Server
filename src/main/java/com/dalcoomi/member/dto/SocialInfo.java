package com.dalcoomi.member.dto;

import com.dalcoomi.member.domain.SocialType;

import lombok.Builder;

@Builder
public record SocialInfo(
	String socialId,
	SocialType socialType
) {

}
