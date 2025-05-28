package com.dalcoomi.transaction.presentation;

import static com.dalcoomi.transaction.domain.TransactionType.EXPENSE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
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
import com.dalcoomi.fixture.TeamFixture;
import com.dalcoomi.fixture.TransactionFixture;
import com.dalcoomi.member.application.repository.MemberRepository;
import com.dalcoomi.member.domain.Member;
import com.dalcoomi.team.application.repository.TeamMemberRepository;
import com.dalcoomi.team.application.repository.TeamRepository;
import com.dalcoomi.team.domain.Team;
import com.dalcoomi.team.domain.TeamMember;
import com.dalcoomi.transaction.application.repository.TransactionRepository;
import com.dalcoomi.transaction.domain.Transaction;
import com.dalcoomi.transaction.dto.TransactionSearchCriteria;
import com.dalcoomi.transaction.dto.request.TransactionRequest;
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

	@Autowired
	private TeamRepository teamRepository;

	@Autowired
	private TeamMemberRepository teamMemberRepository;

	@Test
	@DisplayName("통합 테스트 - 개인 거래 내역 저장 성공")
	void save_my_transaction_success() throws Exception {
		// given
		Member member = MemberFixture.getMember1();
		member = memberRepository.save(member);

		// 인증 설정
		CustomUserDetails memberUserDetails = new CustomUserDetails(member.getId(),
			member.getId().toString(),
			authoritiesMapper.mapAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))));

		Authentication authentication = new UsernamePasswordAuthenticationToken(memberUserDetails, null,
			authoritiesMapper.mapAuthorities(memberUserDetails.getAuthorities()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		Category category = CategoryFixture.getCategory1(member);
		category = categoryRepository.save(category);

		Long amount = 30000L;
		String content = "앙";
		LocalDateTime transactionDate = LocalDateTime.of(2025, 3, 1, 12, 0);

		TransactionRequest request = new TransactionRequest(null, amount, content, transactionDate, EXPENSE,
			category.getId());

		// when & then
		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(post("/api/transaction")
				.content(json)
				.contentType(APPLICATION_JSON))
			.andExpect(status().isCreated())
			.andDo(print());

		TransactionSearchCriteria criteria = TransactionSearchCriteria.of(member.getId(), null,
			transactionDate.getYear(), transactionDate.getMonthValue());

		List<Transaction> transactions = transactionRepository.findTransactions(criteria);

		assertThat(transactions.getFirst().getCreator().getId()).isEqualTo(member.getId());
		assertThat(transactions.getFirst().getAmount()).isEqualTo(amount);
		assertThat(transactions.getFirst().getContent()).isEqualTo(content);
		assertThat(transactions.getFirst().getTransactionDate()).isEqualTo(transactionDate);
		assertThat(transactions.getFirst().getTransactionType()).isEqualTo(EXPENSE);
	}

	@Test
	@DisplayName("통합 테스트 - 그룹 거래 내역 저장 성공")
	void save_team_transaction_success() throws Exception {
		// given
		Member member = MemberFixture.getMember1();
		member = memberRepository.save(member);

		Team team = TeamFixture.getTeam1(member);
		team = teamRepository.save(team);

		TeamMember leaderTeamMember = TeamMember.of(team, member);
		teamMemberRepository.save(leaderTeamMember);

		// 인증 설정
		CustomUserDetails memberUserDetails = new CustomUserDetails(member.getId(),
			member.getId().toString(),
			authoritiesMapper.mapAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))));

		Authentication authentication = new UsernamePasswordAuthenticationToken(memberUserDetails, null,
			authoritiesMapper.mapAuthorities(memberUserDetails.getAuthorities()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		Category category = CategoryFixture.getCategory1(member);
		category = categoryRepository.save(category);

		Long amount = 30000L;
		String content = "앙";
		LocalDateTime transactionDate = LocalDateTime.of(2025, 3, 1, 12, 0);

		TransactionRequest request = new TransactionRequest(team.getId(), amount, content, transactionDate, EXPENSE,
			category.getId());

		// when & then
		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(post("/api/transaction")
				.content(json)
				.contentType(APPLICATION_JSON))
			.andExpect(status().isCreated())
			.andDo(print());

		TransactionSearchCriteria criteria = TransactionSearchCriteria.of(member.getId(), null,
			transactionDate.getYear(), transactionDate.getMonthValue());

		List<Transaction> transactions = transactionRepository.findTransactions(criteria);

		assertThat(transactions.getFirst().getCreator().getId()).isEqualTo(member.getId());
		assertThat(transactions.getFirst().getAmount()).isEqualTo(amount);
		assertThat(transactions.getFirst().getContent()).isEqualTo(content);
		assertThat(transactions.getFirst().getTransactionDate()).isEqualTo(transactionDate);
		assertThat(transactions.getFirst().getTransactionType()).isEqualTo(EXPENSE);
	}

	@Test
	@DisplayName("통합 테스트 - 개인 거래 내역 리스트 조회 성공")
	void get_my_transactions_with_year_and_month_success() throws Exception {
		// given
		Member member = MemberFixture.getMember1();
		member = memberRepository.save(member);

		Team team = TeamFixture.getTeam1(member);
		team = teamRepository.save(team);

		TeamMember teamMember = TeamMember.of(team, member);
		teamMemberRepository.save(teamMember);

		// 인증 설정
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
		Transaction transaction5 = TransactionFixture.getTeamTransactionWithExpense1(member, team.getId(), category);
		Transaction transaction6 = TransactionFixture.getTeamTransactionWithExpense2(member, team.getId(), category);
		Transaction transaction7 = TransactionFixture.getTeamTransactionWithExpense3(member, team.getId(), category);
		Transaction transaction8 = TransactionFixture.getTeamTransactionWithExpense4(member, team.getId(), category);
		List<Transaction> transactions = Arrays.asList(transaction1, transaction2, transaction3, transaction4,
			transaction5, transaction6, transaction7, transaction8);
		transactionRepository.saveAll(transactions);

		int year = 2025;
		int month = 3;

		// when & then
		mockMvc.perform(get("/api/transaction")
				.param("year", String.valueOf(year))
				.param("month", String.valueOf(month))
				.contentType(APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.income").value(0))
			.andExpect(jsonPath("$.expense").value(
				transaction1.getAmount() + transaction2.getAmount() + transaction3.getAmount()
					+ transaction5.getAmount() + transaction6.getAmount() + transaction7.getAmount()))
			.andExpect(jsonPath("$.total").value(
				-(transaction1.getAmount() + transaction2.getAmount() + transaction3.getAmount()
					+ transaction5.getAmount() + transaction6.getAmount() + transaction7.getAmount())))
			.andExpect(jsonPath("$.transactions").isArray())
			.andExpect(jsonPath("$.transactions.length()").value(6))
			.andExpect(jsonPath("$.transactions[0].content").value(transaction7.getContent()))
			.andExpect(jsonPath("$.transactions[1].content").value(transaction6.getContent()))
			.andExpect(jsonPath("$.transactions[2].content").value(transaction5.getContent()))
			.andExpect(jsonPath("$.transactions[0].amount").value(transaction7.getAmount()))
			.andExpect(jsonPath("$.transactions[0].transactionType").value(transaction7.getTransactionType().name()))
			.andExpect(jsonPath("$.transactions[0].categoryName").value(category.getName()))
			.andExpect(jsonPath("$.transactions[0].creatorNickname").value(member.getNickname()))
			.andDo(print());
	}

	@Test
	@DisplayName("통합 테스트 - 데이터가 없는 달의 개인 거래 내역 조회 시 빈 배열 조회 성공")
	void get_my_transactions_empty_month_success() throws Exception {
		// given
		Member member = MemberFixture.getMember1();
		member = memberRepository.save(member);

		// 인증 설정
		CustomUserDetails memberUserDetails = new CustomUserDetails(member.getId(),
			member.getId().toString(),
			authoritiesMapper.mapAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))));

		Authentication authentication = new UsernamePasswordAuthenticationToken(memberUserDetails, null,
			authoritiesMapper.mapAuthorities(memberUserDetails.getAuthorities()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		int year = 2024;
		int month = 12;

		// when & then
		mockMvc.perform(get("/api/transaction")
				.param("year", String.valueOf(year))
				.param("month", String.valueOf(month))
				.contentType(APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.income").value(0))
			.andExpect(jsonPath("$.expense").value(0))
			.andExpect(jsonPath("$.total").value(0))
			.andExpect(jsonPath("$.transactions").isArray())
			.andExpect(jsonPath("$.transactions.length()").value(0))
			.andDo(print());
	}

	@Test
	@DisplayName("통합 테스트 - 그룹 거래 내역 리스트 조회 성공")
	void get_team_transactions_success() throws Exception {
		// given
		Member member = MemberFixture.getMember1();
		member = memberRepository.save(member);

		Team team = TeamFixture.getTeam1(member);
		team = teamRepository.save(team);

		TeamMember teamMember = TeamMember.of(team, member);
		teamMemberRepository.save(teamMember);

		// 인증 설정
		CustomUserDetails memberUserDetails = new CustomUserDetails(member.getId(),
			member.getId().toString(),
			authoritiesMapper.mapAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))));

		Authentication authentication = new UsernamePasswordAuthenticationToken(memberUserDetails, null,
			authoritiesMapper.mapAuthorities(memberUserDetails.getAuthorities()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		Category category = CategoryFixture.getCategory1(member);
		category = categoryRepository.save(category);

		Transaction transaction1 = TransactionFixture.getTeamTransactionWithExpense1(member, team.getId(), category);
		Transaction transaction2 = TransactionFixture.getTeamTransactionWithExpense2(member, team.getId(), category);
		Transaction transaction3 = TransactionFixture.getTeamTransactionWithExpense3(member, team.getId(), category);
		Transaction transaction4 = TransactionFixture.getTeamTransactionWithExpense4(member, team.getId(), category);
		List<Transaction> transactions = Arrays.asList(transaction1, transaction2, transaction3, transaction4);
		transactionRepository.saveAll(transactions);

		int year = 2025;
		int month = 3;

		// when & then
		mockMvc.perform(get("/api/transaction")
				.param("teamId", String.valueOf(team.getId()))
				.param("year", String.valueOf(year))
				.param("month", String.valueOf(month))
				.contentType(APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.income").value(0))
			.andExpect(jsonPath("$.expense").value(
				transaction1.getAmount() + transaction2.getAmount() + transaction3.getAmount()))
			.andExpect(jsonPath("$.total").value(
				-(transaction1.getAmount() + transaction2.getAmount() + transaction3.getAmount())))
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
	@DisplayName("통합 테스트 - 데이터가 없는 달의 그룹 거래 내역 조회 시 빈 배열 조회 성공")
	void get_team_transactions_empty_month_success() throws Exception {
		// given
		Member member = MemberFixture.getMember1();
		member = memberRepository.save(member);

		Team team = TeamFixture.getTeam1(member);
		team = teamRepository.save(team);

		TeamMember teamMember = TeamMember.of(team, member);
		teamMemberRepository.save(teamMember);

		// 인증 설정
		CustomUserDetails memberUserDetails = new CustomUserDetails(member.getId(),
			member.getId().toString(),
			authoritiesMapper.mapAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))));

		Authentication authentication = new UsernamePasswordAuthenticationToken(memberUserDetails, null,
			authoritiesMapper.mapAuthorities(memberUserDetails.getAuthorities()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		int year = 2024;
		int month = 12;

		// when & then
		mockMvc.perform(get("/api/transaction")
				.param("teamId", String.valueOf(team.getId()))
				.param("year", String.valueOf(year))
				.param("month", String.valueOf(month))
				.contentType(APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.income").value(0))
			.andExpect(jsonPath("$.expense").value(0))
			.andExpect(jsonPath("$.total").value(0))
			.andExpect(jsonPath("$.transactions").isArray())
			.andExpect(jsonPath("$.transactions.length()").value(0))
			.andDo(print());
	}

	@Test
	@DisplayName("통합 테스트 - 특정 거래 내역 조회 성공")
	void get_my_transaction_success() throws Exception {
		// given
		Member member = MemberFixture.getMember1();
		member = memberRepository.save(member);

		// 인증 설정
		CustomUserDetails memberUserDetails = new CustomUserDetails(member.getId(),
			member.getId().toString(),
			authoritiesMapper.mapAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))));

		Authentication authentication = new UsernamePasswordAuthenticationToken(memberUserDetails, null,
			authoritiesMapper.mapAuthorities(memberUserDetails.getAuthorities()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		Category category = CategoryFixture.getCategory1(member);
		category = categoryRepository.save(category);

		Transaction transaction1 = TransactionFixture.getTransactionWithExpense1(member, category);
		transaction1 = transactionRepository.save(transaction1);

		// when & then
		mockMvc.perform(get("/api/transaction/{transactionId}", transaction1.getId())
				.contentType(APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.transactionId").value(transaction1.getId()))
			.andExpect(jsonPath("$.amount").value(transaction1.getAmount()))
			.andExpect(jsonPath("$.content").value(transaction1.getContent()))
			.andExpect(jsonPath("$.transactionType").value(transaction1.getTransactionType().name()))
			.andExpect(jsonPath("$.categoryName").value(category.getName()))
			.andExpect(jsonPath("$.iconUrl").value(category.getIconUrl()))
			.andDo(print());
	}

	@Test
	@DisplayName("통합 테스트 - 특정 거래 내역 수정 성공")
	void update_transaction_success() throws Exception {
		// given
		Member member = MemberFixture.getMember1();
		member = memberRepository.save(member);

		// 인증 설정
		CustomUserDetails memberUserDetails = new CustomUserDetails(member.getId(),
			member.getId().toString(),
			authoritiesMapper.mapAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))));

		Authentication authentication = new UsernamePasswordAuthenticationToken(memberUserDetails, null,
			authoritiesMapper.mapAuthorities(memberUserDetails.getAuthorities()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		Category category = CategoryFixture.getCategory1(member);
		category = categoryRepository.save(category);

		Transaction transaction1 = TransactionFixture.getTransactionWithExpense1(member, category);
		transaction1 = transactionRepository.save(transaction1);

		Long amount = 20000L;
		String content = "엉";
		LocalDateTime transactionDate = LocalDateTime.of(2025, 3, 2, 12, 0);
		TransactionRequest request = new TransactionRequest(null, amount, content, transactionDate, EXPENSE,
			category.getId());

		// when & then
		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(put("/api/transaction/{transactionId}", transaction1.getId())
				.content(json)
				.contentType(APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(print());

		Transaction transaction = transactionRepository.findByIdAndCreatorId(transaction1.getId(), member.getId());

		assertThat(transaction.getCreator().getId()).isEqualTo(member.getId());
		assertThat(transaction.getAmount()).isEqualTo(amount);
		assertThat(transaction.getContent()).isEqualTo(content);
		assertThat(transaction.getTransactionDate()).isEqualTo(transactionDate);
		assertThat(transaction.getTransactionType()).isEqualTo(EXPENSE);
	}

	@Test
	@DisplayName("통합 테스트 - 특정 거래 내역 삭제 성공")
	void delete_transaction_success() throws Exception {
		// given
		Member member = MemberFixture.getMember1();
		member = memberRepository.save(member);

		// 인증 설정
		CustomUserDetails memberUserDetails = new CustomUserDetails(member.getId(),
			member.getId().toString(),
			authoritiesMapper.mapAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))));

		Authentication authentication = new UsernamePasswordAuthenticationToken(memberUserDetails, null,
			authoritiesMapper.mapAuthorities(memberUserDetails.getAuthorities()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		Category category = CategoryFixture.getCategory1(member);
		category = categoryRepository.save(category);

		Transaction transaction1 = TransactionFixture.getTransactionWithExpense1(member, category);
		transaction1 = transactionRepository.save(transaction1);

		// when & then
		mockMvc.perform(delete("/api/transaction/{transactionId}", transaction1.getId())
				.contentType(APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(print());
	}
}
