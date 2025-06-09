package com.dalcoomi.team.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamJpaRepository extends JpaRepository<TeamJpaEntity, Long> {

	List<TeamJpaEntity> findByInvitationCodeIn(Set<String> candidates);

	Optional<TeamJpaEntity> findByInvitationCode(String invitationCode);

	Optional<TeamJpaEntity> findByIdAndLeaderId(Long teamId, Long leaderId);
}
