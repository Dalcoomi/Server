package com.dalcoomi.group.domain;

import static com.dalcoomi.common.error.model.ErrorMessage.GROUP_INVALID_COUNT;
import static com.dalcoomi.common.error.model.ErrorMessage.GROUP_INVALID_GOAL;
import static io.micrometer.common.util.StringUtils.isBlank;
import static java.lang.String.format;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import com.dalcoomi.group.dto.request.GroupRequest;
import com.dalcoomi.member.domain.Member;

import lombok.Builder;
import lombok.Getter;

@Getter
public class Group {

	public static final int INVITATION_CODE_LENGTH = 8;
	public static final int MAX_MEMBER_COUNT = 10;
	public static final int GOAL_LENGTH = 100;

	private final Long id;
	private final String title;
	private final Integer count;
	private final String goal;
	private final LocalDateTime deletedAt;
	private Member member;
	private String invitationCode;

	@Builder
	public Group(Long id, Member member, String title, String invitationCode, Integer count, String goal,
		LocalDateTime deletedAt) {
		this.id = id;
		this.member = member;
		this.title = title;
		this.invitationCode = invitationCode;
		this.count = validateCount(count);
		this.goal = validateGoal(goal);
		this.deletedAt = deletedAt;
	}

	public static Group from(GroupRequest request) {
		return Group.builder()
			.title(request.title())
			.count(request.count())
			.goal(request.goal())
			.build();
	}

	public static String generateInvitationCode() {
		String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		SecureRandom random = new SecureRandom();
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < INVITATION_CODE_LENGTH; i++) {
			sb.append(chars.charAt(random.nextInt(chars.length())));
		}

		return sb.toString();
	}

	public void updateMember(Member member) {
		this.member = member;
	}

	public void updateInvitationCode(String invitationCode) {
		this.invitationCode = invitationCode;
	}

	private Integer validateCount(Integer count) {
		if (count == null || count < 1 || count > MAX_MEMBER_COUNT) {
			throw new IllegalArgumentException(format(GROUP_INVALID_COUNT.getMessage(), MAX_MEMBER_COUNT));
		}

		return count;
	}

	private String validateGoal(String content) {
		if (!isBlank(content) && content.length() > GOAL_LENGTH) {
			throw new IllegalArgumentException(GROUP_INVALID_GOAL.getMessage());
		}

		return content;
	}
}
