package com.dalcoomi.auth.application;

import static com.dalcoomi.auth.constant.TokenConstants.MEMBER_ROLE;
import static com.dalcoomi.auth.constant.TokenConstants.TEST_ROLE;
import static com.dalcoomi.common.error.model.ErrorMessage.MEMBER_DORMANT_ACCOUNT;
import static com.dalcoomi.common.error.model.ErrorMessage.MEMBER_NOT_FOUND;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dalcoomi.auth.dto.LoginInfo;
import com.dalcoomi.auth.dto.TokenInfo;
import com.dalcoomi.common.error.exception.LockedException;
import com.dalcoomi.common.error.exception.NotFoundException;
import com.dalcoomi.member.application.repository.MemberRepository;
import com.dalcoomi.member.application.repository.SocialConnectionRepository;
import com.dalcoomi.member.domain.Member;
import com.dalcoomi.member.domain.SocialConnection;
import com.dalcoomi.member.dto.SocialInfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

	private final JwtService jwtService;
	private final MemberRepository memberRepository;
	private final SocialConnectionRepository socialConnectionRepository;

	@Transactional
	public LoginInfo login(SocialInfo socialInfo) {
		List<SocialConnection> socialConnections = socialConnectionRepository.findBySocialEmailOrSocialId(
			socialInfo.socialEmail(), socialInfo.socialId());

		// 신규 회원 = 영구 탈퇴 회원
		if (socialConnections.isEmpty()) {
			throw new NotFoundException(MEMBER_NOT_FOUND);
		}

		SocialConnection existingSocialConnection = socialConnections.getFirst();
		Member member = existingSocialConnection.getMember();

		// 휴면 회원
		if (member.getDeletedAt() != null) {
			throw new LockedException(MEMBER_DORMANT_ACCOUNT);
		}

		for (SocialConnection socialConnection : socialConnections) {
			if (socialConnection.getSocialId().equals(socialInfo.socialId())
				&& socialConnection.getSocialType() == socialInfo.socialType()) {
				TokenInfo tokenInfo = jwtService.createAndSaveToken(member.getId(), MEMBER_ROLE);

				if (!socialConnection.getSocialEmail().equals(socialInfo.socialEmail())) {
					socialConnection.updateSocialEmail(socialInfo.socialEmail());

					socialConnectionRepository.save(socialConnection);
				}

				member.updateLoginTime(LocalDateTime.now());

				memberRepository.save(member);

				log.info("로그인 성공 - memberId: {}", member.getId());

				return LoginInfo.builder()
					.sameSocial(true)
					.existingSocialType(socialConnection.getSocialType())
					.accessToken(tokenInfo.accessToken())
					.refreshToken(tokenInfo.refreshToken())
					.build();
			}
		}

		// 다른 소셜 계정으로 로그인 시도한 경우, 기존 소셜 타입 반환
		return LoginInfo.builder()
			.sameSocial(false)
			.existingSocialType(existingSocialConnection.getSocialType())
			.build();
	}

	public void logout(Long memberId) {
		jwtService.deleteRefreshToken(memberId);
	}

	public TokenInfo reissueToken(Long memberId) {
		jwtService.deleteRefreshToken(memberId);

		return jwtService.createAndSaveToken(memberId, MEMBER_ROLE);
	}

	@Transactional(readOnly = true)
	public TokenInfo createTestToken(Long memberId) {
		memberRepository.findById(memberId);

		return jwtService.createAndSaveToken(memberId, TEST_ROLE);
	}
}
