package com.dalcoomi.auth.filter;

import static jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.dalcoomi.auth.application.JwtService;
import com.dalcoomi.common.error.exception.UnauthorizedException;
import com.dalcoomi.common.error.model.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String AUTHORIZATION_HEADER = "Authorization";
	private static final GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();
	private static final AntPathMatcher pathMatcher = new AntPathMatcher();

	private final List<String> allowedUris;
	private final JwtService jwtService;
	private final ObjectMapper objectMapper;

	@Override
	protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response,
		@NonNull FilterChain filterChain) throws ServletException, IOException {
		String requestUri = request.getRequestURI();
		String queryString = request.getQueryString();

		log.info("들어온 요청 - URI: {}, Query: {}, Method: {}", requestUri, queryString != null ? queryString : "쿼리 스트링 없음",
			request.getMethod());

		if (isAllowedUri(requestUri)) {
			filterChain.doFilter(request, response);
			return;
		}

		try {
			String memberId = jwtService.authenticate(request.getHeader(AUTHORIZATION_HEADER));

			saveAuthentication(memberId);
		} catch (UnauthorizedException e) {
			response.setContentType("application/json;charset=UTF-8");
			response.setStatus(SC_UNAUTHORIZED);
			response.getWriter().write(objectMapper.writeValueAsString(new ErrorResponse(e.getMessage())));
			return;
		}

		filterChain.doFilter(request, response);
	}

	private boolean isAllowedUri(String requestUri) {
		boolean allowed = false;

		for (String pattern : allowedUris) {
			if (pathMatcher.match(pattern, requestUri)) {
				allowed = true;
				break;
			}
		}

		return allowed;
	}

	private void saveAuthentication(String memberId) {
		CustomUserDetails userDetails = new CustomUserDetails(Long.valueOf(memberId), memberId,
			authoritiesMapper.mapAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))));

		Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
			userDetails.getAuthorities());

		SecurityContextHolder.getContext().setAuthentication(authentication);

		log.info("시큐리티 컨텍스트에 인증 정보 저장 - memberId: {}", memberId);
	}
}
