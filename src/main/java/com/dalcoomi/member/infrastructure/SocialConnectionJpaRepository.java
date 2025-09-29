package com.dalcoomi.member.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.dalcoomi.member.domain.SocialType;

public interface SocialConnectionJpaRepository extends JpaRepository<SocialConnectionJpaEntity, Long> {

	boolean existsBySocialIdHashAndSocialType(String socialIdHash, SocialType socialType);

	Optional<SocialConnectionJpaEntity> findByMemberIdAndSocialType(Long memberId, SocialType socialType);

	List<SocialConnectionJpaEntity> findByMemberId(Long memberId);

	void deleteByMemberId(Long memberId);

	@Query("""
		SELECT sc
		FROM SocialConnectionJpaEntity sc
		WHERE (sc.socialEmailHash IS NULL OR sc.socialEmailHash = '')
		OR (sc.socialIdHash IS NULL OR sc.socialIdHash = '')
		""")
	List<SocialConnectionJpaEntity> findPlainTextConnections(Pageable pageable);
}
