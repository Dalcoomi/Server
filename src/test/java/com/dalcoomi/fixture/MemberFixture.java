package com.dalcoomi.fixture;

import java.time.LocalDate;

import com.dalcoomi.member.domain.Member;

public final class MemberFixture {

	public static Member getMember1() {
		String email = "abc1@gmail.com";
		String name = "조만제";
		String nickname = "조만제#1234";
		LocalDate birthday = LocalDate.of(2000, 1, 1);
		String gender = "MALE";
		String profileImageUrl = "https://profile.com/1234567890";

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
		String name = "조만제";
		String nickname = "조만제#1234";
		LocalDate birthday = LocalDate.of(2000, 1, 1);
		String gender = "MALE";
		String profileImageUrl = "https://profile.com/1234567890";

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
		String name = "손민정";
		String nickname = "손민정#1234";
		LocalDate birthday = LocalDate.of(2000, 2, 2);
		String gender = "FEMALE";
		String profileImageUrl = "https://profile.com/1234567890";

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
		String name = "손민정";
		String nickname = "손민정#1234";
		LocalDate birthday = LocalDate.of(2000, 2, 2);
		String gender = "FEMALE";
		String profileImageUrl = "https://profile.com/1234567890";

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
}
