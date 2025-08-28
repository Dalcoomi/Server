package com.dalcoomi.member.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
	@NotBlank(message = "이름은 필수입니다.")
	@Size(min = 2, max = 30, message = "이름은 2~30자 입니다.")
	String name,

	@NotBlank(message = "닉네임은 필수입니다.")
	@Size(max = 15, message = "닉네임은 15자 이하여야 합니다.")
	@Pattern(regexp = "^[가-힣a-zA-Z0-9]+$", message = "닉네임은 한글, 영문, 숫자, 언더스코어만 사용 가능합니다.")
	String nickname,

	@Past(message = "생년월일은 과거 날짜여야 합니다.")
	LocalDate birthday,

	@Size(max = 10, message = "성별은 최대 2자입니다.")
	String gender
) {

}
