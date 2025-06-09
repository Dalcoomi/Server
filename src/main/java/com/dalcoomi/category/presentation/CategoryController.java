package com.dalcoomi.category.presentation;

import static org.springframework.http.HttpStatus.OK;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.dalcoomi.auth.config.AuthMember;
import com.dalcoomi.category.application.CategoryService;
import com.dalcoomi.category.dto.CategoryInfo;
import com.dalcoomi.category.dto.response.GetCategoriesResponse;
import com.dalcoomi.transaction.domain.TransactionType;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryController {

	private final CategoryService categoryService;

	@GetMapping("/my")
	@ResponseStatus(OK)
	public GetCategoriesResponse getMyCategoryWithTransactionType(@AuthMember Long memberId,
		@RequestParam("transactionType") TransactionType transactionType) {
		List<CategoryInfo> categoryInfos = categoryService.getMyCategories(memberId, transactionType);

		return GetCategoriesResponse.from(categoryInfos);
	}

	@GetMapping("/team")
	@ResponseStatus(OK)
	public GetCategoriesResponse getTeamCategoryWithTransactionType(@AuthMember Long memberId,
		@RequestParam("teamId") Long teamId,
		@RequestParam("transactionType") TransactionType transactionType) {
		List<CategoryInfo> categoryInfos = categoryService.getTeamCategories(memberId, teamId, transactionType);

		return GetCategoriesResponse.from(categoryInfos);
	}
}
