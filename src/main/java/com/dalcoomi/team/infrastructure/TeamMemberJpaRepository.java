package com.dalcoomi.team.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamMemberJpaRepository extends JpaRepository<TeamMemberJpaEntity, Long> {

	boolean existsByTeamIdAndMemberIdAndDeletedAtIsNull(Long teamId, Long memberId);

	int countByTeamIdAndDeletedAtIsNull(Long teamId);
}
