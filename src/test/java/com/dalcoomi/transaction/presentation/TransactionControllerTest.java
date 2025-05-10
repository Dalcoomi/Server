package com.dalcoomi.transaction.presentation;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
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
import com.dalcoomi.fixture.TransactionFixture;
import com.dalcoomi.member.application.repository.MemberRepository;
import com.dalcoomi.member.domain.Member;
import com.dalcoomi.transaction.application.repository.TransactionRepository;
import com.dalcoomi.transaction.domain.Transaction;
import com.fasterxml.jackson.databind.ObjectMapper;

@Transactional
@SpringBootTest
@TestPropertySource("classpath:application-test.properties")
@AutoConfigureMockMvc(addFilters = false)
class TransactionControllerTest {

	private final GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private TransactionRepository transactionRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Test
	@DisplayName("통합 테스트 - 개인 거래 내역 조회 성공")
	void get_my_transactions_with_year_and_month_success() throws Exception {
		// given
		Member member = MemberFixture.getMember1();

		member = memberRepository.save(member);

		CustomUserDetails memberUserDetails = new CustomUserDetails(member.getId(),
			member.getId().toString(),
			authoritiesMapper.mapAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))));

		Authentication authentication = new UsernamePasswordAuthenticationToken(memberUserDetails, null,
			authoritiesMapper.mapAuthorities(memberUserDetails.getAuthorities()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		Category category = CategoryFixture.getCategory1(member);

		category = categoryRepository.save(category);

		Transaction transaction1 = TransactionFixture.getTransactionWithExpense1(member, category);
		Transaction transaction2 = TransactionFixture.getTransactionWithExpense2(member, category);
		Transaction transaction3 = TransactionFixture.getTransactionWithExpense3(member, category);
		Transaction transaction4 = TransactionFixture.getTransactionWithExpense4(member, category);

		List<Transaction> transactions = Arrays.asList(transaction1, transaction2, transaction3, transaction4);

		transactionRepository.saveAll(transactions);

		int year = 2025;
		int month = 3;

		// when & then
		mockMvc.perform(get("/api/transaction/my")
				.param("year", String.valueOf(year))
				.param("month", String.valueOf(month))
				.contentType(APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.transactions").isArray())
			.andExpect(jsonPath("$.transactions.length()").value(3))
			.andExpect(jsonPath("$.transactions[0].content").value(transaction3.getContent()))
			.andExpect(jsonPath("$.transactions[1].content").value(transaction2.getContent()))
			.andExpect(jsonPath("$.transactions[2].content").value(transaction1.getContent()))
			.andExpect(jsonPath("$.transactions[0].amount").value(transaction3.getAmount()))
			.andExpect(jsonPath("$.transactions[0].transactionType").value(transaction3.getTransactionType().name()))
			.andExpect(jsonPath("$.transactions[0].categoryName").value(category.getName()))
			.andExpect(jsonPath("$.transactions[0].creatorNickname").value(member.getNickname()))
			.andDo(print());
	}

	@Test
	@DisplayName("통합 테스트 - 데이터가 없는 달의 거래 내역 조회")
	void get_transactions_empty_month() throws Exception {
		// given
		Member member = MemberFixture.getMember1();

		member = memberRepository.save(member);

		CustomUserDetails memberUserDetails = new CustomUserDetails(member.getId(),
			member.getId().toString(),
			authoritiesMapper.mapAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))));

		Authentication authentication = new UsernamePasswordAuthenticationToken(memberUserDetails, null,
			authoritiesMapper.mapAuthorities(memberUserDetails.getAuthorities()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		int year = 2024;
		int month = 12;

		// when & then
		mockMvc.perform(get("/api/transaction/my")
				.param("year", String.valueOf(year))
				.param("month", String.valueOf(month))
				.contentType(APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.transactions").isArray())
			.andExpect(jsonPath("$.transactions.length()").value(0))
			.andDo(print());
	}
}
