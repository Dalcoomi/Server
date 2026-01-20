package com.dalcoomi.team.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TeamRequest(
	Long teamId,

	@NotNull(message = "그룹명은 필수입니다.")
	@Size(max = 20, message = "그룹명은 최대 20자입니다.")
	String title,

	@NotNull(message = "인원 수는 필수입니다.")
	@Max(value = 10, message = "인원 수는 최대 10명입니다.")
	Integer memberLimit,

	@NotNull(message = "라벨 컬러는 필수입니다.")
	String label,

	@Size(max = 30, message = "그룹 목표는 최대 30자입니다.")
	String purpose
) {

}
