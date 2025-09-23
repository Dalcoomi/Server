package com.dalcoomi.common.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@EnableAsync
@Configuration
public class AsyncConfig {

	@Bean("taskExecutor")
	public Executor taskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(2);        // 기본 스레드 수
		executor.setMaxPoolSize(10);        // 최대 스레드 수
		executor.setQueueCapacity(100);     // 큐 크기
		executor.setThreadNamePrefix("async-transaction-");
		executor.initialize();

		return executor;
	}
}
