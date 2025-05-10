package com.dalcoomi.fixture;

import static com.dalcoomi.transaction.domain.OwnerType.ADMIN;
import static com.dalcoomi.transaction.domain.TransactionType.EXPENSE;

import com.dalcoomi.category.domain.Category;
import com.dalcoomi.member.domain.Member;

public final class CategoryFixture {

	public static Category getCategory1(Member member) {
		Long ownerId = member.getId();
		String name = "식비";
		boolean isActive = true;

		return Category.builder()
			.member(member)
			.ownerId(ownerId)
			.name(name)
			.isActive(isActive)
			.transactionType(EXPENSE)
			.ownerType(ADMIN)
			.build();
	}
}
