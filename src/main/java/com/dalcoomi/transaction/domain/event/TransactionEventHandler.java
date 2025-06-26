package com.dalcoomi.transaction.domain.event;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.dalcoomi.transaction.application.TransactionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionEventHandler {

	private final TransactionService transactionService;

	@EventListener
	@Async("taskExecutor")
	public void handleTransactionCreated(TransactionCreatedEvent event) {
		log.info("거래 생성 이벤트 처리 시작: taskId={}", event.getTaskId());

		try {
			transactionService.sendToAiServer(event.getTaskId(), event.getTransactions());
			log.info("AI 서버 전송 성공: taskId={}", event.getTaskId());

		} catch (Exception e) {
			handleAiServerFailure(event, e);
		}
	}

	private void handleAiServerFailure(TransactionCreatedEvent event, Exception e) {
		// 실패 처리 로직
		log.error("AI 서버 전송 실패: taskId={}, error={}", event.getTaskId(), e.getMessage(), e);
	}
}
