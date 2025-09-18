package com.dalcoomi.member.dto;

import java.time.LocalDate;

import com.dalcoomi.member.domain.SocialType;

import lombok.Builder;

@Builder
public record SignUpInfo(
	String socialEmail,
	String socialId,
	SocialType socialType,
	String email,
	String name,
	String nickname,
	LocalDate birthday,
	String gender,
	String profileImageUrl,
	Boolean serviceAgreement,
	Boolean collectionAgreement
) {

}
