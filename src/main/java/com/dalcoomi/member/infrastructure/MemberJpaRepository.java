package com.dalcoomi.member.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberJpaRepository extends JpaRepository<MemberJpaEntity, Long> {

	boolean existsByNickname(String nickname);

	Optional<MemberJpaEntity> findByIdAndDeletedAtIsNull(Long memberId);

	List<MemberJpaEntity> findAllByIdInAndDeletedAtIsNull(List<Long> memberIds);

	Optional<MemberJpaEntity> findByNicknameAndDeletedAtIsNull(String nextLeaderNickname);

}
