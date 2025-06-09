package com.dalcoomi.team.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamMemberJpaRepository extends JpaRepository<TeamMemberJpaEntity, Long> {

	boolean existsByTeamIdAndMemberId(Long teamId, Long memberId);

	int countByTeamId(Long teamId);

	int countByMemberId(Long memberId);

	void deleteByTeamIdAndMemberId(Long teamId, Long memberId);
}
