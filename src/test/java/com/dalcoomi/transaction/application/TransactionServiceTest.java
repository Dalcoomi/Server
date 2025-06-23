package com.dalcoomi.transaction.application;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import com.dalcoomi.transaction.dto.ReceiptInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

	@InjectMocks
	private TransactionService transactionService;

	@Mock
	private WebClient webClient;

	@Mock
	private ObjectMapper objectMapper;

	@Test
	@DisplayName("영수증 분석 성공")
	void analyseReceipt_success() throws Exception {
		// given
		MockMultipartFile receipt = new MockMultipartFile(
			"receipt", "test.jpg", "image/jpeg", "test".getBytes());
		List<String> categoryNames = Arrays.asList("카페", "식비");

		String mockResponse = "[{\"date\":\"2025-01-23\",\"categoryName\":\"카페\",\"content\":\"커피\",\"amount\":4800}]";

		// WebClient Mock 체이닝
		WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
		WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
		WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
		WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

		given(webClient.post()).willReturn(requestBodyUriSpec);
		given(requestBodyUriSpec.uri(anyString())).willReturn(requestBodySpec);
		given(requestBodySpec.contentType(any())).willReturn(requestBodySpec);
		given(requestBodySpec.bodyValue(any())).willReturn(requestHeadersSpec);
		given(requestHeadersSpec.retrieve()).willReturn(responseSpec);
		given(responseSpec.onStatus(any(), any())).willReturn(responseSpec);
		given(responseSpec.bodyToMono(String.class)).willReturn(Mono.just(mockResponse));

		// ObjectMapper Mock
		given(objectMapper.writeValueAsString(categoryNames)).willReturn("[\"카페\",\"식비\"]");

		CollectionType mockCollectionType = mock(CollectionType.class);
		given(objectMapper.getTypeFactory()).willReturn(mock(TypeFactory.class));
		given(objectMapper.getTypeFactory().constructCollectionType(List.class, ReceiptInfo.class)).willReturn(
			mockCollectionType);

		List<ReceiptInfo> expectedResult = Collections.singletonList(
			ReceiptInfo.builder()
				.date(LocalDate.of(2025, 1, 23))
				.categoryName("카페")
				.content("커피")
				.amount(4800L)
				.build()
		);
		given(objectMapper.readValue(mockResponse, mockCollectionType)).willReturn(expectedResult);

		// when
		List<ReceiptInfo> result = transactionService.analyseReceipt(receipt, categoryNames);

		// then
		assertThat(result).hasSize(1);
		assertThat(result.getFirst().categoryName()).isEqualTo("카페");
		assertThat(result.getFirst().content()).isEqualTo("커피");
		assertThat(result.getFirst().amount()).isEqualTo(4800L);
	}
}
