package com.dalcoomi.member.infrastructure;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dalcoomi.member.domain.SocialType;

public interface SocialConnectionJpaRepository extends JpaRepository<SocialConnectionJpaEntity, Long> {

	boolean existsBySocialIdAndSocialType(String socialId, SocialType socialType);

	Optional<SocialConnectionJpaEntity> findByMemberId(Long memberId);

	Optional<SocialConnectionJpaEntity> findBySocialIdAndSocialType(String socialId, SocialType socialType);

	void deleteByMemberId(Long memberId);
}
