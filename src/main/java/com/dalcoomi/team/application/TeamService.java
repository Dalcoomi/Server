package com.dalcoomi.team.application;

import static com.dalcoomi.common.constant.TeamConstants.MAX_TEAM_LIMIT;
import static com.dalcoomi.common.error.model.ErrorMessage.TEAM_COUNT_EXCEEDED;
import static com.dalcoomi.common.error.model.ErrorMessage.TEAM_MEMBER_ALREADY_EXISTS;
import static com.dalcoomi.common.error.model.ErrorMessage.TEAM_MEMBER_COUNT_EXCEEDED;
import static com.dalcoomi.common.error.model.ErrorMessage.TEAM_MEMBER_NOT_FOUND;
import static com.dalcoomi.common.error.model.ErrorMessage.TEAM_NOT_FOUND;
import static com.dalcoomi.team.domain.Team.generateInvitationCode;
import static java.util.stream.Collectors.toSet;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dalcoomi.common.error.exception.ConflictException;
import com.dalcoomi.common.error.exception.NotFoundException;
import com.dalcoomi.member.application.repository.MemberRepository;
import com.dalcoomi.member.domain.Member;
import com.dalcoomi.team.application.repository.TeamMemberRepository;
import com.dalcoomi.team.application.repository.TeamRepository;
import com.dalcoomi.team.domain.Team;
import com.dalcoomi.team.domain.TeamMember;
import com.dalcoomi.team.dto.LeaveTeamInfo;
import com.dalcoomi.team.dto.TeamInfo;
import com.dalcoomi.team.dto.TeamsInfo;
import com.dalcoomi.transaction.application.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TeamService {

	private final TeamRepository teamRepository;
	private final MemberRepository memberRepository;
	private final TeamMemberRepository teamMemberRepository;
	private final TransactionRepository transactionRepository;

	@Transactional
	public String createTeam(Long memberId, Team team) {
		Member member = memberRepository.findById(memberId);

		validateMaxTeamCount(memberId);

		String uniqueCode = findUniqueInvitationCode();

		team.updateMember(member);
		team.updateInvitationCode(uniqueCode);

		Team savedTeam = teamRepository.save(team);

		TeamMember teamMember = TeamMember.of(savedTeam, member);

		teamMemberRepository.save(teamMember);

		return savedTeam.getInvitationCode();
	}

	@Transactional
	public void joinTeam(Long memberId, String invitationCode) {
		Member member = memberRepository.findById(memberId);
		Team team = teamRepository.findByInvitationCode(invitationCode);

		if (teamMemberRepository.existsByTeamIdAndMemberId(team.getId(), memberId)) {
			throw new ConflictException(TEAM_MEMBER_ALREADY_EXISTS);
		}

		validateMaxTeamCount(memberId);

		int currentMemberCount = teamMemberRepository.countByTeamId(team.getId());

		if (currentMemberCount >= team.getMemberLimit()) {
			throw new ConflictException(TEAM_MEMBER_COUNT_EXCEEDED);
		}

		TeamMember teamMember = TeamMember.of(team, member);

		teamMemberRepository.save(teamMember);
	}

	@Transactional(readOnly = true)
	public TeamsInfo getMyTeams(Long memberId) {
		List<TeamMember> teamMembers = teamMemberRepository.find(null, memberId);

		List<Team> teams = teamMembers.stream().map(TeamMember::getTeam).toList().reversed();

		List<Long> teamIds = teams.stream().map(Team::getId).toList();

		List<Integer> memberCounts = teams.stream()
			.map(team -> teamMemberRepository.countByTeamIds(teamIds).get(team.getId())).toList();

		return TeamsInfo.of(teams, memberCounts);
	}

	@Transactional(readOnly = true)
	public TeamInfo getTeam(Long teamId, Long memberId) {
		List<TeamMember> teamMembers = teamMemberRepository.find(teamId, null);

		if (teamMembers.isEmpty()) {
			throw new NotFoundException(TEAM_NOT_FOUND);
		}

		TeamMember requestingMember = teamMembers.stream()
			.filter(teamMember -> teamMember.getMember().getId().equals(memberId))
			.findFirst()
			.orElseThrow(() -> new NotFoundException(TEAM_MEMBER_NOT_FOUND));

		Team team = requestingMember.getTeam();

		List<Member> members = teamMembers.stream().map(TeamMember::getMember).toList();

		return TeamInfo.of(team, members);
	}

	@Transactional
	public void leaveTeam(LeaveTeamInfo leaveTeamInfo, Long memberId) {
		List<TeamMember> teamMember = teamMemberRepository.find(leaveTeamInfo.teamId(), memberId);

		if (teamMember.isEmpty()) {
			throw new NotFoundException(TEAM_NOT_FOUND);
		}

		Team team = teamMember.getFirst().getTeam();

		// 요청한 사람이 그룹 리더일 경우 새 리더 지정
		if (team.getLeader().getId().equals(memberId) && leaveTeamInfo.nextLeaderNickname() != null) {
			Member nextLeader = memberRepository.findByNickname(leaveTeamInfo.nextLeaderNickname());

			team.updateMember(nextLeader);

			teamRepository.save(team);
		}

		teamMemberRepository.deleteByTeamIdAndMemberId(leaveTeamInfo.teamId(), memberId);

		if (teamMemberRepository.countByTeamId(leaveTeamInfo.teamId()) == 0) {
			teamRepository.deleteById(leaveTeamInfo.teamId());
			transactionRepository.deleteByTeamId(leaveTeamInfo.teamId());
		}
	}

	private String findUniqueInvitationCode() {
		Set<String> candidates = IntStream.range(0, 10)
			.mapToObj(i -> generateInvitationCode())
			.collect(toSet());

		Set<String> existingCodes = teamRepository.findExistingCodes(candidates);

		return candidates.stream()
			.filter(code -> !existingCodes.contains(code))
			.findFirst()
			.orElseGet(Team::generateInvitationCode);
	}

	private void validateMaxTeamCount(Long memberId) {
		int currentTeamCount = teamMemberRepository.countByMemberId(memberId);

		if (currentTeamCount >= MAX_TEAM_LIMIT) {
			throw new ConflictException(TEAM_COUNT_EXCEEDED);
		}
	}
}
