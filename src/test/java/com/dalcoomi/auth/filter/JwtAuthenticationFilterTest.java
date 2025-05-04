package com.dalcoomi.auth.filter;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.dalcoomi.auth.application.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

	@Mock
	private JwtService jwtService;

	@Mock
	private ObjectMapper objectMapper;

	@Mock
	private HttpServletRequest request;

	@Mock
	private HttpServletResponse response;

	@Mock
	private FilterChain filterChain;

	private JwtAuthenticationFilter jwtAuthenticationFilter;

	@BeforeEach
	void setUp() {
		List<String> allowedUris = List.of("/api/auth/**");

		jwtAuthenticationFilter = new JwtAuthenticationFilter(
			allowedUris,
			jwtService,
			objectMapper
		);
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("허용된 URI 토큰 인증 스킵 성공")
	void allowed_uri_skip_token_authentication_success() throws ServletException, IOException {
		// given
		given(request.getRequestURI()).willReturn("/api/auth/login");
		given(request.getQueryString()).willReturn(null);

		// when
		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		// then
		verify(jwtService, never()).authenticate(any());
		verify(filterChain).doFilter(request, response);
	}

	@Test
	@DisplayName("유효한 토큰으로 인증 성공")
	void authenticate_with_valid_token_success() throws ServletException, IOException {
		// given
		String memberId = "123";
		String accessToken = "test.access.token";
		String authHeader = "Bearer " + accessToken;

		given(request.getRequestURI()).willReturn("/api/member/me");
		given(request.getHeader("Authorization")).willReturn(authHeader);
		given(jwtService.authenticate(authHeader)).willReturn(memberId);

		// when
		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		// then
		verify(filterChain).doFilter(request, response);
		verify(jwtService).authenticate(authHeader);

		// SecurityContext 검증
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		assertThat(auth).isNotNull();
		assertThat(auth.getName()).isEqualTo(memberId);
	}

	@Test
	@DisplayName("필터 체인 진행 중 ServletException 예외 발생")
	void handle_servlet_exception_error() throws ServletException, IOException {
		// given
		String memberId = "123";
		String accessToken = "test.access.token";
		String authHeader = "Bearer " + accessToken;

		given(request.getRequestURI()).willReturn("/api/member/me");
		given(request.getHeader("Authorization")).willReturn(authHeader);
		given(jwtService.authenticate(authHeader)).willReturn(memberId);
		willThrow(new ServletException("필터 체인 에러")).given(filterChain).doFilter(any(), any());

		// when & then
		assertThatThrownBy(() ->
			jwtAuthenticationFilter.doFilterInternal(request, response, filterChain))
			.isInstanceOf(ServletException.class)
			.hasMessage("필터 체인 에러");
	}
}
