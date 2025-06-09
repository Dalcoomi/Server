package com.dalcoomi.fixture;

import com.dalcoomi.member.domain.Member;
import com.dalcoomi.team.domain.Team;

public final class TeamFixture {

	public static Team getTeam1(Member leader) {
		String title = "그룹1";
		String invitationCode = "12345678";
		Integer memberLimit = 1;
		String purpose = "절약";

		return Team.builder()
			.leader(leader)
			.title(title)
			.invitationCode(invitationCode)
			.memberLimit(memberLimit)
			.purpose(purpose)
			.build();
	}

	public static Team getTeam2(Member leader) {
		String title = "그룹2";
		String invitationCode = "87654321";
		Integer memberLimit = 3;
		String purpose = "여행";

		return Team.builder()
			.leader(leader)
			.title(title)
			.invitationCode(invitationCode)
			.memberLimit(memberLimit)
			.purpose(purpose)
			.build();
	}

	public static Team getTeam3(Member leader) {
		String title = "그룹3";
		String invitationCode = "34589439";
		Integer memberLimit = 7;
		String purpose = "저축";

		return Team.builder()
			.leader(leader)
			.title(title)
			.invitationCode(invitationCode)
			.memberLimit(memberLimit)
			.purpose(purpose)
			.build();
	}

	public static Team getTeam4(Member leader) {
		String title = "그룹4";
		String invitationCode = "65454321";
		Integer memberLimit = 10;
		String purpose = "밥";

		return Team.builder()
			.leader(leader)
			.title(title)
			.invitationCode(invitationCode)
			.memberLimit(memberLimit)
			.purpose(purpose)
			.build();
	}

	public static Team getTeam5(Member leader) {
		String title = "그룹5";
		String invitationCode = "18271323";
		Integer memberLimit = 5;
		String purpose = "몰라";

		return Team.builder()
			.leader(leader)
			.title(title)
			.invitationCode(invitationCode)
			.memberLimit(memberLimit)
			.purpose(purpose)
			.build();
	}
}
