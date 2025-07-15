package com.dalcoomi.category.presentation;

import static com.dalcoomi.transaction.domain.TransactionType.EXPENSE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.dalcoomi.auth.filter.CustomUserDetails;
import com.dalcoomi.category.application.repository.CategoryRepository;
import com.dalcoomi.category.domain.Category;
import com.dalcoomi.fixture.CategoryFixture;
import com.dalcoomi.fixture.MemberFixture;
import com.dalcoomi.fixture.TeamFixture;
import com.dalcoomi.member.application.repository.MemberRepository;
import com.dalcoomi.member.domain.Member;
import com.dalcoomi.team.application.repository.TeamMemberRepository;
import com.dalcoomi.team.application.repository.TeamRepository;
import com.dalcoomi.team.domain.Team;
import com.dalcoomi.team.domain.TeamMember;

@Transactional
@SpringBootTest
@TestPropertySource("classpath:application-test.properties")
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

	private final GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private TeamRepository teamRepository;

	@Autowired
	private TeamMemberRepository teamMemberRepository;

	@Test
	@DisplayName("통합 테스트 - 개인 지출 카테고리 조회 성공")
	void get_my_category_with_transaction_type_success() throws Exception {
		// given
		Member admin = MemberFixture.getMember1();
		admin = memberRepository.save(admin);

		Member member = MemberFixture.getMember2();
		member = memberRepository.save(member);

		Category category1 = CategoryFixture.getCategory1(admin);
		Category category2 = CategoryFixture.getCategory2(admin);
		Category category3 = CategoryFixture.getCategory3(member);

		List<Category> categories = List.of(category1, category2, category3);

		categoryRepository.saveAll(categories);

		// 인증 설정
		setAuthentication(member.getId());

		// when & then
		mockMvc.perform(get("/api/categories")
				.param("transactionType", String.valueOf(EXPENSE))
				.contentType(APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.categories").isArray())
			.andExpect(jsonPath("$.categories.length()").value(3))
			.andExpect(jsonPath("$.categories[0].name").value(category1.getName()))
			.andExpect(jsonPath("$.categories[1].name").value(category2.getName()))
			.andExpect(jsonPath("$.categories[2].name").value(category3.getName()))
			.andExpect(jsonPath("$.categories[0].iconUrl").value(category1.getIconUrl()))
			.andExpect(jsonPath("$.categories[1].iconUrl").value(category2.getIconUrl()))
			.andExpect(jsonPath("$.categories[2].iconUrl").value(category3.getIconUrl()))
			.andExpect(jsonPath("$.categories[0].ownerType").value(category1.getOwnerType().name()))
			.andExpect(jsonPath("$.categories[1].ownerType").value(category2.getOwnerType().name()))
			.andExpect(jsonPath("$.categories[2].ownerType").value(category3.getOwnerType().name()))
			.andDo(print());
	}

	@Test
	@DisplayName("통합 테스트 - 그룹 지출 카테고리 조회 성공")
	void get_team_category_with_transaction_type_success() throws Exception {
		// given
		Member member = MemberFixture.getMember1();
		member = memberRepository.save(member);

		Team team = TeamFixture.getTeam1(member);
		team = teamRepository.save(team);

		TeamMember teamMember1 = TeamMember.of(team, member);
		teamMemberRepository.save(teamMember1);

		Category category1 = CategoryFixture.getCategory1(member);
		Category category2 = CategoryFixture.getCategory2(member);
		Category category3 = CategoryFixture.getCategory3(member);

		List<Category> categories = List.of(category1, category2, category3);

		categoryRepository.saveAll(categories);

		// 인증 설정
		setAuthentication(member.getId());

		// when & then
		mockMvc.perform(get("/api/categories")
				.param("teamId", team.getId().toString())
				.param("transactionType", String.valueOf(EXPENSE))
				.contentType(APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.categories").isArray())
			.andExpect(jsonPath("$.categories.length()").value(2))
			.andExpect(jsonPath("$.categories[0].name").value(category1.getName()))
			.andExpect(jsonPath("$.categories[1].name").value(category2.getName()))
			.andExpect(jsonPath("$.categories[0].iconUrl").value(category1.getIconUrl()))
			.andExpect(jsonPath("$.categories[1].iconUrl").value(category2.getIconUrl()))
			.andExpect(jsonPath("$.categories[0].ownerType").value(category1.getOwnerType().name()))
			.andExpect(jsonPath("$.categories[1].ownerType").value(category2.getOwnerType().name()))
			.andDo(print());
	}

	private void setAuthentication(Long memberId) {
		CustomUserDetails memberUserDetails = new CustomUserDetails(memberId, memberId.toString(),
			authoritiesMapper.mapAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))));

		Authentication authentication = new UsernamePasswordAuthenticationToken(memberUserDetails, null,
			authoritiesMapper.mapAuthorities(memberUserDetails.getAuthorities()));

		SecurityContextHolder.getContext().setAuthentication(authentication);
	}
}
