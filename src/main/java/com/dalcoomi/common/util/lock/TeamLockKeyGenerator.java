package com.dalcoomi.common.util.lock;

import org.springframework.stereotype.Component;

@Component
public class TeamLockKeyGenerator {

	public String generateLeaveLockKey(Long teamId) {
		return String.format("team:leave:%d", teamId);
	}
}
