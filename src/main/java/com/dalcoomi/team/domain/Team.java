package com.dalcoomi.team.domain;

import static com.dalcoomi.common.error.model.ErrorMessage.TEAM_INVALID_MEMBER_LIMIT;
import static com.dalcoomi.common.error.model.ErrorMessage.TEAM_INVALID_PURPOSE;
import static io.micrometer.common.util.StringUtils.isBlank;
import static java.lang.String.format;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import com.dalcoomi.member.domain.Member;
import com.dalcoomi.team.dto.request.TeamRequest;

import lombok.Builder;
import lombok.Getter;

@Getter
public class Team {

	public static final int INVITATION_CODE_LENGTH = 8;
	public static final int MEMBER_LIMIT_SIZE = 10;
	public static final int PURPOSE_LENGTH = 100;

	private final Long id;
	private final String title;
	private final Integer memberLimit;
	private final String purpose;
	private final LocalDateTime deletedAt;
	private Member member;
	private String invitationCode;

	@Builder
	public Team(Long id, Member member, String title, String invitationCode, Integer memberLimit, String purpose,
		LocalDateTime deletedAt) {
		this.id = id;
		this.member = member;
		this.title = title;
		this.invitationCode = invitationCode;
		this.memberLimit = validateMemberLimit(memberLimit);
		this.purpose = validatePurpose(purpose);
		this.deletedAt = deletedAt;
	}

	public static Team from(TeamRequest request) {
		return Team.builder()
			.title(request.title())
			.memberLimit(request.memberLimit())
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

	public void updateMember(Member member) {
		this.member = member;
	}

	public void updateInvitationCode(String invitationCode) {
		this.invitationCode = invitationCode;
	}

	private Integer validateMemberLimit(Integer memberLimit) {
		if (memberLimit == null || memberLimit < 1 || memberLimit > MEMBER_LIMIT_SIZE) {
			throw new IllegalArgumentException(format(TEAM_INVALID_MEMBER_LIMIT.getMessage(), MEMBER_LIMIT_SIZE));
		}

		return memberLimit;
	}

	private String validatePurpose(String purpose) {
		if (!isBlank(purpose) && purpose.length() > PURPOSE_LENGTH) {
			throw new IllegalArgumentException(TEAM_INVALID_PURPOSE.getMessage());
		}

		return purpose;
	}
}
