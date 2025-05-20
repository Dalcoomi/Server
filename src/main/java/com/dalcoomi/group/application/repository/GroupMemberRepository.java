package com.dalcoomi.group.application.repository;

import com.dalcoomi.group.domain.GroupMember;

public interface GroupMemberRepository {

	GroupMember save(GroupMember groupMember);

	boolean existsByGroupIdAndMemberId(Long groupId, Long memberId);

	int countByGroupId(Long groupId);
}
