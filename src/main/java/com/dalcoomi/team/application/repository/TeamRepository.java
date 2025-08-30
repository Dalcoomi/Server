package com.dalcoomi.team.application.repository;

import java.util.List;
import java.util.Set;

import com.dalcoomi.team.domain.Team;

public interface TeamRepository {

	Team save(Team team);

	Set<String> findExistingCodes(Set<String> candidates);

	Team findById(Long id);

	Team findByInvitationCode(String invitationCode);

	List<Team> findByLeaderId(Long leaderId);

	void deleteById(Long teamId);
}
