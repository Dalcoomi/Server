package com.dalcoomi.team.infrastructure;

import org.springframework.stereotype.Repository;

import com.dalcoomi.team.application.repository.TeamMemberRepository;
import com.dalcoomi.team.domain.TeamMember;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TeamMemberRepositoryImpl implements TeamMemberRepository {

	private final TeamMemberJpaRepository teamMemberJpaRepository;

	@Override
	public TeamMember save(TeamMember teamMember) {
		return teamMemberJpaRepository.save(TeamMemberJpaEntity.from(teamMember)).toModel();
	}

	@Override
	public boolean existsByTeamIdAndMemberId(Long teamId, Long memberId) {
		return teamMemberJpaRepository.existsByTeamIdAndMemberIdAndDeletedAtIsNull(teamId, memberId);
	}

	@Override
	public int countByTeamId(Long teamId) {
		return teamMemberJpaRepository.countByTeamIdAndDeletedAtIsNull(teamId);
	}
}
