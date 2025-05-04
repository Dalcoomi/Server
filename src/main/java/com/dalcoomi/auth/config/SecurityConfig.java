package com.dalcoomi.auth.config;

import static jakarta.servlet.http.HttpServletResponse.SC_NOT_FOUND;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.dalcoomi.auth.application.JwtService;
import com.dalcoomi.auth.filter.JwtAuthenticationFilter;
import com.dalcoomi.common.error.model.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private static final String[] ALLOWED_URIS = {
		"/",
		"/login/**"
	};

	private final ObjectMapper objectMapper;
	private final JwtService jwtService;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.cors(Customizer.withDefaults())
			.formLogin(AbstractHttpConfigurer::disable)
			.logout(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.csrf(AbstractHttpConfigurer::disable)
			.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
			.sessionManagement(sessionManagement ->
				sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(request ->
				request.requestMatchers(ALLOWED_URIS).permitAll().anyRequest().authenticated())
			// .addFilterAfter(jwtAuthenticationFilter(), LogoutFilter.class)
			.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
			.exceptionHandling(e -> e.authenticationEntryPoint((request, response, authException) -> {
				log.error("(유효하지 않는 URL) 시큐리티 필터 에러: {}", authException.getMessage(), authException);

				response.setStatus(SC_NOT_FOUND);
				response.setContentType("application/json;charset=UTF-8");
				response.getWriter().write(objectMapper.writeValueAsString(new ErrorResponse("유효하지 않는 URL 입니다.")));
			}));

		return http.build();
	}

	@Bean
	public JwtAuthenticationFilter jwtAuthenticationFilter() {
		return new JwtAuthenticationFilter(Arrays.asList(ALLOWED_URIS), jwtService, objectMapper);
	}
}
