package com.dalcoomi.member.dto.response;

import java.time.LocalDate;
import java.util.List;

import com.dalcoomi.member.domain.SocialType;
import com.dalcoomi.member.dto.MemberInfo;

import lombok.Builder;

@Builder
public record GetMemberResponse(
	List<SocialType> socialTypes,
	String email,
	String name,
	String nickname,
	LocalDate birthday,
	String gender,
	String profileImageUrl,
	Boolean aiLearningAgreement
) {

	public static GetMemberResponse from(MemberInfo memberInfo) {
		return GetMemberResponse.builder()
			.socialTypes(memberInfo.socialTypes())
			.email(memberInfo.email())
			.name(memberInfo.name())
			.nickname(memberInfo.nickname())
			.birthday(memberInfo.birthday())
			.gender(memberInfo.gender())
			.profileImageUrl(memberInfo.profileImageUrl())
			.aiLearningAgreement(memberInfo.aiLearningAgreement())
			.build();
	}
}
