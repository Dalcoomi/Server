package com.dalcoomi.common.util.lock;

import static com.dalcoomi.common.error.model.ErrorMessage.LOCK_EXIST_ERROR;

import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import com.dalcoomi.common.error.exception.ConcurrentRequestException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisLockUtil {

	private final LockManager lockManager;

	public <T> T acquireAndRunLock(String key, Supplier<T> block) {
		boolean acquired = acquireLock(key);

		if (acquired) {
			return proceedWithLock(key, block);
		} else {
			throw new ConcurrentRequestException(LOCK_EXIST_ERROR);
		}
	}

	private Boolean acquireLock(String key) {
		try {
			return lockManager.lock(key);
		} catch (Exception e) {
			log.error("[RedisLock] 동일한 요청이 처리 중입니다. key: {}", key, e);

			return false;
		}
	}

	private <T> T proceedWithLock(String key, Supplier<T> block) {
		try {
			return block.get();
		} finally {
			boolean released = releaseLock(key);

			if (!released) {
				log.error("[RedisLock] 락 해제 실패. TTL 자동 해제 대기 중. key: {}", key);
			}
		}
	}

	private Boolean releaseLock(String key) {
		try {
			return lockManager.unlock(key);
		} catch (Exception e) {
			log.error("[RedisLock] 락 해제에 실패했습니다. key: {}", key, e);

			return false;
		}
	}
}
