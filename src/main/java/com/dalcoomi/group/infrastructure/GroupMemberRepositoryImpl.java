package com.dalcoomi.group.infrastructure;

import org.springframework.stereotype.Repository;

import com.dalcoomi.group.application.repository.GroupMemberRepository;
import com.dalcoomi.group.domain.GroupMember;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class GroupMemberRepositoryImpl implements GroupMemberRepository {

	private final GroupMemberJpaRepository groupMemberJpaRepository;

	@Override
	public GroupMember save(GroupMember groupMember) {
		return groupMemberJpaRepository.save(GroupMemberJpaEntity.from(groupMember)).toModel();
	}

	@Override
	public boolean existsByGroupIdAndMemberId(Long groupId, Long memberId) {
		return groupMemberJpaRepository.existsByGroupIdAndMemberIdAndDeletedAtIsNull(groupId, memberId);
	}

	@Override
	public int countByGroupId(Long groupId) {
		return groupMemberJpaRepository.countByGroupIdAndDeletedAtIsNull(groupId);
	}
}
