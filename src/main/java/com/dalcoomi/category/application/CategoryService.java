package com.dalcoomi.category.application;

import static com.dalcoomi.common.error.model.ErrorMessage.TEAM_MEMBER_NOT_FOUND;
import static com.dalcoomi.transaction.domain.TransactionType.EXPENSE;

import java.util.List;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dalcoomi.category.application.repository.CategoryRepository;
import com.dalcoomi.category.domain.Category;
import com.dalcoomi.category.dto.CategoryInfo;
import com.dalcoomi.common.error.exception.NotFoundException;
import com.dalcoomi.team.application.repository.TeamMemberRepository;
import com.dalcoomi.transaction.domain.TransactionType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {

	private final CategoryRepository categoryRepository;
	private final TeamMemberRepository teamMemberRepository;

	@Transactional(readOnly = true)
	public List<CategoryInfo> getMyCategories(Long memberId, TransactionType transactionType) {
		List<Category> categories = categoryRepository.findMyCategories(memberId, transactionType);

		return categories.stream().map(CategoryInfo::from).toList();
	}

	@Transactional(readOnly = true)
	public List<CategoryInfo> getTeamCategories(Long memberId, Long teamId, TransactionType transactionType) {
		validateTeamMember(teamId, memberId);

		List<Category> categories = categoryRepository.findTeamCategories(teamId, transactionType);

		return categories.stream().map(CategoryInfo::from).toList();
	}

	@Transactional(readOnly = true)
	public List<String> fetchCategoryNames(Long memberId, @Nullable Long teamId) {
		List<Category> categories;

		if (teamId != null) {
			validateTeamMember(teamId, memberId);

			categories = categoryRepository.findTeamCategories(teamId, EXPENSE);
		} else {
			categories = categoryRepository.findMyCategories(memberId, EXPENSE);
		}

		return categories.stream().map(Category::getName).toList();
	}

	private void validateTeamMember(Long teamId, Long memberId) {
		if (teamId == null) {
			return;
		}

		if (!teamMemberRepository.existsByTeamIdAndMemberId(teamId, memberId)) {
			throw new NotFoundException(TEAM_MEMBER_NOT_FOUND);
		}
	}
}
