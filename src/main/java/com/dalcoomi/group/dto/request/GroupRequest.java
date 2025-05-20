package com.dalcoomi.group.dto.request;

import jakarta.validation.constraints.NotNull;

public record GroupRequest(
	@NotNull(message = "그룹명은 필수입니다.")
	String title,

	@NotNull(message = "인원 수는 필수입니다.")
	Integer count,

	String goal
) {

}
