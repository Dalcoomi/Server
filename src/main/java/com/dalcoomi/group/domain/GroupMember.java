package com.dalcoomi.group.domain;

import java.time.LocalDateTime;

import com.dalcoomi.member.domain.Member;

import lombok.Builder;
import lombok.Getter;

@Getter
public class GroupMember {

	private final Long id;
	private final LocalDateTime deletedAt;
	private final Group group;
	private final Member member;

	@Builder
	public GroupMember(Long id, Group group, Member member, LocalDateTime deletedAt) {
		this.id = id;
		this.group = group;
		this.member = member;
		this.deletedAt = deletedAt;
	}

	public static GroupMember of(Group group, Member member) {
		return GroupMember.builder()
			.group(group)
			.member(member)
			.build();
	}
}
