package com.dalcoomi.team.dto.response;

import java.util.List;

import com.dalcoomi.member.domain.Member;
import com.dalcoomi.team.dto.TeamInfo;

import lombok.Builder;

@Builder
public record GetTeamResponse(
	Long teamId,
	String title,
	String invitationCode,
	Integer memberLimit,
	String purpose,
	String leaderNickname,
	List<MemberInfo> members
) {

	public static GetTeamResponse from(TeamInfo teamInfo) {
		List<MemberInfo> membersInfo = teamInfo.members().stream().map(MemberInfo::from).toList();

		return GetTeamResponse.builder()
			.teamId(teamInfo.team().getId())
			.title(teamInfo.team().getTitle())
			.invitationCode(teamInfo.team().getInvitationCode())
			.memberLimit(teamInfo.team().getMemberLimit())
			.purpose(teamInfo.team().getPurpose())
			.leaderNickname(teamInfo.team().getLeader().getNickname())
			.members(membersInfo)
			.build();
	}

	@Builder
	public record MemberInfo(
		String nickname,
		String profileImageUrl
	) {

		public static MemberInfo from(Member member) {
			return MemberInfo.builder()
				.nickname(member.getNickname())
				.profileImageUrl(member.getProfileImageUrl())
				.build();
		}
	}
}
