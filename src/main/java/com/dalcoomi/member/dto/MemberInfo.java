package com.dalcoomi.member.dto;

import java.time.LocalDate;

import lombok.Builder;

@Builder
public record MemberInfo(
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
