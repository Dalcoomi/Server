package com.dalcoomi.transaction.domain.event;

import static com.dalcoomi.transaction.domain.TransactionType.EXPENSE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;
import static org.mockito.BDDMockito.willThrow;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dalcoomi.category.domain.Category;
import com.dalcoomi.common.error.exception.DalcoomiException;
import com.dalcoomi.fixture.CategoryFixture;
import com.dalcoomi.fixture.MemberFixture;
import com.dalcoomi.member.domain.Member;
import com.dalcoomi.transaction.application.TransactionService;
import com.dalcoomi.transaction.domain.Transaction;

@ExtendWith(MockitoExtension.class)
class TransactionEventHandlerTest {

	@InjectMocks
	private TransactionEventHandler eventHandler;

	@Mock
	private TransactionService transactionService;

	@Test
	@DisplayName("거래 생성 이벤트 처리 성공")
	void handle_transaction_created_success() {
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

		TransactionCreatedEvent event = new TransactionCreatedEvent(this, taskId, transactions);

		// when & then
		assertThatNoException().isThrownBy(() -> eventHandler.handleTransactionCreated(event));
	}

	@Test
	@DisplayName("거래 생성 이벤트 처리 실패")
	void handle_transaction_created_fail() {
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

		TransactionCreatedEvent event = new TransactionCreatedEvent(this, taskId, transactions);

		willThrow(new DalcoomiException("AI 서버 오류")).given(transactionService).sendToAiServer(taskId, transactions);

		// when & then
		assertThatNoException().isThrownBy(() -> eventHandler.handleTransactionCreated(event));
	}
}
