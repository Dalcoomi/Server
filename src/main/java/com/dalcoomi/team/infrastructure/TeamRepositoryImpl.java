package com.dalcoomi.team.infrastructure;

import static com.dalcoomi.common.error.model.ErrorMessage.TEAM_NOT_FOUND;
import static java.util.stream.Collectors.toSet;

import java.util.Set;

import org.springframework.stereotype.Repository;

import com.dalcoomi.common.error.exception.NotFoundException;
import com.dalcoomi.member.infrastructure.MemberJpaEntity;
import com.dalcoomi.team.application.repository.TeamRepository;
import com.dalcoomi.team.domain.Team;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TeamRepositoryImpl implements TeamRepository {

	private final TeamJpaRepository teamJpaRepository;
	private final EntityManager entityManager;

	@Override
	public Team save(Team team) {
		TeamJpaEntity teamJpaEntity;

		if (team.getId() != null) {
			// 기존 엔티티 업데이트: leader를 getReference()로 설정하여 프록시 사용
			MemberJpaEntity leaderReference = entityManager.getReference(MemberJpaEntity.class,
				team.getLeader().getId());

			teamJpaEntity = TeamJpaEntity.builder()
				.id(team.getId())
				.leader(leaderReference)
				.title(team.getTitle())
				.invitationCode(team.getInvitationCode())
				.memberLimit(team.getMemberLimit())
				.purpose(team.getPurpose())
				.build();
		} else {
			// 새로운 엔티티 생성
			teamJpaEntity = TeamJpaEntity.from(team);
		}

		return teamJpaRepository.save(teamJpaEntity).toModel();
	}

	@Override
	public boolean existsById(Long teamId) {
		return teamJpaRepository.existsById(teamId);
	}

	@Override
	public Set<String> findExistingCodes(Set<String> candidates) {
		return teamJpaRepository.findByInvitationCodeIn(candidates)
			.stream().map(TeamJpaEntity::getInvitationCode).collect(toSet());
	}

	@Override
	public Team findById(Long teamId) {
		return teamJpaRepository.findById(teamId).orElseThrow(() -> new NotFoundException(TEAM_NOT_FOUND)).toModel();
	}

	@Override
	public Team findByInvitationCode(String invitationCode) {
		return teamJpaRepository.findByInvitationCode(invitationCode)
			.orElseThrow(() -> new NotFoundException(TEAM_NOT_FOUND)).toModel();
	}

	@Override
	public void deleteById(Long teamId) {
		teamJpaRepository.deleteById(teamId);
	}
}
