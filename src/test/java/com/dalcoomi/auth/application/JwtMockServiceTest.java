package com.dalcoomi.auth.application;

import static com.dalcoomi.common.error.model.ErrorMessage.AUTHORIZATION_HEADER_ERROR;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dalcoomi.common.error.exception.UnauthorizedException;

@ExtendWith(MockitoExtension.class)
class JwtMockServiceTest {

	@Mock
	private JwtService jwtService;

	@Test
	@DisplayName("null 헤더로 인증 시 예외 발생")
	void null_header_throws_unauthorized_exception() {
		// given
		given(jwtService.authenticate(null)).willThrow(new UnauthorizedException(AUTHORIZATION_HEADER_ERROR));

		// when & then
		assertThatThrownBy(() -> jwtService.authenticate(null))
			.isInstanceOf(UnauthorizedException.class)
			.hasMessage(AUTHORIZATION_HEADER_ERROR.getMessage());
	}

	@Test
	@DisplayName("잘못된 형식의 헤더로 인증 시 예외 발생")
	void invalid_header_format_throws_unauthorized_exception() {
		// given
		String header = "Invalid header";

		given(jwtService.authenticate(header)).willThrow(new UnauthorizedException(AUTHORIZATION_HEADER_ERROR));

		// when & then
		assertThatThrownBy(() -> jwtService.authenticate(header))
			.isInstanceOf(UnauthorizedException.class)
			.hasMessage(AUTHORIZATION_HEADER_ERROR.getMessage());
	}
}
