package com.dalcoomi.common.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {

	private static final int BUFFER_SIZE = 20 * 1024 * 1024; // 20MB
	private static final int CONNECT_TIMEOUT = 10000; // 10초
	private static final int READ_TIMEOUT = 30; // 30초
	private static final int WRITE_TIMEOUT = 30; // 30초

	@Bean
	public WebClient webClient() {
		// HTTP 클라이언트 설정 (타임아웃)
		HttpClient httpClient = HttpClient.create()
			.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT)
			.responseTimeout(Duration.ofSeconds(30))
			.doOnConnected(conn -> conn
				.addHandlerLast(new ReadTimeoutHandler(READ_TIMEOUT))
				.addHandlerLast(new WriteTimeoutHandler(WRITE_TIMEOUT)));

		// 메모리 버퍼 크기 설정
		ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
			.codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(BUFFER_SIZE))
			.build();

		return WebClient.builder()
			.clientConnector(new ReactorClientHttpConnector(httpClient))
			.exchangeStrategies(exchangeStrategies)
			.build();
	}
}
