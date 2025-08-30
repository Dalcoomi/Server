package com.dalcoomi.auth.application;

import static com.dalcoomi.auth.constant.TokenConstants.ACCESS_TOKEN_TYPE;
import static com.dalcoomi.auth.constant.TokenConstants.MEMBER_ROLE;
import static com.dalcoomi.common.error.model.ErrorMessage.MALFORMED_TOKEN;
import static com.dalcoomi.common.error.model.ErrorMessage.TOKEN_HAS_EXPIRED;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import com.dalcoomi.common.error.exception.UnauthorizedException;

@Transactional
@SpringBootTest
@TestPropertySource("classpath:application-test.properties")
@AutoConfigureMockMvc(addFilters = false)
class JwtServiceTest {

	@Autowired
	private JwtService jwtService;

	@Autowired
	private StringRedisTemplate redisTemplate;

	@Value("${jwt.issuer}")
	private String issuer;

	@Value("${jwt.secret-key}")
	private String tokenSecret;

	@Value("${jwt.access.duration}")
	private long accessTokenDuration;

	@Value("${jwt.refresh.duration}")
	private long refreshTokenDuration;

	@Test
	@DisplayName("통합 테스트 - 실제 토큰 생성 및 인증 성공")
	void create_and_authenticate_real_token_success() {
		// given
		Long memberId = 123L;

		// when
		String token = jwtService.createToken(memberId, accessTokenDuration, ACCESS_TOKEN_TYPE, MEMBER_ROLE);
		String authHeader = "Bearer " + token;
		String extractedMemberId = jwtService.authenticate(authHeader);

		// then
		assertThat(extractedMemberId).isEqualTo(String.valueOf(memberId));
	}

	@Test
	@DisplayName("통합 테스트 - 잘못된 형식의 토큰으로 인증 시 예외 발생")
	void malformed_token_throws_unauthorized_exception() {
		// given
		String authHeader = "Bearer invalid.token.format";

		// when & then
		assertThatThrownBy(() -> jwtService.authenticate(authHeader))
			.isInstanceOf(UnauthorizedException.class)
			.hasMessage(MALFORMED_TOKEN.getMessage());
	}

	@Test
	@DisplayName("통합 테스트 - 만료된 토큰으로 인증 시 예외 발생")
	void expired_token_throws_unauthorized_exception() {
		// given
		JwtService expiredTokenService = new JwtService(redisTemplate);
		ReflectionTestUtils.setField(expiredTokenService, "tokenSecret", tokenSecret);
		ReflectionTestUtils.setField(expiredTokenService, "accessTokenDuration", -1000L); // 음수로 만료
		ReflectionTestUtils.setField(expiredTokenService, "issuer", issuer);

		String expiredToken = "Bearer "
			+ expiredTokenService.createToken(123L, -1000L, ACCESS_TOKEN_TYPE, MEMBER_ROLE);

		// when & then
		Assertions.assertThatThrownBy(() -> jwtService.authenticate(expiredToken))
			.isInstanceOf(UnauthorizedException.class)
			.hasMessage(TOKEN_HAS_EXPIRED.getMessage());
	}
}
