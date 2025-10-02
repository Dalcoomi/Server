package com.dalcoomi.auth.presentation;

import static org.springframework.http.HttpStatus.OK;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.dalcoomi.auth.annotation.AuthMember;
import com.dalcoomi.auth.application.AuthService;
import com.dalcoomi.auth.application.JwtService;
import com.dalcoomi.auth.dto.LoginInfo;
import com.dalcoomi.auth.dto.TokenInfo;
import com.dalcoomi.auth.dto.request.LoginRequest;
import com.dalcoomi.auth.dto.response.LoginResponse;
import com.dalcoomi.auth.dto.response.ReissueTokenResponse;
import com.dalcoomi.auth.dto.response.TestTokenResponse;
import com.dalcoomi.member.application.MemberService;
import com.dalcoomi.member.dto.SocialInfo;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	private final JwtService jwtService;
	private final MemberService memberService;

	@PostMapping("/login")
	@ResponseStatus(OK)
	public LoginResponse login(@RequestBody @Valid LoginRequest request) {
		SocialInfo socialInfo = SocialInfo.builder()
			.socialEmail(request.socialEmail())
			.socialId(request.socialId())
			.socialRefreshToken(request.socialRefreshToken())
			.socialType(request.socialType())
			.build();

		LoginInfo loginInfo = authService.login(socialInfo);

		return LoginResponse.from(loginInfo);
	}

	@PostMapping("/logout")
	@ResponseStatus(OK)
	public void logout(@AuthMember Long memberId) {
		authService.logout(memberId);
	}

	@PostMapping("/reissue")
	@ResponseStatus(OK)
	public ReissueTokenResponse reissueToken(@RequestHeader("Refresh-Token") String refreshToken) {
		Long memberId = jwtService.validateRefreshToken(refreshToken);
		TokenInfo tokenInfo = authService.reissueToken(memberId);

		memberService.updateLastLoginTime(memberId);

		return ReissueTokenResponse.from(tokenInfo);
	}

	@GetMapping("/test/token")
	@ResponseStatus(OK)
	public TestTokenResponse createTestToken(@RequestParam("memberId") Long memberId) {
		TokenInfo tokenInfo = authService.createTestToken(memberId);

		return TestTokenResponse.from(tokenInfo);
	}
}
