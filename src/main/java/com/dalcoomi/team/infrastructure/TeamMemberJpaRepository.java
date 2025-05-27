package com.dalcoomi.team.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamMemberJpaRepository extends JpaRepository<TeamMemberJpaEntity, Long> {

	boolean existsByTeamIdAndMemberId(Long teamId, Long memberId);

	int countByTeamId(Long teamId);

	void deleteByTeamIdAndMemberId(Long teamId, Long memberId);
}
