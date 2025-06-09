package com.dalcoomi.fixture;

import static com.dalcoomi.category.domain.OwnerType.ADMIN;
import static com.dalcoomi.category.domain.OwnerType.MEMBER;
import static com.dalcoomi.transaction.domain.TransactionType.EXPENSE;

import com.dalcoomi.category.domain.Category;
import com.dalcoomi.member.domain.Member;

public final class CategoryFixture {

	public static Category getCategory1(Member member) {
		String name = "식비";
		String iconUrl = "https://example.com/112233";
		boolean isActive = true;

		return Category.builder()
			.creator(member)
			.name(name)
			.iconUrl(iconUrl)
			.isActive(isActive)
			.transactionType(EXPENSE)
			.ownerType(ADMIN)
			.build();
	}

	public static Category getCategory2(Member member) {
		String name = "카페";
		String iconUrl = "https://example.com/111222";
		boolean isActive = true;

		return Category.builder()
			.creator(member)
			.name(name)
			.iconUrl(iconUrl)
			.isActive(isActive)
			.transactionType(EXPENSE)
			.ownerType(ADMIN)
			.build();
	}

	public static Category getCategory3(Member member) {
		String name = "게임";
		String iconUrl = "https://example.com/111222333";
		boolean isActive = true;

		return Category.builder()
			.creator(member)
			.name(name)
			.iconUrl(iconUrl)
			.isActive(isActive)
			.transactionType(EXPENSE)
			.ownerType(MEMBER)
			.build();
	}
}
