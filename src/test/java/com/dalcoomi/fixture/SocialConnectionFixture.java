package com.dalcoomi.fixture;

import static com.dalcoomi.member.domain.SocialType.KAKAO;
import static com.dalcoomi.member.domain.SocialType.NAVER;

import com.dalcoomi.member.domain.Member;
import com.dalcoomi.member.domain.SocialConnection;

public final class SocialConnectionFixture {

	public static SocialConnection getSocialConnection1(Member member) {
		String socialId = "123";

		return SocialConnection.builder()
			.member(member)
			.socialId(socialId)
			.socialType(KAKAO)
			.build();
	}

	public static SocialConnection getSocialConnection2(Member member) {
		String socialId = "123";

		return SocialConnection.builder()
			.member(member)
			.socialId(socialId)
			.socialType(NAVER)
			.build();
	}
}
