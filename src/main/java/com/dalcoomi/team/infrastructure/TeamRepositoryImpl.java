package com.dalcoomi.team.infrastructure;

import static com.dalcoomi.common.error.model.ErrorMessage.TEAM_NOT_FOUND;
import static java.util.stream.Collectors.toSet;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	private static final Logger log = LoggerFactory.getLogger(TeamRepositoryImpl.class);

	private final TeamJpaRepository teamJpaRepository;
	private final EntityManager entityManager;

	@Override
	public Team save(Team team) {
		log.info("=== TeamRepositoryImpl.save START ===");
		log.info("Team ID: {}, Leader ID: {}", team.getId(), team.getLeader().getId());

		TeamJpaEntity teamJpaEntity;

		if (team.getId() != null) {
			log.info("Existing team - using getReference()");
			// 기존 엔티티 업데이트: leader를 getReference()로 설정하여 프록시 사용
			MemberJpaEntity leaderReference = entityManager.getReference(MemberJpaEntity.class,
				team.getLeader().getId());

			log.info("Created leader reference: {}", leaderReference);

			teamJpaEntity = TeamJpaEntity.builder()
				.id(team.getId())
				.leader(leaderReference)
				.title(team.getTitle())
				.invitationCode(team.getInvitationCode())
				.memberLimit(team.getMemberLimit())
				.purpose(team.getPurpose())
				.build();
		} else {
			log.info("New team - using from()");
			// 새로운 엔티티 생성
			teamJpaEntity = TeamJpaEntity.from(team);
		}

		log.info("Calling teamJpaRepository.save...");
		Team savedTeam = teamJpaRepository.save(teamJpaEntity).toModel();
		log.info("=== TeamRepositoryImpl.save END ===");
		return savedTeam;
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
