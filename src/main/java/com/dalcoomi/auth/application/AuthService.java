package com.dalcoomi.auth.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dalcoomi.auth.dto.TokenInfo;
import com.dalcoomi.member.application.repository.SocialConnectionRepository;
import com.dalcoomi.member.dto.SocialInfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

	private final JwtService jwtService;
	private final SocialConnectionRepository socialConnectionRepository;

	@Transactional
	public TokenInfo login(SocialInfo socialInfo) {
		Long memberId = socialConnectionRepository.findMemberIdBySocialIdAndSocialType(socialInfo.socialId(),
			socialInfo.socialType());

		TokenInfo tokenInfo = jwtService.createAndSaveToken(memberId);

		log.info("로그인 성공 - memberId: {}", memberId);

		return tokenInfo;
	}

	public TokenInfo reissueToken(Long memberId) {
		jwtService.deleteRefreshToken(memberId);

		return jwtService.createAndSaveToken(memberId);
	}
}
