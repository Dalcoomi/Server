package com.dalcoomi.category.dto.response;

import java.util.List;

import com.dalcoomi.category.domain.OwnerType;
import com.dalcoomi.category.dto.CategoryInfo;

import lombok.Builder;

@Builder
public record GetMyCategoryResponse(
	List<GetMyCategoryResponseItem> categories
) {

	public static GetMyCategoryResponse from(List<CategoryInfo> categoryInfos) {
		List<GetMyCategoryResponseItem> categories = categoryInfos.stream()
			.map(GetMyCategoryResponseItem::from)
			.toList();

		return GetMyCategoryResponse.builder()
			.categories(categories)
			.build();
	}

	@Builder
	public record GetMyCategoryResponseItem(
		String name,
		String iconUrl,
		OwnerType ownerType
	) {

		public static GetMyCategoryResponseItem from(CategoryInfo categoryInfo) {
			return GetMyCategoryResponseItem.builder()
				.name(categoryInfo.name())
				.iconUrl(categoryInfo.iconUrl())
				.ownerType(categoryInfo.ownerType())
				.build();
		}
	}
}
