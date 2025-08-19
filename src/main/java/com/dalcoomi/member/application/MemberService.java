package com.dalcoomi.member.application;

import static com.dalcoomi.common.constant.ImageConstants.DEFAULT_PROFILE_IMAGE_1;
import static com.dalcoomi.common.constant.ImageConstants.DEFAULT_PROFILE_IMAGE_2;
import static com.dalcoomi.common.constant.ImageConstants.DEFAULT_PROFILE_IMAGE_3;
import static com.dalcoomi.common.constant.ImageConstants.DEFAULT_PROFILE_IMAGE_4;
import static com.dalcoomi.common.error.model.ErrorMessage.MEMBER_CONFLICT;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dalcoomi.common.error.exception.ConflictException;
import com.dalcoomi.common.util.provider.NicknameProvider;
import com.dalcoomi.member.application.repository.MemberRepository;
import com.dalcoomi.member.application.repository.SocialConnectionRepository;
import com.dalcoomi.member.application.repository.WithdrawalRepository;
import com.dalcoomi.member.domain.Member;
import com.dalcoomi.member.domain.SocialConnection;
import com.dalcoomi.member.domain.Withdrawal;
import com.dalcoomi.member.domain.WithdrawalType;
import com.dalcoomi.member.dto.SocialInfo;
import com.dalcoomi.team.application.repository.TeamMemberRepository;
import com.dalcoomi.team.application.repository.TeamRepository;
import com.dalcoomi.team.domain.Team;
import com.dalcoomi.team.domain.TeamMember;
import com.dalcoomi.team.dto.LeaveTeamInfo;
import com.dalcoomi.transaction.application.repository.TransactionRepository;
import com.dalcoomi.transaction.domain.Transaction;
import com.dalcoomi.transaction.dto.TransactionSearchCriteria;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberService {

	private final MemberRepository memberRepository;
	private final SocialConnectionRepository socialConnectionRepository;
	private final TeamMemberRepository teamMemberRepository;
	private final TeamRepository teamRepository;
	private final TransactionRepository transactionRepository;
	private final WithdrawalRepository withdrawalRepository;

	@Transactional
	public Long signUp(SocialInfo socialInfo) {
		boolean existsMember = socialConnectionRepository.existsMemberBySocialIdAndSocialType(
			socialInfo.socialId(), socialInfo.socialType());

		if (existsMember) {
			throw new ConflictException(MEMBER_CONFLICT);
		}

		String name = socialInfo.memberInfo().name();
		String nickname = new NicknameProvider().generateUniqueNickname(name, 4);
		String randomProfileUrl = getRandomDefaultProfileImage();

		Member member = Member.builder()
			.email(socialInfo.memberInfo().email())
			.name(name)
			.nickname(nickname)
			.birthday(socialInfo.memberInfo().birthday())
			.gender(socialInfo.memberInfo().gender())
			.profileImageUrl(randomProfileUrl)
			.serviceAgreement(socialInfo.memberInfo().serviceAgreement())
			.collectionAgreement(socialInfo.memberInfo().collectionAgreement())
			.build();

		member = memberRepository.save(member);

		SocialConnection socialConnection = SocialConnection.builder()
			.member(member)
			.socialId(socialInfo.socialId())
			.socialType(socialInfo.socialType())
			.build();

		socialConnectionRepository.save(socialConnection);

		return member.getId();
	}

	@Transactional(readOnly = true)
	public Member get(Long memberId) {
		return memberRepository.findById(memberId);
	}

	private String getRandomDefaultProfileImage() {
		String[] defaultImages = {
			DEFAULT_PROFILE_IMAGE_1,
			DEFAULT_PROFILE_IMAGE_2,
			DEFAULT_PROFILE_IMAGE_3,
			DEFAULT_PROFILE_IMAGE_4
		};

		Random random = new SecureRandom();
		int randomIndex = random.nextInt(defaultImages.length);

		return defaultImages[randomIndex];
	}

	@Transactional
	public void withdraw(Long memberId, WithdrawalType withdrawalType, String otherReason,
		List<LeaveTeamInfo> leaveTeamInfos) {
		Member member = memberRepository.findById(memberId);

		// 개인 거래 내역 소프트 삭제
		TransactionSearchCriteria criteria = TransactionSearchCriteria.builder()
			.memberId(memberId)
			.teamId(null)
			.build();

		List<Transaction> transactions = transactionRepository.findTransactions(criteria);

		for (Transaction transaction : transactions) {
			transaction.softDelete();
		}

		transactionRepository.saveAll(transactions);

		// 속한 그룹 떠나기 or 삭제
		List<TeamMember> teamMembers = teamMemberRepository.find(null, memberId);

		if (!teamMembers.isEmpty()) {
			Map<Long, String> teamToNextLeaderMap = leaveTeamInfos.stream()
				.collect(Collectors.toMap(LeaveTeamInfo::teamId, LeaveTeamInfo::nextLeaderNickname));

			for (TeamMember teamMember : teamMembers) {
				Team team = teamMember.getTeam();
				Long teamId = team.getId();
				Long leaderId = team.getLeader().getId();

				if (leaderId.equals(memberId)) {
					String nextLeaderNickname = teamToNextLeaderMap.get(teamId);

					if (nextLeaderNickname != null) {
						Member nextLeader = memberRepository.findByNickname(nextLeaderNickname);

						team.updateLeader(nextLeader);

						teamRepository.save(team);
					}
				}

				teamMemberRepository.deleteByTeamIdAndMemberId(teamId, memberId);

				if (teamMemberRepository.countByTeamId(teamId) == 0) {
					teamRepository.deleteById(teamId);
					transactionRepository.deleteByTeamId(teamId);
				}
			}
		}

		// 소셜 연결 삭제
		socialConnectionRepository.deleteByMemberId(memberId);

		// 회원 정보 소프트 삭제
		member.softDelete();

		memberRepository.save(member);

		// 탈퇴 사유 저장
		Withdrawal withdrawal = Withdrawal.builder()
			.member(member)
			.withdrawalType(withdrawalType)
			.otherReason(otherReason)
			.withdrawalDate(LocalDateTime.now())
			.build();

		withdrawalRepository.save(withdrawal);
	}
}
