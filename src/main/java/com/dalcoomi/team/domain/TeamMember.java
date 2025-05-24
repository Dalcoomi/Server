package com.dalcoomi.team.domain;

import java.time.LocalDateTime;

import com.dalcoomi.member.domain.Member;

import lombok.Builder;
import lombok.Getter;

@Getter
public class TeamMember {

	private final Long id;
	private final LocalDateTime deletedAt;
	private final Team team;
	private final Member member;

	@Builder
	public TeamMember(Long id, Team team, Member member, LocalDateTime deletedAt) {
		this.id = id;
		this.team = team;
		this.member = member;
		this.deletedAt = deletedAt;
	}

	public static TeamMember of(Team team, Member member) {
		return TeamMember.builder()
			.team(team)
			.member(member)
			.build();
	}
}
