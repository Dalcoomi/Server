package com.dalcoomi.team.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record UpdateTeamOrderRequest(
	@Valid
	@NotEmpty(message = "순서 정보는 필수입니다.")
	List<TeamOrderItem> orders
) {

	public record TeamOrderItem(
		@NotNull(message = "그룹 ID는 필수입니다.")
		Long teamId,

		@NotNull(message = "순서는 필수입니다.")
		Integer displayOrder
	) {

	}
}
