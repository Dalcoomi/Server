package com.dalcoomi.team.domain;

import com.dalcoomi.member.domain.Member;

import lombok.Builder;
import lombok.Getter;

@Getter
public class TeamMember {

	private final Long id;
	private final Team team;
	private final Member member;
	private int displayOrder;

	@Builder
	public TeamMember(Long id, Team team, Member member, int displayOrder) {
		this.id = id;
		this.team = team;
		this.member = member;
		this.displayOrder = displayOrder;
	}

	public static TeamMember of(Team team, Member member) {
		return TeamMember.builder()
			.team(team)
			.member(member)
			.displayOrder(0)
			.build();
	}

	public void updateDisplayOrder(int displayOrder) {
		this.displayOrder = displayOrder;
	}
}
