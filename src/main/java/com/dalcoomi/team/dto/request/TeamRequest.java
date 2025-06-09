package com.dalcoomi.team.dto.request;

import jakarta.validation.constraints.NotNull;

public record TeamRequest(
	@NotNull(message = "그룹명은 필수입니다.")
	String title,

	@NotNull(message = "인원 수는 필수입니다.")
	Integer memberLimit,

	String purpose
) {

}
