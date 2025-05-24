package com.dalcoomi.team.presentation;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.dalcoomi.auth.config.AuthMember;
import com.dalcoomi.team.application.TeamService;
import com.dalcoomi.team.domain.Team;
import com.dalcoomi.team.dto.request.TeamRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/team")
@RequiredArgsConstructor
public class TeamController {

	private final TeamService teamService;

	@PostMapping
	@ResponseStatus(CREATED)
	public String createTeam(@AuthMember Long memberId, @RequestBody TeamRequest request) {
		Team team = Team.from(request);

		return teamService.createTeam(memberId, team);
	}

	@PostMapping("/join/{invitationCode}")
	@ResponseStatus(OK)
	public void joinTeam(@AuthMember Long memberId, @PathVariable("invitationCode") String invitationCode) {
		teamService.joinTeam(memberId, invitationCode);
	}
}
