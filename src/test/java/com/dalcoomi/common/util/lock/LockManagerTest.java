package com.dalcoomi.common.util.lock;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import java.time.Duration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class LockManagerTest {

	@Mock
	private RedisTemplate<String, String> redisTemplate;

	@Mock
	private ValueOperations<String, String> valueOperations;

	@InjectMocks
	private LockManager lockManager;

	@Test
	@DisplayName("lock 메서드에서 Redis 예외 발생 시 false 반환 성공")
	void lock_when_redis_exception_should_return_false_success() {
		// given
		given(redisTemplate.opsForValue()).willReturn(valueOperations);
		given(valueOperations.setIfAbsent(anyString(), eq("lock"), any(Duration.class))).willThrow(
			new RuntimeException("Redis failed"));

		// when
		Boolean result = lockManager.lock("test:key");

		// then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("unlock 메서드에서 Redis 예외 발생 시 false 반환 성공")
	void unlock_when_redis_exception_should_return_false_success() {
		// given
		String lockKey = "test:unlock:key";
		given(redisTemplate.delete(lockKey)).willThrow(new RuntimeException("Redis delete failed"));

		// when
		Boolean result = lockManager.unlock(lockKey);

		// then
		assertThat(result).isFalse();
	}
}
