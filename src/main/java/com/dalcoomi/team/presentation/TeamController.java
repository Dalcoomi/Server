package com.dalcoomi.team.presentation;

import static java.util.stream.Collectors.toMap;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.dalcoomi.auth.annotation.AuthMember;
import com.dalcoomi.common.util.lock.RedisLockUtil;
import com.dalcoomi.common.util.lock.TeamLockKeyGenerator;
import com.dalcoomi.team.application.TeamService;
import com.dalcoomi.team.domain.Team;
import com.dalcoomi.team.dto.LeaveTeamInfo;
import com.dalcoomi.team.dto.TeamInfo;
import com.dalcoomi.team.dto.TeamsInfo;
import com.dalcoomi.team.dto.request.LeaveTeamRequest;
import com.dalcoomi.team.dto.request.TeamRequest;
import com.dalcoomi.team.dto.request.UpdateTeamOrderRequest;
import com.dalcoomi.team.dto.response.GetMyTeamsResponse;
import com.dalcoomi.team.dto.response.GetTeamResponse;
import com.dalcoomi.team.dto.response.JoinResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

	private final TeamService teamService;
	private final RedisLockUtil redisLockUtil;
	private final TeamLockKeyGenerator teamLockKeyGenerator;

	@PostMapping
	@ResponseStatus(CREATED)
	public String create(@AuthMember Long memberId, @RequestBody @Valid TeamRequest request) {
		Team team = Team.from(request);

		return teamService.create(memberId, team);
	}

	@PostMapping("/join/{invitationCode}")
	@ResponseStatus(OK)
	public JoinResponse join(@AuthMember Long memberId, @PathVariable("invitationCode") String invitationCode) {
		Team team = teamService.join(memberId, invitationCode);

		return new JoinResponse(team.getId(), team.getTitle());
	}

	@GetMapping
	@ResponseStatus(OK)
	public GetMyTeamsResponse get(@AuthMember Long memberId) {
		TeamsInfo teamsInfo = teamService.get(memberId);

		return GetMyTeamsResponse.from(teamsInfo);
	}

	@GetMapping("/{teamId}")
	@ResponseStatus(OK)
	public GetTeamResponse getTeamInfo(@AuthMember Long memberId, @PathVariable("teamId") Long teamId) {
		TeamInfo teamInfo = teamService.getTeamInfo(teamId, memberId);

		return GetTeamResponse.from(teamInfo);
	}

	@PatchMapping
	@ResponseStatus(OK)
	public void update(@AuthMember Long memberId, @RequestBody @Valid TeamRequest request) {
		Team team = Team.from(request);

		teamService.update(memberId, team);
	}

	@PatchMapping("/order")
	@ResponseStatus(OK)
	public void updateDisplayOrder(@AuthMember Long memberId, @RequestBody @Valid UpdateTeamOrderRequest request) {
		Map<Long, Integer> teamIdsAndDisplayOrders = request.orders().stream()
			.collect(toMap(
				UpdateTeamOrderRequest.TeamOrderItem::teamId,
				UpdateTeamOrderRequest.TeamOrderItem::displayOrder
			));

		teamService.updateDisplayOrder(memberId, teamIdsAndDisplayOrders);
	}

	@DeleteMapping("/leave")
	@ResponseStatus(OK)
	public void leave(@AuthMember Long memberId, @RequestBody @Valid LeaveTeamRequest request) {
		Long teamId = request.teamId();
		String lockKey = teamLockKeyGenerator.generateLeaveLockKey(teamId);

		redisLockUtil.acquireAndRunLock(lockKey, () -> {
			LeaveTeamInfo leaveTeamInfo = LeaveTeamInfo.of(teamId, request.nextLeaderNickname());

			teamService.leave(leaveTeamInfo, memberId);

			return null;
		});
	}
}
