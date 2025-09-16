package com.dalcoomi.batch;

import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BatchSchedulerTest {

	@Mock
	private WithdrawalBatchService withdrawalBatchService;

	@Mock
	private TransactionBatchService transactionBatchService;

	@InjectMocks
	private BatchScheduler batchScheduler;

	@Test
	@DisplayName("휴면 탈퇴 데이터 정리 배치 스케줄러 실행 성공")
	void run_withdrawal_cleanup_batch_calls_withdrawal_batch_service_success() {
		// given
		willDoNothing().given(withdrawalBatchService).cleanupExpiredWithdrawalData();

		// when
		batchScheduler.runWithdrawalCleanupBatch();

		// then
		then(withdrawalBatchService).should().cleanupExpiredWithdrawalData();
	}

	@Test
	@DisplayName("익명화된 데이터 삭제 배치 스케줄러 실행 성공")
	void run_anonymized_data_deletion_batch_calls_transaction_batch_service_success() {
		// given
		willDoNothing().given(transactionBatchService).deleteExpiredAnonymizedData();

		// when
		batchScheduler.runAnonymizedDataDeletionBatch();

		// then
		then(transactionBatchService).should().deleteExpiredAnonymizedData();
	}

	@Test
	@DisplayName("휴면 탈퇴 데이터 정리 배치에서 예외 발생 시 로그만 출력하고 계속 진행 성공")
	void run_withdrawal_cleanup_batch_exception_handling_success() {
		// given
		RuntimeException exception = new RuntimeException("배치 실행 실패");
		willThrow(exception).given(withdrawalBatchService).cleanupExpiredWithdrawalData();

		// when
		batchScheduler.runWithdrawalCleanupBatch();

		// then
		then(withdrawalBatchService).should().cleanupExpiredWithdrawalData();
	}

	@Test
	@DisplayName("익명화 데이터 삭제 배치에서 예외 발생 시 로그만 출력하고 계속 진행 성공")
	void run_anonymized_data_deletion_batch_exception_handling_success() {
		// given
		RuntimeException exception = new RuntimeException("배치 실행 실패");
		willThrow(exception).given(transactionBatchService).deleteExpiredAnonymizedData();

		// when
		batchScheduler.runAnonymizedDataDeletionBatch();

		// then
		then(transactionBatchService).should().deleteExpiredAnonymizedData();
	}
}
