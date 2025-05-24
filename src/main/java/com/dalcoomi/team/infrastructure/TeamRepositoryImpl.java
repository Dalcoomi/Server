package com.dalcoomi.team.infrastructure;

import static com.dalcoomi.common.error.model.ErrorMessage.TEAM_NOT_FOUND;
import static java.util.stream.Collectors.toSet;

import java.util.Set;

import org.springframework.stereotype.Repository;

import com.dalcoomi.common.error.exception.NotFoundException;
import com.dalcoomi.team.application.repository.TeamRepository;
import com.dalcoomi.team.domain.Team;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TeamRepositoryImpl implements TeamRepository {

	private final TeamJpaRepository teamJpaRepository;

	@Override
	public Team save(Team team) {
		return teamJpaRepository.save(TeamJpaEntity.from(team)).toModel();
	}

	@Override
	public Set<String> findExistingCodes(Set<String> candidates) {
		return teamJpaRepository.findByInvitationCodeInAndDeletedAtIsNull(candidates)
			.stream()
			.map(TeamJpaEntity::getInvitationCode)
			.collect(toSet());
	}

	@Override
	public Team findByInvitationCode(String invitationCode) {
		return teamJpaRepository.findByInvitationCodeAndDeletedAtIsNull(invitationCode)
			.orElseThrow(() -> new NotFoundException(TEAM_NOT_FOUND)).toModel();
	}
}
