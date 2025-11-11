package com.dalcoomi.transaction.presentation;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.dalcoomi.AbstractContainerBaseTest;
import com.dalcoomi.transaction.dto.ReceiptInfo;
import com.dalcoomi.transaction.dto.request.ReceiptCallbackRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

@Transactional
@SpringBootTest
@TestPropertySource("classpath:application-test.properties")
@AutoConfigureMockMvc
class ReceiptCallbackControllerTest extends AbstractContainerBaseTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	@DisplayName("통합 테스트 - AI 서버로부터 콜백 성공")
	void receipt_callback_success() throws Exception {
		// given
		ReceiptCallbackRequest request = ReceiptCallbackRequest.builder()
			.taskId("receipt-test-123")
			.transactions(List.of(
				ReceiptInfo.builder()
					.date(LocalDate.of(2025, 1, 15))
					.categoryName("식비")
					.content("점심식사")
					.amount(12000L)
					.build(),
				ReceiptInfo.builder()
					.date(LocalDate.of(2025, 1, 15))
					.categoryName("식비")
					.content("커피")
					.amount(5000L)
					.build()
			))
			.build();

		// when & then
		mockMvc.perform(post("/api/transactions/receipts/callback")
				.header("X-API-Key", "test-api-key")
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print())
			.andExpect(status().isOk());
	}

	@Test
	@DisplayName("통합 테스트 - API 키 없이 콜백 요청 시 실패")
	void receipt_callback_without_api_key_fail() throws Exception {
		// given
		ReceiptCallbackRequest request = ReceiptCallbackRequest.builder()
			.taskId("receipt-test-789")
			.transactions(List.of())
			.build();

		// when & then
		mockMvc.perform(post("/api/transactions/receipts/callback")
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print())
			.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("통합 테스트 - 잘못된 API 키로 콜백 요청 시 실패")
	void receipt_callback_with_invalid_api_key_fail() throws Exception {
		// given
		ReceiptCallbackRequest request = ReceiptCallbackRequest.builder()
			.taskId("receipt-test-999")
			.transactions(List.of())
			.build();

		// when & then
		mockMvc.perform(post("/api/transactions/receipts/callback")
				.header("X-API-Key", "wrong-api-key")
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print())
			.andExpect(status().isForbidden());
	}
}
