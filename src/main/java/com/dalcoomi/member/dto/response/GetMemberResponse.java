package com.dalcoomi.member.dto.response;

import com.dalcoomi.member.domain.Member;

import lombok.Builder;

@Builder
public record GetMemberResponse(
	String email,
	String name,
	String nickname,
	String profileImageUrl
) {

	public static GetMemberResponse from(Member member) {
		return GetMemberResponse.builder()
			.email(member.getEmail())
			.name(member.getName())
			.nickname(member.getNickname())
			.profileImageUrl(member.getProfileImageUrl())
			.build();
	}
}
