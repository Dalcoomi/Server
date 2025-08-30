package com.dalcoomi.member.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
	@NotBlank(message = "이름은 필수입니다.")
	@Size(min = 2, max = 30, message = "이름은 2~30자입니다.")
	String name,

	String nickname,

	@Past(message = "생년월일은 과거 날짜여야 합니다.")
	LocalDate birthday,

	@Size(max = 10, message = "성별은 최대 2자입니다.")
	String gender
) {

}
