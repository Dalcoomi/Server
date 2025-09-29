package com.dalcoomi.member.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MemberJpaRepository extends JpaRepository<MemberJpaEntity, Long> {

	boolean existsByEmailHash(String emailHash);

	boolean existsByNickname(String nickname);

	Optional<MemberJpaEntity> findByIdAndDeletedAtIsNull(Long memberId);

	Optional<MemberJpaEntity> findByNicknameAndDeletedAtIsNull(String nextLeaderNickname);

	Optional<MemberJpaEntity> findByEmailHashAndDeletedAtIsNull(String emailHash);

	@Query("""
		SELECT m
		FROM MemberJpaEntity m
		WHERE (m.emailHash IS NULL OR m.emailHash = '')
		OR (m.nameHash IS NULL OR m.nameHash = '')
		OR (m.birthdayHash IS NULL AND m.birthday IS NOT NULL)
		OR (m.genderHash IS NULL AND m.gender IS NOT NULL)
		""")
	List<MemberJpaEntity> findPlainTextMembers(Pageable pageable);
}
