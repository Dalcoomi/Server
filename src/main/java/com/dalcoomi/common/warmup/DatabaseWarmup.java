package com.dalcoomi.common.warmup;

import java.time.LocalDate;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.dalcoomi.transaction.application.TransactionService;
import com.dalcoomi.transaction.dto.TransactionSearchCriteria;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 애플리케이션 시작 시 데이터베이스 워밍업을 수행하는 컴포넌트
 * JPA 초기화, 커넥션 풀 워밍업, 쿼리 플랜 캐싱을 통해 첫 사용자 요청의 응답 시간을 개선
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseWarmup {

	private final TransactionService transactionService;

	/**
	 * 애플리케이션이 완전히 시작된 후 워밍업 수행
	 * ApplicationReadyEvent는 모든 빈 초기화가 완료된 후 발생
	 */
	@EventListener(ApplicationReadyEvent.class)
	@Transactional(readOnly = true)
	public void warmup() {
		log.info("=== Database Warmup 시작 ===");

		long startTime = System.currentTimeMillis();

		try {
			// 1. 거래 내역 조회 워밍업 (가장 자주 사용되는 API)
			warmupTransactionQuery();

			long elapsed = System.currentTimeMillis() - startTime;
			log.info("=== Database Warmup 완료: {}ms ===", elapsed);
		} catch (Exception e) {
			// 워밍업 실패는 애플리케이션 시작을 막지 않음
			log.warn("Database Warmup 실패 (애플리케이션은 정상 동작): {}", e.getMessage());
		}
	}

	/**
	 * 거래 내역 조회 쿼리 워밍업
	 * - JPA EntityManager 초기화
	 * - HikariCP 커넥션 풀 워밍업
	 * - 쿼리 실행 계획 캐싱
	 * - 인덱스 버퍼 풀 로딩
	 */
	private void warmupTransactionQuery() {
		try {
			// 현재 년/월 기준 더미 조회 (실제 데이터가 없어도 무방)
			LocalDate now = LocalDate.now();
			TransactionSearchCriteria criteria = TransactionSearchCriteria.of(
				1L,  // 더미 memberId
				null,
				now.getYear(),
				now.getMonthValue(),
				null,
				null
			);

			transactionService.get(criteria);

			log.debug("거래 내역 조회 쿼리 워밍업 완료");
		} catch (Exception e) {
			log.debug("거래 내역 조회 워밍업 중 예외 발생 (무시 가능): {}", e.getMessage());
		}
	}
}
