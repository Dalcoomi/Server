package com.dalcoomi.batch;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchScheduler {

	private final WithdrawalBatchService withdrawalBatchService;
	private final TransactionBatchService transactionBatchService;
	private final DataMigrationBatchService dataMigrationBatchService;

	/**
	 * 휴면 탈퇴 데이터 정리 배치
	 * 매일 새벽 2시에 실행
	 * - 90일 경과한 개인 거래 내역 처리 (익명화 또는 삭제)
	 * - 90일 경과한 소셜 연결 및 회원 정보 삭제
	 */
	@Scheduled(cron = "0 0 2 * * *")
	public void runWithdrawalCleanupBatch() {
		log.info("===== 휴면 탈퇴 데이터 정리 배치 스케줄러 시작 =====");

		try {
			withdrawalBatchService.cleanupExpiredWithdrawalData();
			log.info("휴면 탈퇴 데이터 정리 배치 스케줄러 완료");
		} catch (Exception e) {
			log.error("휴면 탈퇴 데이터 정리 배치 실행 중 오류 발생", e);
		}

		log.info("=====================================================");
	}

	/**
	 * 익명화된 데이터 삭제 배치
	 * 매월 1일 새벽 3시에 실행
	 * - 5년 경과한 익명화된 개인 거래 내역 완전 삭제 (삭제 요청권 만료)
	 */
	@Scheduled(cron = "0 0 3 1 * *")
	public void runAnonymizedDataDeletionBatch() {
		log.info("===== 익명화된 데이터 삭제 배치 스케줄러 시작 =====");

		try {
			transactionBatchService.deleteExpiredAnonymizedData();
			log.info("익명화된 데이터 삭제 배치 스케줄러 완료");
		} catch (Exception e) {
			log.error("익명화된 데이터 삭제 배치 실행 중 오류 발생", e);
		}

		log.info("================================");
	}

	/**
	 * 평문 데이터 암호화 마이그레이션 배치
	 * 매일 새벽 4시에 실행 (임시 - 마이그레이션 완료 후 제거 예정)
	 * - 평문으로 저장된 개인정보를 암호화로 변환
	 * - 검색용 해시 값 생성
	 */
	@Scheduled(cron = "0 0 4 * * *")
	public void runDataMigrationBatch() {
		log.info("===== 평문 데이터 암호화 마이그레이션 배치 시작 =====");

		try {
			dataMigrationBatchService.migratePlainTextData();
			log.info("평문 데이터 암호화 마이그레이션 배치 완료");
		} catch (Exception e) {
			log.error("평문 데이터 암호화 마이그레이션 배치 실행 중 오류 발생", e);
		}

		log.info("================================================");
	}
}
