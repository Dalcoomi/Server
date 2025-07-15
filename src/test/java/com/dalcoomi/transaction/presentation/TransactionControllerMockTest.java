package com.dalcoomi.transaction.presentation;

import static com.dalcoomi.common.error.model.ErrorMessage.LOCK_EXIST_ERROR;
import static com.dalcoomi.transaction.domain.TransactionType.EXPENSE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
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

import com.dalcoomi.AbstractContainerBaseTest;
import com.dalcoomi.auth.filter.CustomUserDetails;
import com.dalcoomi.category.application.CategoryService;
import com.dalcoomi.common.util.lock.LockManager;
import com.dalcoomi.transaction.application.TransactionService;
import com.dalcoomi.transaction.domain.event.TransactionEventHandler;
import com.dalcoomi.transaction.dto.ReceiptInfo;
import com.dalcoomi.transaction.dto.request.SaveReceiptRequest;
import com.dalcoomi.transaction.dto.request.TransactionRequest;
import com.dalcoomi.transaction.dto.response.AiReceiptResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@TestPropertySource("classpath:application-test.properties")
@AutoConfigureMockMvc(addFilters = false)
class TransactionControllerMockTest extends AbstractContainerBaseTest {

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
	private LockManager lockManager;

	@MockitoBean
	private TransactionEventHandler eventHandler;

	@BeforeEach
	void setUp() {
		// Mock 설정 초기화
		reset(lockManager);

		// 기본적으로 락이 성공하도록 설정
		given(lockManager.lock(anyString())).willReturn(true);
		given(lockManager.unlock(anyString())).willReturn(true);
	}

	@Test
	@DisplayName("통합 테스트 - 개인 영수증 업로드 성공")
	void upload_my_receipt_success() throws Exception {
		// given
		Long memberId = 1L;
		String taskId = "1-1";
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
		AiReceiptResponse response = AiReceiptResponse.builder().taskId(taskId).transactions(mockReceiptInfos).build();

		// Mock 설정
		given(categoryService.fetchCategoryNames(eq(memberId), isNull())).willReturn(Arrays.asList("카페", "식비"));
		given(transactionService.analyseReceipt(any(MultipartFile.class), any(List.class))).willReturn(response);

		MockMultipartFile receiptFile = new MockMultipartFile(
			"receipt",
			"receipt.jpg",
			"image/jpeg",
			"mock receipt image content".getBytes()
		);

		// 인증 설정
		setAuthentication(memberId);

		// when & then
		mockMvc.perform(multipart("/api/transactions/receipts/upload")
				.file(receiptFile)
				.contentType(MULTIPART_FORM_DATA))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.taskId").value(taskId))
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
		String taskId = "1-1";
		List<ReceiptInfo> mockReceiptInfos = Collections.singletonList(
			ReceiptInfo.builder()
				.date(LocalDate.of(2025, 1, 23))
				.categoryName("회식")
				.content("삼겹살")
				.amount(25000L)
				.build()
		);
		AiReceiptResponse response = AiReceiptResponse.builder().taskId(taskId).transactions(mockReceiptInfos).build();

		// Mock 설정
		given(categoryService.fetchCategoryNames(memberId, teamId)).willReturn(Arrays.asList("회식", "대관"));
		given(transactionService.analyseReceipt(any(MultipartFile.class), any(List.class))).willReturn(response);

		MockMultipartFile receiptFile = new MockMultipartFile(
			"receipt",
			"receipt.jpg",
			"image/jpeg",
			"mock receipt image content".getBytes()
		);

		// 인증 설정
		setAuthentication(memberId);

		// when & then
		mockMvc.perform(multipart("/api/transactions/receipts/upload")
				.file(receiptFile)
				.param("teamId", teamId.toString())
				.contentType(MULTIPART_FORM_DATA))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.taskId").value(taskId))
			.andExpect(jsonPath("$.transactions").isArray())
			.andExpect(jsonPath("$.transactions.length()").value(1))
			.andExpect(jsonPath("$.transactions[0].categoryName").value("회식"))
			.andExpect(jsonPath("$.transactions[0].content").value("삼겹살"))
			.andExpect(jsonPath("$.transactions[0].amount").value(25000))
			.andDo(print());
	}

