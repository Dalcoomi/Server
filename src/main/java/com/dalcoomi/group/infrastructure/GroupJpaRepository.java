package com.dalcoomi.group.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupJpaRepository extends JpaRepository<GroupJpaEntity, Long> {

	List<GroupJpaEntity> findByInvitationCodeInAndDeletedAtIsNull(Set<String> candidates);

	Optional<GroupJpaEntity> findByInvitationCodeAndDeletedAtIsNull(String invitationCode);
}
