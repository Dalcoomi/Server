package com.dalcoomi.category.application.repository;

import java.util.List;

import com.dalcoomi.category.domain.Category;
import com.dalcoomi.transaction.domain.TransactionType;

public interface CategoryRepository {

	Category save(Category category);

	List<Category> saveAll(List<Category> categories);

	Category findById(Long categoryId);

	List<Category> findAllById(List<Long> categoryIds);
	
	List<Category> findMyCategories(Long creatorId, TransactionType transactionType);

	List<Category> findTeamCategories(Long teamId, TransactionType transactionType);
}