	@Test
	@DisplayName("통합 테스트 - 파일 해시 생성 실패 시 파일명/크기로 대체 성공")
	void file_hash_generation_success() throws IOException {
		// given
		Long memberId = 1L;
		String taskId = "1-1";
		List<ReceiptInfo> mockReceiptInfos = Collections.singletonList(
			ReceiptInfo.builder()
				.date(LocalDate.of(2025, 1, 23))
				.categoryName("카페")
				.content("커피")
				.amount(4800L)
				.build()
		);
		AiReceiptResponse response = AiReceiptResponse.builder().taskId(taskId).transactions(mockReceiptInfos).build();

		given(categoryService.fetchCategoryNames(eq(memberId), isNull())).willReturn(Arrays.asList("카페", "식비"));
		given(transactionService.analyseReceipt(any(MultipartFile.class), any(List.class))).willReturn(response);

		MockMultipartFile originalFile = new MockMultipartFile(
			"receipt",
			"test-receipt-hash-fail.jpg",
			"image/jpeg",
			"hash failure test content".getBytes()
		);

		MockMultipartFile spyFile = spy(originalFile);
		willThrow(new IOException("Hash generation failed")).given(spyFile).getBytes();

		// 인증 설정
		setAuthentication(memberId);

		// when & then
		assertDoesNotThrow(() -> {
			mockMvc.perform(multipart("/api/transactions/receipts/upload")
					.file(spyFile)
					.contentType(MULTIPART_FORM_DATA))
				.andExpect(status().isOk())
				.andDo(print());
		});
	}

	@Test
	@DisplayName("통합 테스트 - Redis 연결 실패 시 lock 메서드 예외 발생 성공")
	void receipt_save_with_lock_manager_lock_method_exception_success() throws Exception {
		// given
		Long memberId = 1L;
		Long categoryId = 1L;

		given(lockManager.lock(anyString())).willThrow(new RuntimeException("Redis connection failed"));

		String taskId = "1-1";
		List<TransactionRequest> transactionRequests = List.of(
			new TransactionRequest(null, 3000L, "락 예외 테스트", LocalDateTime.of(2025, 1, 23, 10, 30), EXPENSE,
				categoryId));
		SaveReceiptRequest saveRequest = SaveReceiptRequest.builder()
			.taskId(taskId)
			.transactions(transactionRequests)
			.build();

		// 인증 설정
		setAuthentication(memberId);

		// when & then
		mockMvc.perform(post("/api/transactions/receipts/save")
				.content(objectMapper.writeValueAsString(saveRequest))
				.contentType(APPLICATION_JSON))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.message").value(LOCK_EXIST_ERROR.getMessage()))
			.andDo(print());
	}

	@Test
	@DisplayName("통합 테스트 - Redis 연결 실패 시 unlock 메서드 예외 발생 성공")
	void receipt_save_with_lock_manager_unlock_method_exception_success() throws Exception {
		// given
		Long memberId = 1L;
		Long categoryId = 1L;
		String taskId = "1-1";

		given(lockManager.unlock(anyString())).willThrow(new RuntimeException("Redis connection failed"));

		List<TransactionRequest> transactionRequests = List.of(
			new TransactionRequest(null, 3000L, "락 예외 테스트", LocalDateTime.of(2025, 1, 23, 10, 30), EXPENSE,
				categoryId));
		SaveReceiptRequest saveRequest = SaveReceiptRequest.builder()
			.taskId(taskId)
			.transactions(transactionRequests)
			.build();

		// 인증 설정
		setAuthentication(memberId);

		// when - 첫 번째 요청 (unlock 실패)
		mockMvc.perform(post("/api/transactions/receipts/save")
				.content(objectMapper.writeValueAsString(saveRequest))
				.contentType(APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(print());

		reset(lockManager);
		given(lockManager.lock(anyString())).willReturn(false);

		// then - 두 번째 요청 (락 충돌)
		mockMvc.perform(post("/api/transactions/receipts/save")
				.content(objectMapper.writeValueAsString(saveRequest))
				.contentType(APPLICATION_JSON))
			.andExpect(status().isConflict())  // 409 Conflict
			.andExpect(jsonPath("$.message").value(LOCK_EXIST_ERROR.getMessage()))
			.andDo(print());

		reset(lockManager);
		given(lockManager.lock(anyString())).willReturn(true);
		given(lockManager.unlock(anyString())).willReturn(true);

		// then - 세 번째 요청 (TTL 만료 후 성공)
		mockMvc.perform(post("/api/transactions/receipts/save")
				.content(objectMapper.writeValueAsString(saveRequest))
				.contentType(APPLICATION_JSON))
			.andExpect(status().isOk())
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
