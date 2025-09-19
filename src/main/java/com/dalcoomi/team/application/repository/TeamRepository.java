package com.dalcoomi.team.application.repository;

import java.util.Set;

import com.dalcoomi.team.domain.Team;

public interface TeamRepository {

	Team save(Team team);

	boolean existsById(Long teamId);

	Set<String> findExistingCodes(Set<String> candidates);

	Team findById(Long id);

	Team findByInvitationCode(String invitationCode);

	void deleteById(Long teamId);
}
