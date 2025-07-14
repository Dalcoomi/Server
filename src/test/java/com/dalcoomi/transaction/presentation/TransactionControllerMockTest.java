package com.dalcoomi.transaction.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
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
import com.dalcoomi.transaction.domain.event.TransactionEventHandler;
import com.dalcoomi.transaction.dto.ReceiptInfo;
import com.dalcoomi.transaction.dto.response.AiReceiptResponse;

@SpringBootTest
@TestPropertySource("classpath:application-test.properties")
@AutoConfigureMockMvc(addFilters = false)
class TransactionControllerMockTest {

	private final GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

	@Autowired
	private MockMvc mockMvc;

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

	private void setAuthentication(Long memberId) {
		CustomUserDetails memberUserDetails = new CustomUserDetails(memberId, memberId.toString(),
			authoritiesMapper.mapAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))));

		Authentication authentication = new UsernamePasswordAuthenticationToken(memberUserDetails, null,
			authoritiesMapper.mapAuthorities(memberUserDetails.getAuthorities()));

		SecurityContextHolder.getContext().setAuthentication(authentication);
	}
}
