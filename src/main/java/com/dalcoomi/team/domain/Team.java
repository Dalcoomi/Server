package com.dalcoomi.team.domain;

import static com.dalcoomi.common.error.model.ErrorMessage.TEAM_INVALID_LABEL;
import static com.dalcoomi.common.error.model.ErrorMessage.TEAM_INVALID_MEMBER_LIMIT;
import static com.dalcoomi.common.error.model.ErrorMessage.TEAM_INVALID_PURPOSE;
import static com.dalcoomi.common.error.model.ErrorMessage.TEAM_INVALID_TITLE;
import static com.dalcoomi.team.constant.TeamConstants.INVITATION_CODE_LENGTH;
import static com.dalcoomi.team.constant.TeamConstants.MAX_MEMBER_LIMIT;
import static com.dalcoomi.team.constant.TeamConstants.PURPOSE_LENGTH;
import static com.dalcoomi.team.constant.TeamConstants.TITLE_LENGTH;
import static io.micrometer.common.util.StringUtils.isBlank;
import static java.lang.String.format;

import java.security.SecureRandom;

import com.dalcoomi.member.domain.Member;
import com.dalcoomi.team.dto.request.TeamRequest;

import lombok.Builder;
import lombok.Getter;

@Getter
public class Team {

	private final Long id;
	private Member leader;
	private String title;
	private String invitationCode;
	private Integer memberLimit;
	private String label;
	private String purpose;

	@Builder
	public Team(Long id, Member leader, String title, String invitationCode, Integer memberLimit, String label,
		String purpose) {
		this.id = id;
		this.leader = leader;
		this.title = validateTitle(title);
		this.invitationCode = invitationCode;
		this.memberLimit = validateMemberLimit(memberLimit);
		this.label = validateLabel(label);
		this.purpose = validatePurpose(purpose);
	}

	public static Team from(TeamRequest request) {
		return Team.builder()
			.id(request.teamId())
			.title(request.title())
			.memberLimit(request.memberLimit())
			.label(request.label())
			.purpose(request.purpose())
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

	private String validateTitle(String title) {
		if (!isBlank(title) && title.length() > TITLE_LENGTH) {
			throw new IllegalArgumentException(TEAM_INVALID_TITLE.getMessage());
		}

		return title;
	}

	private Integer validateMemberLimit(Integer memberLimit) {
		if (memberLimit == null || memberLimit < 1 || memberLimit > MAX_MEMBER_LIMIT) {
			throw new IllegalArgumentException(format(TEAM_INVALID_MEMBER_LIMIT.getMessage(), MAX_MEMBER_LIMIT));
		}

		return memberLimit;
	}

	private String validateLabel(String label) {
		if (isBlank(label)) {
			throw new IllegalArgumentException(TEAM_INVALID_LABEL.getMessage());
		}

		return label;
	}

	private String validatePurpose(String purpose) {
		if (!isBlank(purpose) && purpose.length() > PURPOSE_LENGTH) {
			throw new IllegalArgumentException(TEAM_INVALID_PURPOSE.getMessage());
		}

		return purpose;
	}

	public void updateLeader(Member member) {
		this.leader = member;
	}

	public void updateInvitationCode(String invitationCode) {
		this.invitationCode = invitationCode;
	}

	public void updateTitle(String title) {
		this.title = validateTitle(title);
	}

	public void updateMemberLimit(Integer memberLimit) {
		this.memberLimit = validateMemberLimit(memberLimit);
	}

	public void updateLabel(String label) {
		this.label = validateLabel(label);
	}

	public void updatePurpose(String purpose) {
		this.purpose = validatePurpose(purpose);
	}
}
