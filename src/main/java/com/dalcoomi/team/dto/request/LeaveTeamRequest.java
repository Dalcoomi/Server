package com.dalcoomi.team.dto.request;

import jakarta.validation.constraints.NotNull;

public record LeaveTeamRequest(
	@NotNull(message = "그룹 ID는 필수입니다.")
	Long teamId,

	String nextLeaderNickname
) {

}
