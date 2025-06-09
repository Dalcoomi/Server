package com.dalcoomi.team.dto;

import lombok.Builder;

@Builder
public record LeaveTeamInfo(
	Long teamId,
	String nextLeaderNickname
) {

	public static LeaveTeamInfo of(Long teamId, String nextLeaderNickname) {
		return LeaveTeamInfo.builder()
			.teamId(teamId)
			.nextLeaderNickname(nextLeaderNickname)
			.build();
	}
}
