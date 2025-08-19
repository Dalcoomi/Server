package com.dalcoomi.team.infrastructure;

import static com.dalcoomi.common.jpa.DynamicQuery.generateEq;
import static com.dalcoomi.member.infrastructure.QMemberJpaEntity.memberJpaEntity;
import static com.dalcoomi.team.infrastructure.QTeamJpaEntity.teamJpaEntity;
import static com.dalcoomi.team.infrastructure.QTeamMemberJpaEntity.teamMemberJpaEntity;
import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import com.dalcoomi.team.application.repository.TeamMemberRepository;
import com.dalcoomi.team.domain.TeamMember;
import com.dalcoomi.team.dto.QTeamMemberProjection_TeamMemberCountDto;
import com.dalcoomi.team.dto.TeamMemberProjection;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TeamMemberRepositoryImpl implements TeamMemberRepository {

	private final TeamMemberJpaRepository teamMemberJpaRepository;
	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public TeamMember save(TeamMember teamMember) {
		return teamMemberJpaRepository.save(TeamMemberJpaEntity.from(teamMember)).toModel();
	}

	@Override
	public List<TeamMember> saveAll(List<TeamMember> teamMembers) {
		List<TeamMemberJpaEntity> teamMemberJpaEntities = teamMemberJpaRepository.saveAll(
			teamMembers.stream().map(TeamMemberJpaEntity::from).toList());

		return teamMemberJpaEntities.stream().map(TeamMemberJpaEntity::toModel).toList();
	}

	@Override
	public boolean existsByTeamIdAndMemberId(Long teamId, Long memberId) {
		return teamMemberJpaRepository.existsByTeamIdAndMemberId(teamId, memberId);
	}

	@Override
	public List<TeamMember> find(@Nullable Long teamId, @Nullable Long memberId) {
		return jpaQueryFactory
			.selectFrom(teamMemberJpaEntity)
			.join(teamMemberJpaEntity.team, teamJpaEntity).fetchJoin()
			.join(teamMemberJpaEntity.member, memberJpaEntity).fetchJoin()
			.where(
				generateEq(teamId, teamJpaEntity.id::eq),
				generateEq(memberId, memberJpaEntity.id::eq),
				memberJpaEntity.deletedAt.isNull()
			)
			.fetch()
			.stream()
			.map(TeamMemberJpaEntity::toModel)
			.toList();
	}

	@Override
	public int countByTeamId(Long teamId) {
		return teamMemberJpaRepository.countByTeamId(teamId);
	}

	@Override
	public int countByMemberId(Long memberId) {
		return teamMemberJpaRepository.countByMemberId(memberId);
	}

	@Override
	public Map<Long, Integer> countByTeamIds(List<Long> teamIds) {
		List<TeamMemberProjection.TeamMemberCountDto> results = jpaQueryFactory
			.select(new QTeamMemberProjection_TeamMemberCountDto(
				teamMemberJpaEntity.team.id,
				teamMemberJpaEntity.count().castToNum(Integer.class)
			))
			.from(teamMemberJpaEntity)
			.join(teamMemberJpaEntity.member, memberJpaEntity)
			.where(teamMemberJpaEntity.team.id.in(teamIds), memberJpaEntity.deletedAt.isNull())
			.groupBy(teamMemberJpaEntity.team.id)
			.fetch();

		return results.stream()
			.collect(toMap(
				TeamMemberProjection.TeamMemberCountDto::teamId,
				TeamMemberProjection.TeamMemberCountDto::count
			));
	}

	@Override
	public void deleteByTeamIdAndMemberId(Long teamId, Long memberId) {
		teamMemberJpaRepository.deleteByTeamIdAndMemberId(teamId, memberId);
	}
}
