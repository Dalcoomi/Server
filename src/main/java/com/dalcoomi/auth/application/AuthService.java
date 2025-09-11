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
		List<SocialConnection> socialConnections = socialConnectionRepository.findBySocialEmail(
			socialInfo.socialEmail());

		// 신규 회원 = 영구 탈퇴 회원
		if (socialConnections.isEmpty()) {
			throw new NotFoundException(MEMBER_NOT_FOUND);
		}

		// 기존 회원
		Member member = socialConnections.getFirst().getMember();

		// 휴면 회원
		if (member.getDeletedAt() != null) {
			throw new LockedException(MEMBER_DORMANT_ACCOUNT);
		}

		for (SocialConnection socialConnection : socialConnections) {
			if (socialConnection.getSocialType() == socialInfo.socialType()) {
				TokenInfo tokenInfo = jwtService.createAndSaveToken(member.getId(), MEMBER_ROLE);

				member.updateLoginTime(LocalDateTime.now());

				memberRepository.save(member);

				log.info("로그인 성공 - memberId: {}", member.getId());

				return LoginInfo.builder()
					.sameSocial(true)
					.accessToken(tokenInfo.accessToken())
					.refreshToken(tokenInfo.refreshToken())
					.build();
			}
		}

		return LoginInfo.builder().sameSocial(false).build();
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
