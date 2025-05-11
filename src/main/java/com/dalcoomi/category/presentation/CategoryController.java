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
import com.dalcoomi.category.dto.response.GetMyCategoryResponse;
import com.dalcoomi.transaction.domain.TransactionType;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryController {

	private final CategoryService categoryService;

	@GetMapping("/my")
	@ResponseStatus(OK)
	public GetMyCategoryResponse getMyCategoryWithTransactionType(@AuthMember Long memberId,
		@RequestParam("transactionType") TransactionType transactionType) {
		List<CategoryInfo> categoryInfos = categoryService.getMyCategoryWithTransactionType(memberId, transactionType);

		return GetMyCategoryResponse.from(categoryInfos);
	}
}
