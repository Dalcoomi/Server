package com.dalcoomi.team.application.repository;

import java.util.List;
import java.util.Map;

import com.dalcoomi.team.domain.TeamMember;

public interface TeamMemberRepository {

	TeamMember save(TeamMember teamMember);

	boolean existsByTeamIdAndMemberId(Long teamId, Long memberId);

	List<TeamMember> find(Long teamId, Long memberId);

	int countByTeamId(Long teamId);

	Map<Long, Integer> countByTeamIds(List<Long> teamIds);

	void deleteByTeamIdAndMemberId(Long teamId, Long memberId);
}
