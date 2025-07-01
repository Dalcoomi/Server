package com.dalcoomi.transaction.presentation;

import static com.dalcoomi.transaction.domain.TransactionType.EXPENSE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import com.dalcoomi.auth.filter.CustomUserDetails;
import com.dalcoomi.category.application.CategoryService;
import com.dalcoomi.transaction.application.TransactionService;
import com.dalcoomi.transaction.domain.Transaction;
import com.dalcoomi.transaction.domain.TransactionType;
import com.dalcoomi.transaction.domain.event.TransactionEventHandler;
import com.dalcoomi.transaction.dto.ReceiptInfo;
import com.dalcoomi.transaction.dto.request.BulkTransactionRequest;
import com.dalcoomi.transaction.dto.request.TransactionRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource("classpath:application-test.properties")
class TransactionControllerMockTest {

	private final GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private TransactionService transactionService;

	@MockitoBean
	private CategoryService categoryService;

	@MockitoBean
	private TransactionEventHandler eventHandler;

	@Test
	@DisplayName("통합 테스트 - 개인 영수증 업로드 성공")
	void upload_my_receipt_success() throws Exception {
		// given
		Long memberId = 1L;

		List<ReceiptInfo> mockReceiptInfos = Arrays.asList(
			ReceiptInfo.builder()
				.date(LocalDate.of(2025, 1, 23))
				.categoryName("카페")
				.content("커피")
				.amount(4800L)
				.build(),
			ReceiptInfo.builder()
				.date(LocalDate.of(2025, 1, 23))
				.categoryName("식비")
				.content("칼국수")
				.amount(12000L)
				.build()
		);

		// Mock 설정
		given(categoryService.fetchCategoryNames(eq(memberId), isNull())).willReturn(Arrays.asList("카페", "식비"));
		given(transactionService.analyseReceipt(any(MultipartFile.class), any(List.class))).willReturn(
			mockReceiptInfos);

		MockMultipartFile receiptFile = new MockMultipartFile(
			"receipt",
			"receipt.jpg",
			"image/jpeg",
			"mock receipt image content".getBytes()
		);

		// 인증 설정
		CustomUserDetails memberUserDetails = new CustomUserDetails(memberId,
			memberId.toString(),
			authoritiesMapper.mapAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))));

		Authentication authentication = new UsernamePasswordAuthenticationToken(memberUserDetails, null,
			authoritiesMapper.mapAuthorities(memberUserDetails.getAuthorities()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		// when & then
		mockMvc.perform(multipart("/api/transactions/upload-receipt")
				.file(receiptFile)
				.contentType(MULTIPART_FORM_DATA))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.transactions").isArray())
			.andExpect(jsonPath("$.transactions.length()").value(2))
			.andExpect(jsonPath("$.transactions[0].transactionDate").value("2025-01-23"))
			.andExpect(jsonPath("$.transactions[0].categoryName").value("카페"))
			.andExpect(jsonPath("$.transactions[0].content").value("커피"))
			.andExpect(jsonPath("$.transactions[0].amount").value(4800))
			.andExpect(jsonPath("$.transactions[1].transactionDate").value("2025-01-23"))
			.andExpect(jsonPath("$.transactions[1].categoryName").value("식비"))
			.andExpect(jsonPath("$.transactions[1].content").value("칼국수"))
			.andExpect(jsonPath("$.transactions[1].amount").value(12000))
			.andDo(print());
	}

	@Test
	@DisplayName("통합 테스트 - 그룹 영수증 업로드 성공")
	void upload_team_receipt_success() throws Exception {
		// given
		Long memberId = 1L;
		Long teamId = 2L;

		List<ReceiptInfo> mockReceiptInfos = Collections.singletonList(
			ReceiptInfo.builder()
				.date(LocalDate.of(2025, 1, 23))
				.categoryName("회식")
				.content("삼겹살")
				.amount(25000L)
				.build()
		);

		// Mock 설정
		given(categoryService.fetchCategoryNames(memberId, teamId)).willReturn(Arrays.asList("회식", "대관"));
		given(transactionService.analyseReceipt(any(MultipartFile.class), any(List.class))).willReturn(
			mockReceiptInfos);

		MockMultipartFile receiptFile = new MockMultipartFile(
			"receipt",
			"receipt.jpg",
			"image/jpeg",
			"mock receipt image content".getBytes()
		);

		// 인증 설정
		CustomUserDetails memberUserDetails = new CustomUserDetails(memberId,
			memberId.toString(),
			authoritiesMapper.mapAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))));

		Authentication authentication = new UsernamePasswordAuthenticationToken(memberUserDetails, null,
			authoritiesMapper.mapAuthorities(memberUserDetails.getAuthorities()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		// when & then
		mockMvc.perform(multipart("/api/transactions/upload-receipt")
				.file(receiptFile)
				.param("teamId", teamId.toString())
				.contentType(MULTIPART_FORM_DATA))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.transactions").isArray())
			.andExpect(jsonPath("$.transactions.length()").value(1))
			.andExpect(jsonPath("$.transactions[0].categoryName").value("회식"))
			.andExpect(jsonPath("$.transactions[0].content").value("삼겹살"))
			.andExpect(jsonPath("$.transactions[0].amount").value(25000))
			.andDo(print());
	}

	@Test
	@DisplayName("통합 테스트 - 개인 거래 내역 일괄 생성 및 AI 서버 전송 성공")
	void create_bulk_transactions_and_send_to_ai_server_success() throws Exception {
		// given
		Long memberId = 1L;
		String taskId = "1-1";

		List<TransactionRequest> transactionRequests = Arrays.asList(
			new TransactionRequest(
				null, // teamId
				4800L,
				"커피",
				LocalDateTime.of(2025, 1, 23, 10, 30),
				EXPENSE,
				1L
			),
			new TransactionRequest(
				null,
				12000L,
				"칼국수",
				LocalDateTime.of(2025, 1, 23, 12, 0),
				EXPENSE,
				2L
			)
		);

		BulkTransactionRequest bulkRequest = BulkTransactionRequest.builder()
			.taskId(taskId)
			.transactions(transactionRequests)
			.build();

		// Mock 설정 - 저장된 거래 내역들
		List<Transaction> mockSavedTransactions = Arrays.asList(
			Transaction.builder()
				.id(1L)
				.amount(4800L)
				.content("커피")
				.transactionDate(LocalDateTime.of(2025, 1, 23, 10, 30))
				.transactionType(EXPENSE)
				.build(),
			Transaction.builder()
				.id(2L)
				.amount(12000L)
				.content("칼국수")
				.transactionDate(LocalDateTime.of(2025, 1, 23, 12, 0))
				.transactionType(EXPENSE)
				.build()
		);

		given(transactionService.create(eq(memberId), any(List.class), any(List.class)))
			.willReturn(mockSavedTransactions);

		// 인증 설정
		CustomUserDetails memberUserDetails = new CustomUserDetails(memberId,
			memberId.toString(),
			authoritiesMapper.mapAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))));

		Authentication authentication = new UsernamePasswordAuthenticationToken(memberUserDetails, null,
			authoritiesMapper.mapAuthorities(memberUserDetails.getAuthorities()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		// when & then
		mockMvc.perform(post("/api/transactions/bulk")
				.content(objectMapper.writeValueAsString(bulkRequest))
				.contentType(APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(print());
	}

	@Test
	@DisplayName("통합 테스트 - 그룹 거래 내역 일괄 생성 및 AI 서버 전송 성공")
	void create_bulk_team_transactions_and_send_to_ai_server_success() throws Exception {
		// given
		Long memberId = 1L;
		Long teamId = 2L;
		String taskId = "team-1";

		List<TransactionRequest> transactionRequests = Arrays.asList(
			new TransactionRequest(
				teamId,
				25000L,
				"회식비",
				LocalDateTime.of(2025, 1, 23, 18, 0),
				TransactionType.EXPENSE,
				3L
			),
			new TransactionRequest(
				teamId,
				15000L,
				"카페비",
				LocalDateTime.of(2025, 1, 23, 14, 30),
				TransactionType.EXPENSE,
				4L
			)
		);

		BulkTransactionRequest bulkRequest = BulkTransactionRequest.builder()
			.taskId(taskId)
			.transactions(transactionRequests)
			.build();

		// Mock 설정
		List<Transaction> mockSavedTransactions = Arrays.asList(
			Transaction.builder()
				.id(3L)
				.teamId(teamId)
				.amount(25000L)
				.content("회식비")
				.transactionDate(LocalDateTime.of(2025, 1, 23, 18, 0))
				.transactionType(TransactionType.EXPENSE)
				.build(),
			Transaction.builder()
				.id(4L)
				.teamId(teamId)
				.amount(15000L)
				.content("카페비")
				.transactionDate(LocalDateTime.of(2025, 1, 23, 14, 30))
				.transactionType(TransactionType.EXPENSE)
				.build()
		);

		given(transactionService.create(eq(memberId), any(List.class), any(List.class)))
			.willReturn(mockSavedTransactions);

		// 인증 설정
		CustomUserDetails memberUserDetails = new CustomUserDetails(memberId,
			memberId.toString(),
			authoritiesMapper.mapAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))));

		Authentication authentication = new UsernamePasswordAuthenticationToken(memberUserDetails, null,
			authoritiesMapper.mapAuthorities(memberUserDetails.getAuthorities()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		// when & then
		mockMvc.perform(post("/api/transactions/bulk")
				.content(objectMapper.writeValueAsString(bulkRequest))
				.contentType(APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(print());
	}
}
