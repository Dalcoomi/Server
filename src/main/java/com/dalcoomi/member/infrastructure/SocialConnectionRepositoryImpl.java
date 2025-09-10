package com.dalcoomi.member.infrastructure;

import static com.dalcoomi.common.jpa.DynamicQuery.generateEq;
import static com.dalcoomi.member.infrastructure.QMemberJpaEntity.memberJpaEntity;
import static com.dalcoomi.member.infrastructure.QSocialConnectionJpaEntity.socialConnectionJpaEntity;

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
	public Boolean existsMemberBySocialIdAndSocialType(String socialId, SocialType socialType) {
		return socialConnectionJpaRepository.existsBySocialIdAndSocialType(socialId, socialType);
	}

	@Override
	public List<SocialConnection> findBySocialEmail(String socialEmail) {
		return jpaQueryFactory
			.selectFrom(socialConnectionJpaEntity)
			.join(socialConnectionJpaEntity.member, memberJpaEntity)
			.where(
				generateEq(socialEmail, socialConnectionJpaEntity.socialEmail::eq),
				socialConnectionJpaEntity.deletedAt.isNull()
			)
			.fetch()
			.stream().map(SocialConnectionJpaEntity::toModel).toList();
	}

	@Override
	public List<SocialConnection> findByMemberId(Long memberId) {
		return socialConnectionJpaRepository.findByMemberId(memberId)
			.stream()
			.map(SocialConnectionJpaEntity::toModel)
			.toList();
	}

	@Override
	public void deleteByMemberId(Long memberId) {
		socialConnectionJpaRepository.deleteByMemberId(memberId);
	}
}
