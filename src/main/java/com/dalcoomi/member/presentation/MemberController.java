package com.dalcoomi.member.presentation;

import static org.springframework.http.HttpStatus.CREATED;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.dalcoomi.auth.dto.TokenInfo;
import com.dalcoomi.member.application.MemberService;
import com.dalcoomi.member.dto.MemberInfo;
import com.dalcoomi.member.dto.SocialInfo;
import com.dalcoomi.member.dto.request.SignUpRequest;
import com.dalcoomi.member.dto.response.SignUpResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {

	private final MemberService memberService;

	@PostMapping("/sign-up")
	@ResponseStatus(CREATED)
	public SignUpResponse signUp(@RequestBody SignUpRequest request) {
		MemberInfo memberInfo = MemberInfo.builder()
			.email(request.email())
			.name(request.name())
			.birthday(request.birthday())
			.gender(request.gender())
			.serviceAgreement(request.serviceAgreement())
			.collectionAgreement(request.collectionAgreement())
			.build();

		SocialInfo socialInfo = SocialInfo.builder()
			.socialId(request.socialId())
			.socialType(request.socialType())
			.memberInfo(memberInfo)
			.build();

		TokenInfo tokenInfo = memberService.signUp(socialInfo);

		return new SignUpResponse(tokenInfo.accessToken(), tokenInfo.refreshToken());
	}
}
