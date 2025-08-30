package com.dalcoomi.member.dto.response;

import java.time.LocalDate;

import com.dalcoomi.member.domain.SocialType;
import com.dalcoomi.member.dto.MemberInfo;

import lombok.Builder;

@Builder
public record GetMemberResponse(
	SocialType socialType,
	String email,
	String name,
	String nickname,
	LocalDate birthday,
	String gender,
	String profileImageUrl
) {

	public static GetMemberResponse from(MemberInfo memberInfo) {
		return GetMemberResponse.builder()
			.socialType(memberInfo.socialType())
			.email(memberInfo.email())
			.name(memberInfo.name())
			.nickname(memberInfo.nickname())
			.birthday(memberInfo.birthday())
			.gender(memberInfo.gender())
			.profileImageUrl(memberInfo.profileImageUrl())
			.build();
	}
}
