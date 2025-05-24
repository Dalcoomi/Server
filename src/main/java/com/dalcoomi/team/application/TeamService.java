package com.dalcoomi.team.application;

import static com.dalcoomi.common.error.model.ErrorMessage.TEAM_MEMBER_ALREADY_EXISTS;
import static com.dalcoomi.common.error.model.ErrorMessage.TEAM_MEMBER_COUNT_EXCEEDED;
import static com.dalcoomi.team.domain.Team.generateInvitationCode;
import static java.util.stream.Collectors.toSet;

import java.util.Set;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dalcoomi.common.error.exception.ConflictException;
import com.dalcoomi.member.application.repository.MemberRepository;
import com.dalcoomi.member.domain.Member;
import com.dalcoomi.team.application.repository.TeamMemberRepository;
import com.dalcoomi.team.application.repository.TeamRepository;
import com.dalcoomi.team.domain.Team;
import com.dalcoomi.team.domain.TeamMember;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TeamService {

	private final TeamRepository teamRepository;
	private final MemberRepository memberRepository;
	private final TeamMemberRepository teamMemberRepository;

	@Transactional
	public String createTeam(Long memberId, Team team) {
		Member member = memberRepository.findById(memberId);
		String uniqueCode = findUniqueInvitationCode();

		team.updateMember(member);
		team.updateInvitationCode(uniqueCode);

		Team savedTeam = teamRepository.save(team);

		return savedTeam.getInvitationCode();
	}

	@Transactional
	public void joinTeam(Long memberId, String invitationCode) {
		Member member = memberRepository.findById(memberId);
		Team team = teamRepository.findByInvitationCode(invitationCode);

		if (teamMemberRepository.existsByTeamIdAndMemberId(team.getId(), memberId)) {
			throw new ConflictException(TEAM_MEMBER_ALREADY_EXISTS);
		}

		int currentMemberCount = teamMemberRepository.countByTeamId(team.getId());

		if (currentMemberCount >= team.getMemberLimit()) {
			throw new ConflictException(TEAM_MEMBER_COUNT_EXCEEDED);
		}

		TeamMember teamMember = TeamMember.of(team, member);

		teamMemberRepository.save(teamMember);
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
}
