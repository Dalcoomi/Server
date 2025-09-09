package com.dalcoomi.member.presentation;

import static com.dalcoomi.auth.constant.TokenConstants.MEMBER_ROLE;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.dalcoomi.auth.annotation.AuthMember;
import com.dalcoomi.auth.application.JwtService;
import com.dalcoomi.auth.dto.TokenInfo;
import com.dalcoomi.image.application.S3Service;
import com.dalcoomi.member.application.MemberService;
import com.dalcoomi.member.dto.AvatarInfo;
import com.dalcoomi.member.dto.LeaderTransferInfo;
import com.dalcoomi.member.dto.MemberInfo;
import com.dalcoomi.member.dto.request.SignUpRequest;
import com.dalcoomi.member.dto.request.UpdateAvatarRequest;
import com.dalcoomi.member.dto.request.UpdateProfileRequest;
import com.dalcoomi.member.dto.request.WithdrawRequest;
import com.dalcoomi.member.dto.response.GetMemberResponse;
import com.dalcoomi.member.dto.response.SignUpResponse;
import com.dalcoomi.member.dto.response.UpdateProfileResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

	private final MemberService memberService;
	private final JwtService jwtService;
	private final S3Service s3Service;

	@PostMapping("/sign-up")
	@ResponseStatus(CREATED)
	public SignUpResponse signUp(@RequestBody @Valid SignUpRequest request) {
		MemberInfo memberInfo = MemberInfo.builder()
			.socialId(request.socialId())
			.socialType(request.socialType())
			.email(request.email())
			.name(request.name())
			.birthday(request.birthday())
			.gender(request.gender())
			.serviceAgreement(request.serviceAgreement())
			.collectionAgreement(request.collectionAgreement())
			.build();

		Long memberId = memberService.signUp(memberInfo);

		TokenInfo tokenInfo = jwtService.createAndSaveToken(memberId, MEMBER_ROLE);

		return new SignUpResponse(tokenInfo.accessToken(), tokenInfo.refreshToken());
	}

	@GetMapping
	@ResponseStatus(OK)
	public GetMemberResponse get(@AuthMember Long memberId) {
		MemberInfo memberInfo = memberService.get(memberId);

		return GetMemberResponse.from(memberInfo);
	}

	@GetMapping("/nickname/availability")
	@ResponseStatus(OK)
	public Boolean checkNicknameAvailability(@AuthMember Long memberId, @RequestParam String nickname) {
		return memberService.checkNicknameAvailability(memberId, nickname);
	}

	@PatchMapping("/avatar")
	@ResponseStatus(OK)
	public String updateAvatar(@AuthMember Long memberId, @ModelAttribute @Valid UpdateAvatarRequest request) {
		AvatarInfo avatarInfo = memberService.getAvatarInfo(memberId);

		String newAvatarUrl = s3Service.updateAvatar(request.removeAvatar(), avatarInfo, request.profileImage());

		return memberService.updateAvatar(avatarInfo.member(), newAvatarUrl);
	}

	@PatchMapping("/profile")
	@ResponseStatus(OK)
	public UpdateProfileResponse updateProfile(@AuthMember Long memberId,
		@RequestBody @Valid UpdateProfileRequest request) {
		MemberInfo memberInfo = MemberInfo.builder()
			.name(request.name())
			.nickname(request.nickname())
			.birthday(request.birthday())
			.gender(request.gender())
			.build();

		memberInfo = memberService.updateProfile(memberId, memberInfo);

		return UpdateProfileResponse.from(memberInfo);
	}

	@PatchMapping
	@ResponseStatus(OK)
	public void withdraw(@AuthMember Long memberId, @RequestBody @Valid WithdrawRequest request) {
		Map<Long, String> teamToNextLeaderMap = request.leaderTransferInfos().stream()
			.collect(Collectors.toMap(
				LeaderTransferInfo::teamId,
				LeaderTransferInfo::nextLeaderNickname
			));

		String profileUrl = memberService.withdraw(memberId, request.withdrawalType(), request.otherReason(),
			teamToNextLeaderMap);

		s3Service.deleteImage(profileUrl);
	}
}
