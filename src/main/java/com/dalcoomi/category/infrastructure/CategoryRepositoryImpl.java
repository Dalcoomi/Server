package com.dalcoomi.category.infrastructure;

import org.springframework.stereotype.Repository;

import com.dalcoomi.category.application.repository.CategoryRepository;
import com.dalcoomi.category.domain.Category;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CategoryRepositoryImpl implements CategoryRepository {

	private final CategoryJpaRepository categoryJpaRepository;

	@Override
	public Category save(Category category) {
		return categoryJpaRepository.save(CategoryJpaEntity.from(category)).toModel();
	}
}
