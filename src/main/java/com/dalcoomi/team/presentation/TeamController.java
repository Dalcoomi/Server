package com.dalcoomi.team.presentation;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.dalcoomi.auth.annotation.AuthMember;
import com.dalcoomi.team.application.TeamService;
import com.dalcoomi.team.domain.Team;
import com.dalcoomi.team.dto.LeaveTeamInfo;
import com.dalcoomi.team.dto.TeamInfo;
import com.dalcoomi.team.dto.TeamsInfo;
import com.dalcoomi.team.dto.request.LeaveTeamRequest;
import com.dalcoomi.team.dto.request.TeamRequest;
import com.dalcoomi.team.dto.response.GetMyTeamsResponse;
import com.dalcoomi.team.dto.response.GetTeamResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

	private final TeamService teamService;

	@PostMapping
	@ResponseStatus(CREATED)
	public String create(@AuthMember Long memberId, @RequestBody @Valid TeamRequest request) {
		Team team = Team.from(request);

		return teamService.create(memberId, team);
	}

	@PostMapping("/join/{invitationCode}")
	@ResponseStatus(OK)
	public void join(@AuthMember Long memberId, @PathVariable("invitationCode") String invitationCode) {
		teamService.join(memberId, invitationCode);
	}

	@GetMapping
	@ResponseStatus(OK)
	public GetMyTeamsResponse get(@AuthMember Long memberId) {
		TeamsInfo teamsInfo = teamService.get(memberId);

		return GetMyTeamsResponse.from(teamsInfo);
	}

	@GetMapping("/{teamId}")
	@ResponseStatus(OK)
	public GetTeamResponse get(@AuthMember Long memberId, @PathVariable("teamId") Long teamId) {
		TeamInfo teamInfo = teamService.get(teamId, memberId);

		return GetTeamResponse.from(teamInfo);
	}

	@DeleteMapping("/leave")
	@ResponseStatus(OK)
	public void leave(@AuthMember Long memberId, @RequestBody LeaveTeamRequest request) {
		LeaveTeamInfo leaveTeamInfo = LeaveTeamInfo.of(request.teamId(), request.nextLeaderNickname());

		teamService.leave(leaveTeamInfo, memberId);
	}
}
