package com.dalcoomi.team.dto.response;

import java.util.List;

import com.dalcoomi.team.domain.Team;
import com.dalcoomi.team.dto.TeamsInfo;

import lombok.Builder;

@Builder
public record GetMyTeamsResponse(
	List<GetMyTeamsResponseItem> groups
) {

	public static GetMyTeamsResponse from(TeamsInfo teamsInfo) {
		List<GetMyTeamsResponseItem> groups = teamsInfo.teamsWithCounts().stream()
			.map(teamWithCount -> GetMyTeamsResponseItem.of(teamWithCount.team(), teamWithCount.memberCount()))
			.toList();

		return GetMyTeamsResponse.builder()
			.groups(groups)
			.build();
	}

	@Builder
	public record GetMyTeamsResponseItem(
		Long teamId,
		String title,
		Integer memberCount,
		Integer memberLimit
	) {

		public static GetMyTeamsResponseItem of(Team team, Integer memberCount) {
			return GetMyTeamsResponseItem.builder()
				.teamId(team.getId())
				.title(team.getTitle())
				.memberCount(memberCount)
				.memberLimit(team.getMemberLimit())
				.build();
		}
	}
}
