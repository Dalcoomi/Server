package com.dalcoomi.transaction.application;

import static com.dalcoomi.transaction.domain.TransactionType.EXPENSE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import com.dalcoomi.category.domain.Category;
import com.dalcoomi.common.error.exception.DalcoomiException;
import com.dalcoomi.fixture.CategoryFixture;
import com.dalcoomi.fixture.MemberFixture;
import com.dalcoomi.member.domain.Member;
import com.dalcoomi.transaction.domain.Transaction;

import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

	@InjectMocks
	private TransactionService transactionService;

	@Mock
	private WebClient webClient;

	@Test
	@DisplayName("다수 거래 내역 AI 서버 전송 성공")
	void send_to_ai_server_success() {
		// given
		Member member = MemberFixture.getMemberWithId1();
		Category category = CategoryFixture.getCategory1(member);

		String taskId = "1-1";
		List<Transaction> transactions = Collections.singletonList(
			Transaction.builder()
				.id(1L)
				.amount(4800L)
				.content("커피")
				.transactionDate(LocalDateTime.of(2025, 1, 23, 10, 30))
				.transactionType(EXPENSE)
				.category(category)
				.build()
		);

		// WebClient Mock 설정
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
		given(responseSpec.bodyToMono(String.class)).willReturn(Mono.just("성공"));

		// when & then
		assertThatNoException().isThrownBy(() -> transactionService.sendToAiServer(taskId, transactions));
	}

	@Test
	@DisplayName("다수 거래 내역 AI 서버 전송 실패")
	void send_to_ai_server_fail() {
		// given
		Member member = MemberFixture.getMemberWithId1();
		Category category = CategoryFixture.getCategory1(member);

		String taskId = "1-1";
		List<Transaction> transactions = Collections.singletonList(
			Transaction.builder()
				.id(1L)
				.amount(4800L)
				.content("커피")
				.transactionDate(LocalDateTime.of(2025, 1, 23, 10, 30))
				.transactionType(EXPENSE)
				.category(category)
				.build()
		);

		given(webClient.post()).willThrow(new RuntimeException("네트워크 오류"));

		// when & then
		assertThatThrownBy(() -> transactionService.sendToAiServer(taskId, transactions))
			.isInstanceOf(DalcoomiException.class)
			.hasMessageContaining("AI 서버 전송 중 오류가 발생했습니다.");
	}
}
