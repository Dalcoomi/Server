package com.dalcoomi.member.infrastructure;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dalcoomi.member.domain.SocialType;

public interface SocialConnectionJpaRepository extends JpaRepository<SocialConnectionJpaEntity, Long> {

	boolean existsBySocialIdAndSocialType(String socialId, SocialType socialType);

	List<SocialConnectionJpaEntity> findByMemberId(Long memberId);

	void deleteByMemberId(Long memberId);
}
