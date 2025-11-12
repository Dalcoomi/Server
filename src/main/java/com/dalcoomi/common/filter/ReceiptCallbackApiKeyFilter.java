package com.dalcoomi.common.filter;

import static jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ReceiptCallbackApiKeyFilter extends OncePerRequestFilter {

	private static final String API_KEY_HEADER = "X-API-Key";

	@Value("${receipt.callback-path}")
	private String receiptCallbackPath;

	@Value("${api-key}")
	private String expectedApiKey;

	@Override
	protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response,
		@NonNull FilterChain filterChain) throws ServletException, IOException {
		String requestPath = request.getRequestURI();

		if (!receiptCallbackPath.equals(requestPath)) {
			filterChain.doFilter(request, response);
			return;
		}

		String apiKey = request.getHeader(API_KEY_HEADER);

		if (apiKey == null || apiKey.isBlank()) {
			log.warn("AI 콜백 요청에 API 키가 없음: path={}", requestPath);
			response.setStatus(SC_UNAUTHORIZED);
			response.getWriter().write("API Key 헤더는 필수입니다.");
			return;
		}

		if (!expectedApiKey.equals(apiKey)) {
			log.warn("AI 콜백 요청의 API 키가 일치하지 않음: path={}", requestPath);
			response.setStatus(SC_FORBIDDEN);
			response.getWriter().write("유효하지 않는 API Key 입니다.");
			return;
		}

		filterChain.doFilter(request, response);
	}
}
