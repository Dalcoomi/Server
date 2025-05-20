package com.dalcoomi.group.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupMemberJpaRepository extends JpaRepository<GroupMemberJpaEntity, Long> {

	boolean existsByGroupIdAndMemberIdAndDeletedAtIsNull(Long groupId, Long memberId);

	int countByGroupIdAndDeletedAtIsNull(Long groupId);
}
