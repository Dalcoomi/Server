package com.dalcoomi.auth.dto.request;

import com.dalcoomi.member.domain.SocialType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(
	@NotNull(message = "소셜 이메일은 필수입니다.")
	@Email(message = "올바른 이메일 형식이 아닙니다.")
	String socialEmail,

	@NotBlank(message = "소셜 ID는 필수입니다.")
	String socialId,

	@NotBlank(message = "소셜 리프레시 토큰은 필수입니다.")
	String socialRefreshToken,

	@NotNull(message = "소셜 타입은 필수입니다.")
	SocialType socialType
) {

}
