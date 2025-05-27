package com.dalcoomi.category.application;

import java.util.List;

import org.springframework.stereotype.Service;

import com.dalcoomi.category.application.repository.CategoryRepository;
import com.dalcoomi.category.domain.Category;
import com.dalcoomi.category.dto.CategoryInfo;
import com.dalcoomi.transaction.domain.TransactionType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {

	private final CategoryRepository categoryRepository;

	public List<CategoryInfo> getMyCategoryWithTransactionType(Long memberId, TransactionType transactionType) {
		List<Category> categories = categoryRepository.findByCreatorIdAndTransactionType(memberId, transactionType);

		return categories.stream().map(CategoryInfo::from).toList();
	}
}
