package com.dalcoomi.group.application.repository;

import java.util.Set;

import com.dalcoomi.group.domain.Group;

public interface GroupRepository {

	Group save(Group group);

	Set<String> findExistingCodes(Set<String> candidates);

	Group findByInvitationCode(String invitationCode);
}
