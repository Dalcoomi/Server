package com.dalcoomi.common.util.lock;

import static com.dalcoomi.common.error.model.ErrorMessage.LOCK_EXIST_ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.TestPropertySource;

import com.dalcoomi.AbstractContainerBaseTest;
import com.dalcoomi.common.error.exception.ConcurrentRequestException;

@SpringBootTest
@TestPropertySource("classpath:application-test.properties")
class RedisLockUtilTest extends AbstractContainerBaseTest {

	@Autowired
	private RedisLockUtil redisLockUtil;

	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	@BeforeEach
	void setUp() {
		// 테스트 전 Redis 키 정리
		redisTemplate.delete("test:lock:key");
	}

	@AfterEach
	void tearDown() {
		// 테스트 후 Redis 키 정리
		redisTemplate.delete("test:lock:key");
	}

	@Test
	@DisplayName("락 획득 성공 후 작업 실행 및 락 해제")
	void acquire_lock_and_release_success() {
		// given
		String lockKey = "test:lock:key";
		String expectedResult = "test-result";

		// when
		String result = redisLockUtil.acquireAndRunLock(lockKey, () -> expectedResult);

		// then
		assertThat(result).isEqualTo(expectedResult);

		Boolean hasKey = redisTemplate.hasKey(lockKey);
		assertThat(hasKey).isFalse();
	}

	@Test
	@DisplayName("동일한 키로 동시 요청 시 하나만 성공")
	void concurrent_lock_acquisition_only_one_success() throws InterruptedException {
		// given
		String lockKey = "test:lock:key";
		int threadCount = 3;
		ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
		CyclicBarrier barrier = new CyclicBarrier(threadCount);
		AtomicInteger successCount = new AtomicInteger(0);
		AtomicInteger failureCount = new AtomicInteger(0);

		// when
		for (int i = 0; i < threadCount; i++) {
			executorService.submit(() -> {
				try {
					barrier.await(10, TimeUnit.SECONDS);

					redisLockUtil.acquireAndRunLock(lockKey, () -> "success");

					successCount.incrementAndGet();
				} catch (ConcurrentRequestException e) {
					failureCount.incrementAndGet();
				} catch (Exception e) {
					Thread.currentThread().interrupt();
				}
			});
		}

		executorService.shutdown();
		executorService.awaitTermination(10, TimeUnit.SECONDS);

		// then
		assertThat(successCount.get()).isEqualTo(1);
		assertThat(failureCount.get()).isEqualTo(threadCount - 1);
	}

	@Test
	@DisplayName("이미 락이 존재할 때 예외 발생")
	void lock_already_exists_throws_exception_fail() {
		// given
		String lockKey = "test:lock:key";
		redisTemplate.opsForValue().set(lockKey, "locked");

		// when & then
		assertThatThrownBy(() -> redisLockUtil.acquireAndRunLock(lockKey, () -> "result"))
			.isInstanceOf(ConcurrentRequestException.class)
			.hasMessageContaining(LOCK_EXIST_ERROR.getMessage());
	}

	@Test
	@DisplayName("작업 중 예외 발생해도 락 해제 성공")
	void lock_released_even_on_exception_success() {
		// given
		String lockKey = "test:lock:key";

		// when
		try {
			redisLockUtil.acquireAndRunLock(lockKey, () -> {
				throw new RuntimeException("작업 실패");
			});
		} catch (RuntimeException e) {
			// 예외 무시
		}

		// then
		Boolean hasKey = redisTemplate.hasKey(lockKey);
		assertThat(hasKey).isFalse();
	}

	@Test
	@DisplayName("순차적으로 여러 번 락 획득 성공")
	void sequential_lock_acquisition_success() {
		// given
		String lockKey = "test:lock:key";
		List<String> results = new ArrayList<>();

		// when
		for (int i = 0; i < 3; i++) {
			final int index = i;

			String result = redisLockUtil.acquireAndRunLock(lockKey, () -> "result-" + index);

			results.add(result);
		}

		// then
		assertThat(results).containsExactly("result-0", "result-1", "result-2");
	}

	@Test
	@DisplayName("락 획득 중 예외 발생 시 false 반환하고 ConcurrentRequestException 발생")
	void lock_acquisition_exception_throws_concurrent_exception_fail() {
		// given
		LockManager mockLockManager = mock(LockManager.class);
		RedisLockUtil redisLockUtilWithMock = new RedisLockUtil(mockLockManager);

		given(mockLockManager.lock(anyString())).willThrow(new RuntimeException("Redis connection error"));

		// when & then
		assertThatThrownBy(() -> redisLockUtilWithMock.acquireAndRunLock("test:key", () -> "result"))
			.isInstanceOf(ConcurrentRequestException.class)
			.hasMessageContaining(LOCK_EXIST_ERROR.getMessage());
	}

	@Test
	@DisplayName("락 해제 실패해도 작업 성공")
	void work_completes_even_when_unlock_fails_success() {
		// given
		LockManager mockLockManager = mock(LockManager.class);
		RedisLockUtil redisLockUtilWithMock = new RedisLockUtil(mockLockManager);

		given(mockLockManager.lock(anyString())).willReturn(true);
		given(mockLockManager.unlock(anyString())).willThrow(new RuntimeException("Unlock failed"));

		// when
		String result = redisLockUtilWithMock.acquireAndRunLock("test:key", () -> "completed");

		// then
		assertThat(result).isEqualTo("completed");
	}

	@Test
	@DisplayName("락 해제가 false 반환해도 작업 성공")
	void work_completes_even_when_unlock_returns_false_success() {
		// given
		LockManager mockLockManager = mock(LockManager.class);
		RedisLockUtil redisLockUtilWithMock = new RedisLockUtil(mockLockManager);

		given(mockLockManager.lock(anyString())).willReturn(true);
		given(mockLockManager.unlock(anyString())).willReturn(false); // 해제 실패

		// when
		String result = redisLockUtilWithMock.acquireAndRunLock("test:key", () -> "completed");

		// then
		assertThat(result).isEqualTo("completed");
	}
}
