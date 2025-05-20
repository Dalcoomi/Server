package com.dalcoomi.group.infrastructure;

import static com.dalcoomi.common.error.model.ErrorMessage.GROUP_NOT_FOUND;
import static java.util.stream.Collectors.toSet;

import java.util.Set;

import org.springframework.stereotype.Repository;

import com.dalcoomi.common.error.exception.NotFoundException;
import com.dalcoomi.group.application.repository.GroupRepository;
import com.dalcoomi.group.domain.Group;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class GroupRepositoryImpl implements GroupRepository {

	private final GroupJpaRepository groupJpaRepository;

	@Override
	public Group save(Group group) {
		return groupJpaRepository.save(GroupJpaEntity.from(group)).toModel();
	}

	@Override
	public Set<String> findExistingCodes(Set<String> candidates) {
		return groupJpaRepository.findByInvitationCodeInAndDeletedAtIsNull(candidates)
			.stream()
			.map(GroupJpaEntity::getInvitationCode)
			.collect(toSet());
	}

	@Override
	public Group findByInvitationCode(String invitationCode) {
		return groupJpaRepository.findByInvitationCodeAndDeletedAtIsNull(invitationCode)
			.orElseThrow(() -> new NotFoundException(GROUP_NOT_FOUND)).toModel();
	}
}
