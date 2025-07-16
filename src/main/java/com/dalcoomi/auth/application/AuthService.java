package com.dalcoomi.auth.application;

import static com.dalcoomi.common.constant.TokenConstants.MEMBER_ROLE;
import static com.dalcoomi.common.constant.TokenConstants.TEST_ROLE;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dalcoomi.auth.dto.TokenInfo;
import com.dalcoomi.member.application.repository.MemberRepository;
import com.dalcoomi.member.application.repository.SocialConnectionRepository;
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
	public TokenInfo login(SocialInfo socialInfo) {
		Long memberId = socialConnectionRepository.findMemberIdBySocialIdAndSocialType(socialInfo.socialId(),
			socialInfo.socialType());

		TokenInfo tokenInfo = jwtService.createAndSaveToken(memberId, MEMBER_ROLE);

		log.info("로그인 성공 - memberId: {}", memberId);

		return tokenInfo;
	}

	public TokenInfo reissueToken(Long memberId) {
		jwtService.deleteRefreshToken(memberId);

		return jwtService.createAndSaveToken(memberId, MEMBER_ROLE);
	}

	public TokenInfo createTestToken(Long memberId) {
		memberRepository.findById(memberId);

		return jwtService.createAndSaveToken(memberId, TEST_ROLE);
	}
}
