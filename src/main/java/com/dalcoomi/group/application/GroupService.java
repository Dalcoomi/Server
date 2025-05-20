package com.dalcoomi.group.application;

import static com.dalcoomi.common.error.model.ErrorMessage.GROUP_MEMBER_ALREADY_EXISTS;
import static com.dalcoomi.common.error.model.ErrorMessage.GROUP_MEMBER_COUNT_EXCEEDED;
import static com.dalcoomi.group.domain.Group.generateInvitationCode;
import static java.util.stream.Collectors.toSet;

import java.util.Set;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dalcoomi.common.error.exception.ConflictException;
import com.dalcoomi.group.application.repository.GroupMemberRepository;
import com.dalcoomi.group.application.repository.GroupRepository;
import com.dalcoomi.group.domain.Group;
import com.dalcoomi.group.domain.GroupMember;
import com.dalcoomi.member.application.repository.MemberRepository;
import com.dalcoomi.member.domain.Member;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GroupService {

	private final GroupRepository groupRepository;
	private final MemberRepository memberRepository;
	private final GroupMemberRepository groupMemberRepository;

	@Transactional
	public String createGroup(Long memberId, Group group) {
		Member member = memberRepository.findById(memberId);
		String uniqueCode = findUniqueInvitationCode();

		group.updateMember(member);
		group.updateInvitationCode(uniqueCode);

		Group savedGroup = groupRepository.save(group);

		return savedGroup.getInvitationCode();
	}

	@Transactional
	public void joinGroup(Long memberId, String invitationCode) {
		Member member = memberRepository.findById(memberId);
		Group group = groupRepository.findByInvitationCode(invitationCode);

		if (groupMemberRepository.existsByGroupIdAndMemberId(group.getId(), memberId)) {
			throw new ConflictException(GROUP_MEMBER_ALREADY_EXISTS);
		}

		int currentMemberCount = groupMemberRepository.countByGroupId(group.getId());

		if (currentMemberCount >= group.getCount()) {
			throw new ConflictException(GROUP_MEMBER_COUNT_EXCEEDED);
		}

		GroupMember groupMember = GroupMember.of(group, member);

		groupMemberRepository.save(groupMember);
	}

	private String findUniqueInvitationCode() {
		Set<String> candidates = IntStream.range(0, 10)
			.mapToObj(i -> generateInvitationCode())
			.collect(toSet());

		Set<String> existingCodes = groupRepository.findExistingCodes(candidates);

		return candidates.stream()
			.filter(code -> !existingCodes.contains(code))
			.findFirst()
			.orElseGet(Group::generateInvitationCode);
	}
}
