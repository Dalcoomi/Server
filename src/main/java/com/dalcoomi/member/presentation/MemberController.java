package com.dalcoomi.member.presentation;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.dalcoomi.auth.application.JwtService;
import com.dalcoomi.auth.config.AuthMember;
import com.dalcoomi.auth.dto.TokenInfo;
import com.dalcoomi.member.application.MemberService;
import com.dalcoomi.member.domain.Member;
import com.dalcoomi.member.dto.MemberInfo;
import com.dalcoomi.member.dto.SocialInfo;
import com.dalcoomi.member.dto.request.SignUpRequest;
import com.dalcoomi.member.dto.response.GetMemberResponse;
import com.dalcoomi.member.dto.response.SignUpResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {

	private final MemberService memberService;
	private final JwtService jwtService;

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

		Long memberId = memberService.signUp(socialInfo);

		TokenInfo tokenInfo = jwtService.createAndSaveToken(memberId);

		return new SignUpResponse(tokenInfo.accessToken(), tokenInfo.refreshToken());
	}

	@GetMapping
	@ResponseStatus(OK)
	public GetMemberResponse getMember(@AuthMember Long memberId) {
		Member member = memberService.getMember(memberId);

		return GetMemberResponse.from(member);
	}
}
