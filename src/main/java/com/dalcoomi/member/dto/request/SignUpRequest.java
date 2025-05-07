package com.dalcoomi.member.dto.request;

import java.time.LocalDate;

import com.dalcoomi.member.domain.SocialType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

public record SignUpRequest(
	@NotBlank(message = "소셜 ID는 필수입니다.")
	String socialId,

	@NotNull(message = "소셜 로그인 타입은 필수입니다.")
	SocialType socialType,

	@Email(message = "올바른 이메일 형식이 아닙니다.")
	String email,

	@NotBlank(message = "회원 이름은 필수입니다.")
	String name,

	@Past(message = "생년월일은 과거 날짜여야 합니다.")
	LocalDate birthday,

	String gender,

	@NotNull(message = "서비스 이용 약관 동의 여부는 필수입니다.")
	Boolean serviceAgreement,

	@NotNull(message = "개인 정보 수집 동의 여부는 필수입니다.")
	Boolean collectionAgreement
) {

}
