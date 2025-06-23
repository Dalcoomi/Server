package com.dalcoomi.category.application;

import static com.dalcoomi.transaction.domain.TransactionType.EXPENSE;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dalcoomi.category.application.repository.CategoryRepository;
import com.dalcoomi.category.domain.Category;
import com.dalcoomi.fixture.CategoryFixture;
import com.dalcoomi.fixture.MemberFixture;
import com.dalcoomi.member.domain.Member;
import com.dalcoomi.team.application.repository.TeamMemberRepository;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

	@InjectMocks
	private CategoryService categoryService;

	@Mock
	private CategoryRepository categoryRepository;

	@Mock
	private TeamMemberRepository teamMemberRepository;

	@Test
	@DisplayName("개인 카테고리 이름 조회 성공")
	void fetch_my_category_names_success() {
		// given
		Member member = MemberFixture.getMember1();

		Category category1 = CategoryFixture.getCategory1(member);
		Category category2 = CategoryFixture.getCategory2(member);
		List<Category> mockCategories = Arrays.asList(category1, category2);

		given(categoryRepository.findMyCategories(member.getId(), EXPENSE)).willReturn(mockCategories);

		// when
		List<String> result = categoryService.fetchCategoryNames(member.getId(), null);

		// then
		assertThat(result).hasSize(2);
		assertThat(result).containsExactly("식비", "카페");
		verify(categoryRepository).findMyCategories(member.getId(), EXPENSE);
		verify(categoryRepository, never()).findTeamCategories(any(), any());
	}

	@Test
	@DisplayName("그룹 카테고리 이름 조회 성공")
	void fetch_team_category_names_success() {
		// given
		Member member = MemberFixture.getMemberWithId1();
		Long teamId = 1L;

		Category category1 = CategoryFixture.getTeamCategory1(member, teamId);
		Category category2 = CategoryFixture.getTeamCategory2(member, teamId);
		List<Category> mockCategories = Arrays.asList(category1, category2);

		given(categoryRepository.findTeamCategories(teamId, EXPENSE)).willReturn(mockCategories);
		given(teamMemberRepository.existsByTeamIdAndMemberId(teamId, member.getId())).willReturn(true);

		// when
		List<String> result = categoryService.fetchCategoryNames(member.getId(), teamId);

		// then
		assertThat(result).hasSize(2);
		assertThat(result).containsExactly("회식", "대관");
		verify(categoryRepository).findTeamCategories(teamId, EXPENSE);
		verify(categoryRepository, never()).findMyCategories(any(), any());
	}
}
