package com.dalcoomi.member.application;

import static com.dalcoomi.common.error.model.ErrorMessage.LAST_SOCIAL_CONNECTION;
import static com.dalcoomi.common.error.model.ErrorMessage.MEMBER_CONFLICT;
import static com.dalcoomi.common.error.model.ErrorMessage.MEMBER_NICKNAME_CONFLICT;
import static com.dalcoomi.image.constant.ImageConstants.DEFAULT_PROFILE_IMAGE_1;
import static com.dalcoomi.image.constant.ImageConstants.DEFAULT_PROFILE_IMAGE_2;
import static com.dalcoomi.image.constant.ImageConstants.DEFAULT_PROFILE_IMAGE_3;
import static com.dalcoomi.image.constant.ImageConstants.DEFAULT_PROFILE_IMAGE_4;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dalcoomi.common.error.exception.BadRequestException;
import com.dalcoomi.common.error.exception.ConflictException;
import com.dalcoomi.common.util.provider.NicknameProvider;
import com.dalcoomi.member.application.repository.MemberRepository;
import com.dalcoomi.member.application.repository.SocialConnectionRepository;
import com.dalcoomi.member.application.repository.WithdrawalRepository;
import com.dalcoomi.member.domain.Member;
import com.dalcoomi.member.domain.SocialConnection;
import com.dalcoomi.member.domain.SocialType;
import com.dalcoomi.member.domain.Withdrawal;
import com.dalcoomi.member.domain.validator.NicknameValidator;
import com.dalcoomi.member.dto.AvatarInfo;
import com.dalcoomi.member.dto.MemberInfo;
import com.dalcoomi.member.dto.SignUpInfo;
import com.dalcoomi.member.dto.SocialInfo;
import com.dalcoomi.member.dto.WithdrawalInfo;
import com.dalcoomi.team.application.repository.TeamMemberRepository;
import com.dalcoomi.team.application.repository.TeamRepository;
import com.dalcoomi.team.domain.Team;
import com.dalcoomi.team.domain.TeamMember;
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

	private final NicknameValidator nicknameValidator;

	@Transactional
	public Long signUp(SignUpInfo signUpInfo) {
		boolean existsSocialConnection = socialConnectionRepository.existsMemberBySocialIdAndSocialType(
			signUpInfo.socialId(), signUpInfo.socialType());

		if (existsSocialConnection) {
			throw new ConflictException(MEMBER_CONFLICT);
		}

		String name = signUpInfo.name();
		String nickname = new NicknameProvider().generateUniqueNickname(name, 4);
		String randomProfileUrl = getRandomDefaultProfileImage();

		Member member = Member.builder()
			.email(signUpInfo.socialEmail()) // 나중에 이메일로 수정하기
			.name(name)
			.nickname("dummy")
			.birthday(signUpInfo.birthday())
			.gender(signUpInfo.gender())
			.profileImageUrl(randomProfileUrl)
			.serviceAgreement(signUpInfo.serviceAgreement())
			.collectionAgreement(signUpInfo.collectionAgreement())
			.aiLearningAgreement(signUpInfo.aiLearningAgreement())
			.lastLoginAt(LocalDateTime.now())
			.build();

		member.skipValidationNickname(nickname);

		member = memberRepository.save(member);

		SocialConnection socialConnection = SocialConnection.builder()
			.member(member)
			.socialEmail(signUpInfo.socialEmail())
			.socialId(signUpInfo.socialId())
			.socialRefreshToken(signUpInfo.socialRefreshToken())
			.socialType(signUpInfo.socialType())
			.build();

		socialConnectionRepository.save(socialConnection);

		return member.getId();
	}

	@Transactional
	public void connect(SocialInfo socialInfo) {
		boolean existsSocialConnection = socialConnectionRepository.existsMemberBySocialIdAndSocialType(
			socialInfo.socialId(), socialInfo.socialType());

		if (existsSocialConnection) {
			throw new ConflictException(MEMBER_CONFLICT);
		}

		Member member = memberRepository.findByEmail(socialInfo.socialEmail());

		SocialConnection socialConnection = SocialConnection.builder()
			.member(member)
			.socialEmail(socialInfo.socialEmail())
			.socialId(socialInfo.socialId())
			.socialRefreshToken(socialInfo.socialRefreshToken())
			.socialType(socialInfo.socialType())
			.build();

		socialConnectionRepository.save(socialConnection);
	}

	@Transactional
	public void socialLink(Long memberId, SocialInfo socialInfo) {
		boolean existsSocialConnection = socialConnectionRepository.existsMemberBySocialIdAndSocialType(
			socialInfo.socialId(), socialInfo.socialType());

		if (existsSocialConnection) {
			throw new ConflictException(MEMBER_CONFLICT);
		}

		Member member = memberRepository.findById(memberId);

		SocialConnection socialConnection = SocialConnection.builder()
			.member(member)
			.socialEmail(socialInfo.socialEmail())
			.socialId(socialInfo.socialId())
			.socialRefreshToken(socialInfo.socialRefreshToken())
			.socialType(socialInfo.socialType())
			.build();

		socialConnectionRepository.save(socialConnection);
	}

	@Transactional(readOnly = true)
	public MemberInfo get(Long memberId) {
		Member member = memberRepository.findById(memberId);
		List<SocialConnection> socialConnection = socialConnectionRepository.findByMemberId(memberId);

		return MemberInfo.builder()
			.socialTypes(socialConnection.stream().map(SocialConnection::getSocialType).toList())
			.email(member.getEmail())
			.name(member.getName())
			.nickname(member.getNickname())
			.birthday(member.getBirthday())
			.gender(member.getGender())
			.profileImageUrl(member.getProfileImageUrl())
			.aiLearningAgreement(member.getAiLearningAgreement())
			.build();
	}

	@Transactional(readOnly = true)
	public String getSocialRefreshToken(Long memberId, SocialType socialType) {
		SocialConnection socialConnection = socialConnectionRepository.findByMemberIdAndSocialType(memberId,
			socialType);

		return socialConnection.getSocialRefreshToken();
	}

	@Transactional(readOnly = true)
	public boolean checkNicknameAvailability(Long memberId, String nickname) {
		Member member = memberRepository.findById(memberId);

		if (!member.getNickname().equals(nickname)) {
			nicknameValidator.validate(nickname);
		}

		boolean existence = memberRepository.existsByNickname(nickname);

		return member.getNickname().equals(nickname) || !existence;
	}

	@Transactional(readOnly = true)
	public AvatarInfo getAvatarInfo(Long memberId) {
		Member member = memberRepository.findById(memberId);
		boolean defaultImage = validateDefaultProfileImage(member.getProfileImageUrl());

		return AvatarInfo.builder()
			.member(member)
			.defaultImage(defaultImage)
			.build();
	}

	@Transactional
	public String updateAvatar(Member member, @Nullable String newAvatarUrl) {
		if (newAvatarUrl == null) {
			newAvatarUrl = getRandomDefaultProfileImage();
		}

		member.updateProfileImageUrl(newAvatarUrl);

		memberRepository.save(member);

		return newAvatarUrl;
	}

	@Transactional
	public MemberInfo updateProfile(Long memberId, MemberInfo memberInfo) {
		Member member = memberRepository.findById(memberId);

		if (!member.getNickname().equals(memberInfo.nickname())) {
			nicknameValidator.validate(memberInfo.nickname());

			if (memberRepository.existsByNickname(memberInfo.nickname())) {
				throw new ConflictException(MEMBER_NICKNAME_CONFLICT);
			}
		}

		member.updateProfile(memberInfo.name(), memberInfo.nickname(), memberInfo.birthday(), memberInfo.gender());

		member = memberRepository.save(member);

		return MemberInfo.builder()
			.name(member.getName())
			.nickname(member.getNickname())
			.birthday(member.getBirthday())
			.gender(member.getGender())
			.build();
	}

	@Transactional
	public void updateAiLearningAgreement(Long memberId, Boolean agreement) {
		Member member = memberRepository.findById(memberId);

		member.updateAiLearningAgreement(agreement);

		memberRepository.save(member);
	}

	@Async
	@Transactional
	public void updateLastLoginTime(Long memberId) {
		try {
			Member member = memberRepository.findById(memberId);

			member.updateLoginTime(LocalDateTime.now());

			memberRepository.save(member);
		} catch (Exception e) {
			// do nothing
		}
	}

	@Transactional
	public void disconnect(Long memberId, SocialType socialType) {
		Member member = memberRepository.findById(memberId);
		List<SocialConnection> socialConnections = socialConnectionRepository.findByMemberId(memberId);

		if (socialConnections.size() == 1) {
			throw new BadRequestException(LAST_SOCIAL_CONNECTION);
		}

		for (SocialConnection socialConnection : socialConnections) {
			if (socialConnection.getSocialType().equals(socialType)) {
				socialConnectionRepository.deleteById(socialConnection.getId());
			} else {
				member.updateEmail(socialConnection.getSocialEmail());

				memberRepository.save(member);
			}
		}
	}

	@Transactional
	public String withdraw(Long memberId, WithdrawalInfo withdrawalInfo) {
		Member member = memberRepository.findById(memberId);
		String profileImageUrl = member.getProfileImageUrl();

		TransactionSearchCriteria criteria = TransactionSearchCriteria.builder()
			.memberId(memberId)
			.build();
		List<Transaction> allTransactions = transactionRepository.findTransactions(criteria).stream().toList();
		List<Transaction> teamTransactions = allTransactions.stream()
			.filter(transaction -> transaction.getTeamId() != null)
			.toList();
		List<Transaction> personalTransactions = allTransactions.stream()
			.filter(transaction -> transaction.getTeamId() == null)
			.toList();

		// 참여 중인 그룹 처리
		processTeamWithdrawal(memberId, withdrawalInfo);

		// 그룹 거래 내역 익명화
		anonymizeTeamTransactions(teamTransactions);

		// 탈퇴 방식에 따른 개인 데이터 처리
		if (withdrawalInfo.softDelete()) {
			processSoftWithdrawal(member, personalTransactions, withdrawalInfo);
		} else {
			processHardWithdrawal(memberId, personalTransactions);
		}

		// 탈퇴 사유 저장
		Withdrawal withdrawal = Withdrawal.from(withdrawalInfo.withdrawalType(), withdrawalInfo.otherReason());

		withdrawalRepository.save(withdrawal);

		return profileImageUrl;
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

	private boolean validateDefaultProfileImage(String avatarUrl) {
		String[] defaultImages = {
			DEFAULT_PROFILE_IMAGE_1,
			DEFAULT_PROFILE_IMAGE_2,
			DEFAULT_PROFILE_IMAGE_3,
			DEFAULT_PROFILE_IMAGE_4
		};

		for (String defaultImage : defaultImages) {
			if (avatarUrl.equals(defaultImage)) {
				return true;
			}
		}

		return false;
	}

	private void processTeamWithdrawal(Long memberId, WithdrawalInfo withdrawalInfo) {
		List<TeamMember> teamMembers = teamMemberRepository.find(null, memberId);

		for (TeamMember teamMember : teamMembers) {
			Team team = teamMember.getTeam();
			Long teamId = team.getId();

			if (!team.getLeader().getId().equals(memberId)) {
				return;
			}

			String nextLeaderNickname = withdrawalInfo.teamToNextLeaderMap().get(team.getId());

			if (nextLeaderNickname != null) {
				Member nextLeader = memberRepository.findByNickname(nextLeaderNickname);

				team.updateLeader(nextLeader);

				teamRepository.save(team);
			}

			teamMemberRepository.deleteByTeamIdAndMemberId(teamId, memberId);

			if (teamMemberRepository.countByTeamId(teamId) == 0) {
				teamRepository.deleteById(teamId);
				transactionRepository.deleteByTeamId(teamId);
			}
		}
	}

	private void anonymizeTeamTransactions(List<Transaction> teamTransactions) {
		for (Transaction transaction : teamTransactions) {
			transaction.anonymize();
		}

		transactionRepository.saveAll(teamTransactions);
	}

	private void processSoftWithdrawal(Member member, List<Transaction> personalTransactions,
		WithdrawalInfo withdrawalInfo) {
		List<SocialConnection> socialConnections = socialConnectionRepository.findByMemberId(member.getId());

		// 개인 거래 내역 소프트 삭제
		for (Transaction transaction : personalTransactions) {
			transaction.softDelete();
			transaction.updateDataRetentionConsent(withdrawalInfo.dataRetentionConsent());
		}

		transactionRepository.saveAll(personalTransactions);

		// 소셜 연결 소프트 삭제
		for (SocialConnection socialConnection : socialConnections) {
			socialConnection.softDelete();
		}

		socialConnectionRepository.saveAll(socialConnections);

		// 회원 정보 소프트 삭제
		member.updateProfileImageUrl(getRandomDefaultProfileImage());
		member.softDelete();

		memberRepository.save(member);
	}

	private void processHardWithdrawal(Long memberId, List<Transaction> personalTransactions) {
		transactionRepository.deleteAll(personalTransactions);
		socialConnectionRepository.deleteByMemberId(memberId);
		memberRepository.deleteById(memberId);
	}
}
