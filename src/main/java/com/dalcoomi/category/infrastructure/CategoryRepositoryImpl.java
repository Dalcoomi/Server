package com.dalcoomi.category.infrastructure;

import static com.dalcoomi.category.domain.OwnerType.ADMIN;
import static com.dalcoomi.category.domain.OwnerType.MEMBER;
import static com.dalcoomi.category.infrastructure.QCategoryJpaEntity.categoryJpaEntity;
import static com.dalcoomi.common.error.model.ErrorMessage.CATEGORY_NOT_FOUND;
import static com.dalcoomi.member.infrastructure.QMemberJpaEntity.memberJpaEntity;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.dalcoomi.category.application.repository.CategoryRepository;
import com.dalcoomi.category.domain.Category;
import com.dalcoomi.common.error.exception.NotFoundException;
import com.dalcoomi.transaction.domain.TransactionType;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CategoryRepositoryImpl implements CategoryRepository {

	private final CategoryJpaRepository categoryJpaRepository;
	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public Category save(Category category) {
		return categoryJpaRepository.save(CategoryJpaEntity.from(category)).toModel();
	}

	@Override
	public List<Category> saveAll(List<Category> categories) {
		List<CategoryJpaEntity> categoryJpaEntities = categoryJpaRepository.saveAll(
			categories.stream().map(CategoryJpaEntity::from).toList());

		return categoryJpaEntities.stream().map(CategoryJpaEntity::toModel).toList();
	}

	@Override
	public Category findById(Long categoryId) {
		return categoryJpaRepository.findById(categoryId)
			.orElseThrow(() -> new NotFoundException(CATEGORY_NOT_FOUND)).toModel();
	}

	@Override
	public List<Category> findMyCategories(Long creatorId, TransactionType transactionType) {
		List<CategoryJpaEntity> categories = jpaQueryFactory
			.select(categoryJpaEntity)
			.from(categoryJpaEntity)
			.join(categoryJpaEntity.creator, memberJpaEntity)
			.where(
				categoryJpaEntity.transactionType.eq(transactionType),
				categoryJpaEntity.ownerType.eq(ADMIN)
					.or(categoryJpaEntity.ownerType.eq(MEMBER).and(categoryJpaEntity.creator.id.eq(creatorId))),
				categoryJpaEntity.deletedAt.isNull(),
				memberJpaEntity.deletedAt.isNull()
			)
			.orderBy(categoryJpaEntity.name.asc())
			.fetch();

		return categories.stream().map(CategoryJpaEntity::toModel).toList();
	}

	@Override
	public List<Category> findTeamCategories(Long teamId, TransactionType transactionType) {
		List<CategoryJpaEntity> categories = jpaQueryFactory
			.select(categoryJpaEntity)
			.from(categoryJpaEntity)
			.join(categoryJpaEntity.creator, memberJpaEntity)
			.where(
				categoryJpaEntity.transactionType.eq(transactionType),
				categoryJpaEntity.ownerType.eq(ADMIN)
					.or(categoryJpaEntity.ownerType.eq(MEMBER).and(categoryJpaEntity.teamId.eq(teamId))),
				categoryJpaEntity.deletedAt.isNull(),
				memberJpaEntity.deletedAt.isNull()
			)
			.orderBy(categoryJpaEntity.name.asc())
			.fetch();

		return categories.stream().map(CategoryJpaEntity::toModel).toList();
	}
}
