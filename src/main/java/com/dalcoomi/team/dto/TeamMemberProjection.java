package com.dalcoomi.team.dto;

import com.querydsl.core.annotations.QueryProjection;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TeamMemberProjection {

	@QueryProjection
	public record TeamMemberCountDto(
		Long teamId,
		Integer count
	) {

	}
}
