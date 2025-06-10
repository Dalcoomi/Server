package com.dalcoomi.auth.presentation;

import static org.springframework.http.HttpStatus.OK;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.dalcoomi.auth.application.AuthService;
import com.dalcoomi.auth.config.AuthMember;
import com.dalcoomi.auth.dto.TokenInfo;
import com.dalcoomi.auth.dto.request.LoginRequest;
import com.dalcoomi.auth.dto.response.LoginResponse;
import com.dalcoomi.auth.dto.response.ReissueTokenResponse;
import com.dalcoomi.member.dto.SocialInfo;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/login")
	@ResponseStatus(OK)
	public LoginResponse login(@RequestBody @Valid LoginRequest request) {
		SocialInfo socialInfo = SocialInfo.builder()
			.socialId(request.socialId())
			.socialType(request.socialType())
			.build();

		TokenInfo tokenInfo = authService.login(socialInfo);

		return new LoginResponse(tokenInfo.accessToken(), tokenInfo.refreshToken());
	}

	@PostMapping("/reissue")
	@ResponseStatus(OK)
	public ReissueTokenResponse reissueToken(@AuthMember Long memberId) {
		TokenInfo tokenInfo = authService.reissueToken(memberId);

		return new ReissueTokenResponse(tokenInfo.accessToken(), tokenInfo.refreshToken());
	}
}
