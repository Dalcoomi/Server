package com.dalcoomi.team.application.repository;

import java.util.List;
import java.util.Map;

import org.springframework.lang.Nullable;

import com.dalcoomi.team.domain.TeamMember;

public interface TeamMemberRepository {

	TeamMember save(TeamMember teamMember);

	boolean existsByTeamIdAndMemberId(Long teamId, Long memberId);

	List<TeamMember> find(@Nullable Long teamId, @Nullable Long memberId);

	int countByTeamId(Long teamId);

	int countByMemberId(Long memberId);

	Map<Long, Integer> countByTeamIds(List<Long> teamIds);

	void deleteByTeamIdAndMemberId(Long teamId, Long memberId);
}
