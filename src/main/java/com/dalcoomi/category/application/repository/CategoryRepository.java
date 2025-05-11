package com.dalcoomi.category.application.repository;

import java.util.List;

import com.dalcoomi.category.domain.Category;
import com.dalcoomi.transaction.domain.TransactionType;

public interface CategoryRepository {

	Category save(Category category);

	List<Category> saveAll(List<Category> categories);

	Category findById(Long categoryId);

	List<Category> findByMemberIdAndTransactionType(Long memberId, TransactionType transactionType);
}
