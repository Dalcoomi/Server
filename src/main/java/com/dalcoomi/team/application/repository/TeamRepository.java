package com.dalcoomi.team.application.repository;

import java.util.Set;

import com.dalcoomi.team.domain.Team;

public interface TeamRepository {

	Team save(Team team);

	Set<String> findExistingCodes(Set<String> candidates);

	Team findByInvitationCode(String invitationCode);
}
