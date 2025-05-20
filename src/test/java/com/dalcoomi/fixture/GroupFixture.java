package com.dalcoomi.fixture;

import com.dalcoomi.group.domain.Group;
import com.dalcoomi.member.domain.Member;

public final class GroupFixture {

	public static Group getGroup1(Member member) {
		String title = "그룹1";
		String invitationCode = "12345678";
		Integer count = 1;
		String goal = "절약";

		return Group.builder()
			.member(member)
			.title(title)
			.invitationCode(invitationCode)
			.count(count)
			.goal(goal)
			.build();
	}
}
