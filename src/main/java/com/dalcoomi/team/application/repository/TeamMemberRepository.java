package com.dalcoomi.team.application.repository;

import com.dalcoomi.team.domain.TeamMember;

public interface TeamMemberRepository {

	TeamMember save(TeamMember teamMember);

	boolean existsByTeamIdAndMemberId(Long teamId, Long memberId);

	int countByTeamId(Long teamId);
}
