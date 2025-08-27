package com.dalcoomi.fixture;

import java.time.LocalDate;

import com.dalcoomi.member.domain.Member;

public final class MemberFixture {

	public static Member getMember1() {
		String email = "abc1@gmail.com";
		String name = "가나다";
		String nickname = "가나다아";
		LocalDate birthday = LocalDate.of(2000, 1, 1);
		String gender = "남성";
		String profileImageUrl = "https://profile.com/1";

		return Member.builder()
			.email(email)
			.name(name)
			.nickname(nickname)
			.birthday(birthday)
			.gender(gender)
			.profileImageUrl(profileImageUrl)
			.serviceAgreement(true)
			.collectionAgreement(true)
			.build();
	}

	public static Member getMemberWithId1() {
		Long id = 1L;
		String email = "abc1@gmail.com";
		String name = "가나다";
		String nickname = "가나다#1234";
		LocalDate birthday = LocalDate.of(2000, 1, 1);
		String gender = "남성";
		String profileImageUrl = "https://profile.com/1";

		return Member.builder()
			.id(id)
			.email(email)
			.name(name)
			.nickname(nickname)
			.birthday(birthday)
			.gender(gender)
			.profileImageUrl(profileImageUrl)
			.serviceAgreement(true)
			.collectionAgreement(true)
			.build();
	}

	public static Member getMember2() {
		String email = "abc2@gmail.com";
		String name = "라마바";
		String nickname = "라마바#1234";
		LocalDate birthday = LocalDate.of(2000, 2, 2);
		String gender = "여성";
		String profileImageUrl = "https://profile.com/12";

		return Member.builder()
			.email(email)
			.name(name)
			.nickname(nickname)
			.birthday(birthday)
			.gender(gender)
			.profileImageUrl(profileImageUrl)
			.serviceAgreement(true)
			.collectionAgreement(true)
			.build();
	}

	public static Member getMemberWithId2() {
		String email = "abc2@gmail.com";
		String name = "라마바";
		String nickname = "라마바#1234";
		LocalDate birthday = LocalDate.of(2000, 2, 2);
		String gender = "여성";
		String profileImageUrl = "https://profile.com/12";

		return Member.builder()
			.id(2L)
			.email(email)
			.name(name)
			.nickname(nickname)
			.birthday(birthday)
			.gender(gender)
			.profileImageUrl(profileImageUrl)
			.serviceAgreement(true)
			.collectionAgreement(true)
			.build();
	}

	public static Member getMember3() {
		String email = "abc3@gmail.com";
		String name = "테스트3";
		String nickname = "테스트3#1234";
		LocalDate birthday = LocalDate.of(2000, 3, 3);
		String gender = "남성";
		String profileImageUrl = "https://profile.com/123";

		return Member.builder()
			.email(email)
			.name(name)
			.nickname(nickname)
			.birthday(birthday)
			.gender(gender)
			.profileImageUrl(profileImageUrl)
			.serviceAgreement(true)
			.collectionAgreement(true)
			.build();
	}
}
