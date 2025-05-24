package com.dalcoomi.fixture;

import com.dalcoomi.member.domain.Member;
import com.dalcoomi.team.domain.Team;

public final class TeamFixture {

	public static Team getTeam1(Member member) {
		String title = "그룹1";
		String invitationCode = "12345678";
		Integer memberLimit = 1;
		String purpose = "절약";

		return Team.builder()
			.member(member)
			.title(title)
			.invitationCode(invitationCode)
			.memberLimit(memberLimit)
			.purpose(purpose)
			.build();
	}
}
