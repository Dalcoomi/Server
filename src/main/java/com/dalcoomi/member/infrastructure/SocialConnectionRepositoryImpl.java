package com.dalcoomi.member.infrastructure;

import static com.dalcoomi.member.infrastructure.QMemberJpaEntity.memberJpaEntity;
import static com.dalcoomi.member.infrastructure.QSocialConnectionJpaEntity.socialConnectionJpaEntity;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.dalcoomi.member.application.repository.SocialConnectionRepository;
import com.dalcoomi.member.domain.SocialConnection;
import com.dalcoomi.member.domain.SocialType;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SocialConnectionRepositoryImpl implements SocialConnectionRepository {

	private final SocialConnectionJpaRepository socialConnectionJpaRepository;
	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public SocialConnection save(SocialConnection socialConnection) {
		return socialConnectionJpaRepository.save(SocialConnectionJpaEntity.from(socialConnection)).toModel();
	}

	@Override
	public void saveAll(List<SocialConnection> socialConnections) {
		List<SocialConnectionJpaEntity> entities = socialConnections.stream()
			.map(SocialConnectionJpaEntity::from)
			.toList();

		socialConnectionJpaRepository.saveAll(entities);
	}

	@Override
	public Boolean existsMemberBySocialIdAndSocialType(String socialId, SocialType socialType) {
		return socialConnectionJpaRepository.existsBySocialIdAndSocialType(socialId, socialType);
	}

	@Override
	public List<SocialConnection> findBySocialEmailOrSocialId(String socialEmail, String socialId) {
		return jpaQueryFactory
			.selectDistinct(socialConnectionJpaEntity)
			.from(socialConnectionJpaEntity)
			.join(socialConnectionJpaEntity.member, memberJpaEntity)
			.where(
				socialConnectionJpaEntity.socialEmail.eq(socialEmail)
					.or(socialConnectionJpaEntity.socialId.eq(socialId))
			)
			.fetch()
			.stream()
			.map(SocialConnectionJpaEntity::toModel)
			.toList();
	}

	@Override
	public List<SocialConnection> findByMemberId(Long memberId) {
		return socialConnectionJpaRepository.findByMemberId(memberId)
			.stream()
			.map(SocialConnectionJpaEntity::toModel)
			.toList();
	}

	@Override
	public List<SocialConnection> findExpiredSoftDeletedWithMember(LocalDateTime cutoffDate) {
		return jpaQueryFactory
			.selectFrom(socialConnectionJpaEntity)
			.join(socialConnectionJpaEntity.member, memberJpaEntity).fetchJoin()
			.where(
				socialConnectionJpaEntity.deletedAt.isNotNull(),
				socialConnectionJpaEntity.deletedAt.lt(cutoffDate),
				memberJpaEntity.deletedAt.isNotNull(),
				memberJpaEntity.deletedAt.lt(cutoffDate)
			)
			.fetch()
			.stream()
			.map(SocialConnectionJpaEntity::toModel)
			.toList();
	}

	@Override
	public void deleteById(Long socialConnectionId) {
		socialConnectionJpaRepository.deleteById(socialConnectionId);
	}

	@Override
	public void deleteByMemberId(Long memberId) {
		socialConnectionJpaRepository.deleteByMemberId(memberId);
	}

	@Override
	public void deleteAll(List<SocialConnection> socialConnections) {
		List<SocialConnectionJpaEntity> entities = socialConnections.stream()
			.map(SocialConnectionJpaEntity::from)
			.toList();

		socialConnectionJpaRepository.deleteAll(entities);
	}
}
