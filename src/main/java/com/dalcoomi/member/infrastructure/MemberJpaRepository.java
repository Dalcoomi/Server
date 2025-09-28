package com.dalcoomi.member.infrastructure;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberJpaRepository extends JpaRepository<MemberJpaEntity, Long> {

	boolean existsByEmailHash(String emailHash);

	boolean existsByNickname(String nickname);

	Optional<MemberJpaEntity> findByIdAndDeletedAtIsNull(Long memberId);

	Optional<MemberJpaEntity> findByNicknameAndDeletedAtIsNull(String nextLeaderNickname);

	Optional<MemberJpaEntity> findByEmailHashAndDeletedAtIsNull(String emailHash);
}
