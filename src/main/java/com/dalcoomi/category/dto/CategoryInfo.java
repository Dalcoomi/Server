package com.dalcoomi.category.dto;

import com.dalcoomi.category.domain.Category;
import com.dalcoomi.category.domain.OwnerType;

import lombok.Builder;

@Builder
public record CategoryInfo(
	Long groupId,
	String name,
	String iconUrl,
	Boolean isActive,
	OwnerType ownerType
) {

	public static CategoryInfo from(Category category) {
		return CategoryInfo.builder()
			.name(category.getName())
			.iconUrl(category.getIconUrl())
			.isActive(category.getIsActive())
			.ownerType(category.getOwnerType())
			.build();
	}
}
