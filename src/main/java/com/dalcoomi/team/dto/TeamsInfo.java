package com.dalcoomi.team.dto;

import java.util.List;
import java.util.stream.IntStream;

import com.dalcoomi.team.domain.Team;

import lombok.Builder;

@Builder
public record TeamsInfo(
	List<TeamWithCount> teamsWithCounts
) {

	public static TeamsInfo of(List<Team> teams, List<Integer> memberCounts) {
		List<TeamWithCount> teamsWithCounts = IntStream.range(0, teams.size())
			.mapToObj(i -> TeamWithCount.of(teams.get(i), memberCounts.get(i)))
			.toList();

		return TeamsInfo.builder()
			.teamsWithCounts(teamsWithCounts)
			.build();
	}

	@Builder
	public record TeamWithCount(
		Team team,
		Integer memberCount
	) {

		public static TeamWithCount of(Team team, Integer memberCount) {
			return TeamWithCount.builder()
				.team(team)
				.memberCount(memberCount)
				.build();
		}
	}
}
