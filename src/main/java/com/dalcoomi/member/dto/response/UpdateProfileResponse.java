package com.dalcoomi.member.dto.response;

import java.time.LocalDate;

import com.dalcoomi.member.dto.MemberInfo;

import lombok.Builder;

@Builder
public record UpdateProfileResponse(
	String name,
	String nickname,
	LocalDate birthday,
	String gender
) {

	public static UpdateProfileResponse from(MemberInfo memberInfo) {
		return UpdateProfileResponse.builder()
			.name(memberInfo.name())
			.nickname(memberInfo.nickname())
			.birthday(memberInfo.birthday())
			.gender(memberInfo.gender())
			.build();
	}
}
