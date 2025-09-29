package com.dalcoomi.member.infrastructure;

import static com.dalcoomi.common.error.model.ErrorMessage.SOCIAL_CONNECTION_NOT_FOUND;
import static com.dalcoomi.member.infrastructure.QMemberJpaEntity.memberJpaEntity;
import static com.dalcoomi.member.infrastructure.QSocialConnectionJpaEntity.socialConnectionJpaEntity;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.dalcoomi.common.encryption.HashService;
import com.dalcoomi.common.error.exception.NotFoundException;
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
	private final HashService hashService;

	@Override
	public SocialConnection save(SocialConnection socialConnection) {
		SocialConnectionJpaEntity entity = SocialConnectionJpaEntity.from(socialConnection);

		entity = SocialConnectionJpaEntity.builder()
			.id(entity.getId())
			.member(entity.getMember())
			.socialEmail(entity.getSocialEmail())
			.socialEmailHash(hashService.hash(socialConnection.getSocialEmail()))
			.socialId(entity.getSocialId())
			.socialIdHash(hashService.hash(socialConnection.getSocialId()))
			.socialRefreshToken(entity.getSocialRefreshToken())
			.socialType(entity.getSocialType())
			.deletedAt(entity.getDeletedAt())
			.build();

		return socialConnectionJpaRepository.save(entity).toModel();
	}

	@Override
	public void saveAll(List<SocialConnection> socialConnections) {
		List<SocialConnectionJpaEntity> entities = socialConnections.stream()
			.map(socialConnection -> {
				SocialConnectionJpaEntity entity = SocialConnectionJpaEntity.from(socialConnection);

				return SocialConnectionJpaEntity.builder()
					.id(entity.getId())
					.member(entity.getMember())
					.socialEmail(entity.getSocialEmail())
					.socialEmailHash(hashService.hash(socialConnection.getSocialEmail()))
					.socialId(entity.getSocialId())
					.socialIdHash(hashService.hash(socialConnection.getSocialId()))
					.socialRefreshToken(entity.getSocialRefreshToken())
					.socialType(entity.getSocialType())
					.deletedAt(entity.getDeletedAt())
					.build();
			})
			.toList();

		socialConnectionJpaRepository.saveAll(entities);
	}

	@Override
	public Boolean existsMemberBySocialIdAndSocialType(String socialId, SocialType socialType) {
		String socialIdHash = hashService.hash(socialId);

		return socialConnectionJpaRepository.existsBySocialIdHashAndSocialType(socialIdHash, socialType);
	}

	@Override
	public SocialConnection findByMemberIdAndSocialType(Long memberId, SocialType socialType) {
		return socialConnectionJpaRepository.findByMemberIdAndSocialType(memberId, socialType)
			.orElseThrow(() -> new NotFoundException(SOCIAL_CONNECTION_NOT_FOUND))
			.toModel();
	}

	@Override
	public List<SocialConnection> findBySocialEmailOrSocialId(String socialEmail, String socialId) {
		String socialEmailHash = hashService.hash(socialEmail);
		String socialIdHash = hashService.hash(socialId);

		return jpaQueryFactory
			.selectDistinct(socialConnectionJpaEntity)
			.from(socialConnectionJpaEntity)
			.join(socialConnectionJpaEntity.member, memberJpaEntity)
			.where(
				socialConnectionJpaEntity.socialEmailHash.eq(socialEmailHash)
					.or(socialConnectionJpaEntity.socialIdHash.eq(socialIdHash))
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
