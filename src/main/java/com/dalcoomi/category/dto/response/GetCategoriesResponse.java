package com.dalcoomi.category.dto.response;

import java.util.List;

import com.dalcoomi.category.domain.OwnerType;
import com.dalcoomi.category.dto.CategoryInfo;

import lombok.Builder;

@Builder
public record GetCategoriesResponse(
	List<GetMyCategoryResponseItem> categories
) {

	public static GetCategoriesResponse from(List<CategoryInfo> categoryInfos) {
		List<GetMyCategoryResponseItem> categories = categoryInfos.stream()
			.map(GetMyCategoryResponseItem::from)
			.toList();

		return GetCategoriesResponse.builder()
			.categories(categories)
			.build();
	}

	@Builder
	public record GetMyCategoryResponseItem(
		Long id,
		String name,
		String iconUrl,
		OwnerType ownerType
	) {

		public static GetMyCategoryResponseItem from(CategoryInfo categoryInfo) {
			return GetMyCategoryResponseItem.builder()
				.id(categoryInfo.id())
				.name(categoryInfo.name())
				.iconUrl(categoryInfo.iconUrl())
				.ownerType(categoryInfo.ownerType())
				.build();
		}
	}
}
