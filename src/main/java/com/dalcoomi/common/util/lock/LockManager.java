package com.dalcoomi.common.util.lock;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LockManager {

	private final RedisTemplate<String, String> redisTemplate;

	public Boolean lock(String key) {
		try {
			Boolean result = redisTemplate.opsForValue().setIfAbsent(key, "lock", Duration.ofSeconds(10));

			return Boolean.TRUE.equals(result);
		} catch (Exception e) {
			// Redis 연결 실패 등의 예외 상황
			return false;
		}
	}

	public Boolean unlock(String key) {
		try {
			return redisTemplate.delete(key);
		} catch (Exception e) {
			// Redis 연결 실패 등의 예외 상황
			return false;
		}
	}
}

