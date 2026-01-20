package com.dalcoomi.team.application;

import static com.dalcoomi.common.error.model.ErrorMessage.TEAM_COUNT_EXCEEDED;
import static com.dalcoomi.common.error.model.ErrorMessage.TEAM_DISPLAY_ORDER_DUPLICATED;
import static com.dalcoomi.common.error.model.ErrorMessage.TEAM_INVALID_LEADER;
import static com.dalcoomi.common.error.model.ErrorMessage.TEAM_MEMBER_ALREADY_EXISTS;
import static com.dalcoomi.common.error.model.ErrorMessage.TEAM_MEMBER_COUNT_EXCEEDED;
import static com.dalcoomi.common.error.model.ErrorMessage.TEAM_MEMBER_NOT_ENOUGH_MAX_COUNT;
import static com.dalcoomi.common.error.model.ErrorMessage.TEAM_MEMBER_NOT_FOUND;
import static com.dalcoomi.common.error.model.ErrorMessage.TEAM_NOT_FOUND;
import static com.dalcoomi.team.constant.TeamConstants.MAX_TEAM_LIMIT;
import static com.dalcoomi.team.domain.Team.generateInvitationCode;
import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dalcoomi.common.error.exception.BadRequestException;
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
	public String create(Long memberId, Team team) {
		Member member = memberRepository.findById(memberId);

		validateMaxTeamCount(memberId);

		String uniqueCode = findUniqueInvitationCode();

		team.updateLeader(member);
		team.updateInvitationCode(uniqueCode);

		Team savedTeam = teamRepository.save(team);

		TeamMember teamMember = TeamMember.of(savedTeam, member);

		teamMemberRepository.save(teamMember);

		return savedTeam.getInvitationCode();
	}

	@Transactional
	public Team join(Long memberId, String invitationCode) {
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

		return team;
	}

	@Transactional(readOnly = true)
	public TeamsInfo get(Long memberId) {
		List<TeamMember> teamMembers = teamMemberRepository.find(null, memberId);

		// displayOrder 기준으로 정렬, displayOrder가 같으면 id 역순 정렬
		List<TeamMember> sortedTeamMembers = teamMembers.stream()
			.sorted((tm1, tm2) -> {
				int orderCompare = Integer.compare(tm1.getDisplayOrder(), tm2.getDisplayOrder());

				if (orderCompare != 0) {
					return orderCompare;
				}

				return Long.compare(tm2.getId(), tm1.getId());
			})
			.toList();

		List<Team> teams = sortedTeamMembers.stream().map(TeamMember::getTeam).toList();
		List<Integer> displayOrders = sortedTeamMembers.stream().map(TeamMember::getDisplayOrder).toList();
		List<Long> teamIds = teams.stream().map(Team::getId).toList();
		List<Integer> memberCounts = teams.stream()
			.map(team -> teamMemberRepository.countByTeamIds(teamIds).get(team.getId()))
			.toList();

		return TeamsInfo.of(teams, memberCounts, displayOrders);
	}

	@Transactional(readOnly = true)
	public TeamInfo getTeamInfo(Long teamId, Long memberId) {
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
	public void update(Long memberId, Team team) {
		Team currentTeam = teamRepository.findById(team.getId());
		int currentTeamMemberCount = teamMemberRepository.countByTeamId(currentTeam.getId());

		if (!currentTeam.getLeader().getId().equals(memberId)) {
			throw new BadRequestException(TEAM_INVALID_LEADER);
		}

		if (currentTeamMemberCount > team.getMemberLimit()) {
			throw new BadRequestException(TEAM_MEMBER_NOT_ENOUGH_MAX_COUNT);
		}

		currentTeam.updateTitle(team.getTitle());
		currentTeam.updateMemberLimit(team.getMemberLimit());
		currentTeam.updateLabel(team.getLabel());
		currentTeam.updatePurpose(team.getPurpose());

		teamRepository.save(currentTeam);
	}

	@Transactional
	public void updateDisplayOrder(Long memberId, Map<Long, Integer> teamIdsAndDisplayOrders) {
		List<TeamMember> teamMembers = teamMemberRepository.find(null, memberId);
		Set<Long> memberTeamIds = teamMembers.stream()
			.map(tm -> tm.getTeam().getId())
			.collect(toSet());
		Set<Long> requestedTeamIds = teamIdsAndDisplayOrders.keySet();
		long distinctCount = teamIdsAndDisplayOrders.values().stream().distinct().count();

		// 요청한 teamId가 모두 사용자가 속한 그룹인지 검증
		if (!memberTeamIds.containsAll(requestedTeamIds)) {
			throw new NotFoundException(TEAM_MEMBER_NOT_FOUND);
		}

		// displayOrder 중복 검증
		if (distinctCount != teamIdsAndDisplayOrders.size()) {
			throw new BadRequestException(TEAM_DISPLAY_ORDER_DUPLICATED);
		}

		List<TeamMember> updatedTeamMembers = new ArrayList<>();

		for (TeamMember teamMember : teamMembers) {
			Long teamId = teamMember.getTeam().getId();

			if (teamIdsAndDisplayOrders.containsKey(teamId)) {
				teamMember.updateDisplayOrder(teamIdsAndDisplayOrders.get(teamId));
			}

			updatedTeamMembers.add(teamMember);
		}

		teamMemberRepository.saveAll(updatedTeamMembers);
	}

	@Transactional
	public void leave(LeaveTeamInfo leaveTeamInfo, Long memberId) {
		Long teamId = leaveTeamInfo.teamId();
		String nextLeaderNickname = leaveTeamInfo.nextLeaderNickname();
		List<TeamMember> teamMember = teamMemberRepository.find(teamId, memberId);

		if (teamMember.isEmpty()) {
			throw new NotFoundException(TEAM_NOT_FOUND);
		}

		Team team = teamMember.getFirst().getTeam();
		Long leaderId = team.getLeader().getId();

		// 요청한 사람이 그룹 리더일 경우 새 리더 지정
		if (leaderId.equals(memberId) && nextLeaderNickname != null) {
			Member nextLeader = memberRepository.findByNickname(nextLeaderNickname);

			team.updateLeader(nextLeader);

			teamRepository.save(team);
		}

		teamMemberRepository.deleteByTeamIdAndMemberId(teamId, memberId);

		if (teamMemberRepository.countByTeamId(teamId) == 0) {
			transactionRepository.deleteByTeamId(teamId);
			teamRepository.deleteById(teamId);
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
