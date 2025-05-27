package com.dalcoomi.team.dto;

import java.util.List;

import com.dalcoomi.member.domain.Member;
import com.dalcoomi.team.domain.Team;

import lombok.Builder;

@Builder
public record TeamInfo(
	Team team,
	List<Member> members
) {

	public static TeamInfo of(Team team, List<Member> members) {
		return TeamInfo.builder()
			.team(team)
			.members(members)
			.build();
	}
}
