package com.dalcoomi.auth.application;

import static com.dalcoomi.auth.constant.TokenConstants.ACCESS_TOKEN_TYPE;
import static com.dalcoomi.auth.constant.TokenConstants.MEMBER_ROLE;
import static com.dalcoomi.auth.constant.TokenConstants.REFRESH_TOKEN_REDIS_KEY_SUFFIX;
import static com.dalcoomi.auth.constant.TokenConstants.REFRESH_TOKEN_TYPE;
import static com.dalcoomi.auth.domain.DeviceType.MOBILE;
import static com.dalcoomi.auth.domain.DeviceType.WEB;
import static com.dalcoomi.common.error.model.ErrorMessage.MALFORMED_TOKEN;
import static com.dalcoomi.common.error.model.ErrorMessage.TOKEN_HAS_EXPIRED;
import static com.dalcoomi.common.error.model.ErrorMessage.TOKEN_NOT_FOUND;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
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

import com.dalcoomi.auth.domain.DeviceType;
import com.dalcoomi.auth.dto.RefreshTokenInfo;
import com.dalcoomi.auth.dto.TokenInfo;
import com.dalcoomi.common.error.exception.NotFoundException;
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

	@AfterEach
	void tearDown() {
		assertThat(redisTemplate.getConnectionFactory()).isNotNull();
		requireNonNull(redisTemplate.getConnectionFactory()).getConnection().serverCommands().flushAll();
	}

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
	void malformed_token_throws_unauthorized_exception_fail() {
		// given
		String authHeader = "Bearer invalid.token.format";

		// when & then
		assertThatThrownBy(() -> jwtService.authenticate(authHeader))
			.isInstanceOf(UnauthorizedException.class)
			.hasMessage(MALFORMED_TOKEN.getMessage());
	}

	@Test
	@DisplayName("통합 테스트 - 만료된 토큰으로 인증 시 예외 발생")
	void expired_token_throws_unauthorized_exception_fail() {
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

	@Test
	@DisplayName("통합 테스트 - 디바이스 정보와 함께 토큰 생성 및 저장 성공")
	void create_and_save_token_with_device_type_success() {
		// given
		Long memberId = 100L;
		DeviceType deviceType = WEB;

		// when
		TokenInfo tokenInfo = jwtService.createAndSaveToken(memberId, MEMBER_ROLE, deviceType);

		// then
		assertThat(tokenInfo.accessToken()).isNotNull();
		assertThat(tokenInfo.refreshToken()).isNotNull();

		String key = memberId + REFRESH_TOKEN_REDIS_KEY_SUFFIX + ":" + deviceType.name();
		String tokenInfoJson = redisTemplate.opsForValue().get(key);

		assertThat(tokenInfoJson).isNotNull();

		RefreshTokenInfo savedTokenInfo = RefreshTokenInfo.fromJson(tokenInfoJson);

		assertThat(savedTokenInfo.getToken()).isEqualTo(tokenInfo.refreshToken());
		assertThat(savedTokenInfo.getDeviceType()).isEqualTo(deviceType);
	}

	@Test
	@DisplayName("통합 테스트 - 다중 디바이스 로그인 시 여러 토큰 저장 성공")
	void create_and_save_multiple_tokens_for_different_devices_success() {
		// given
		Long memberId = 101L;

		// when
		TokenInfo webTokenInfo = jwtService.createAndSaveToken(memberId, MEMBER_ROLE, WEB);
		TokenInfo mobileTokenInfo = jwtService.createAndSaveToken(memberId, MEMBER_ROLE, MOBILE);

		// then
		String webKey = memberId + REFRESH_TOKEN_REDIS_KEY_SUFFIX + ":" + WEB.name();
		String mobileKey = memberId + REFRESH_TOKEN_REDIS_KEY_SUFFIX + ":" + MOBILE.name();

		String webTokenInfoJson = redisTemplate.opsForValue().get(webKey);
		String mobileTokenInfoJson = redisTemplate.opsForValue().get(mobileKey);

		assertThat(webTokenInfoJson).isNotNull();
		assertThat(mobileTokenInfoJson).isNotNull();

		RefreshTokenInfo savedWebTokenInfo = RefreshTokenInfo.fromJson(webTokenInfoJson);
		RefreshTokenInfo savedMobileTokenInfo = RefreshTokenInfo.fromJson(mobileTokenInfoJson);

		assertThat(savedWebTokenInfo.getToken()).isEqualTo(webTokenInfo.refreshToken());
		assertThat(savedWebTokenInfo.getDeviceType()).isEqualTo(WEB);

		assertThat(savedMobileTokenInfo.getToken()).isEqualTo(mobileTokenInfo.refreshToken());
		assertThat(savedMobileTokenInfo.getDeviceType()).isEqualTo(MOBILE);
	}

	@Test
	@DisplayName("통합 테스트 - Refresh Token 검증 성공")
	void validate_refresh_token_success() {
		// given
		Long memberId = 102L;
		TokenInfo tokenInfo = jwtService.createAndSaveToken(memberId, MEMBER_ROLE, WEB);

		// when
		Long validatedMemberId = jwtService.validateRefreshToken(tokenInfo.refreshToken());

		// then
		assertThat(validatedMemberId).isEqualTo(memberId);
	}

	@Test
	@DisplayName("통합 테스트 - Redis에 없는 Refresh Token 검증 시 예외 발생")
	void validate_refresh_token_not_in_redis_throws_exception_fail() {
		// given
		Long memberId = 103L;
		String refreshToken = jwtService.createToken(memberId, refreshTokenDuration, REFRESH_TOKEN_TYPE, MEMBER_ROLE);

		// when & then
		assertThatThrownBy(() -> jwtService.validateRefreshToken(refreshToken))
			.isInstanceOf(UnauthorizedException.class)
			.hasMessage(TOKEN_NOT_FOUND.getMessage());
	}

	@Test
	@DisplayName("통합 테스트 - Refresh Token 삭제 성공")
	void delete_refresh_token_success() {
		// given
		Long memberId = 104L;
		TokenInfo tokenInfo = jwtService.createAndSaveToken(memberId, MEMBER_ROLE, WEB);

		// when
		jwtService.deleteRefreshToken(memberId, tokenInfo.refreshToken());

		// then
		String key = memberId + REFRESH_TOKEN_REDIS_KEY_SUFFIX + ":" + WEB.name();
		String tokenInfoJson = redisTemplate.opsForValue().get(key);

		assertThat(tokenInfoJson).isNull();
	}

	@Test
	@DisplayName("통합 테스트 - 존재하지 않는 Refresh Token 삭제 시 예외 발생")
	void delete_non_existent_refresh_token_throws_exception_fail() {
		// given
		Long memberId = 105L;
		String fakeRefreshToken = "fake.refresh.token";

		// when & then
		assertThatThrownBy(() -> jwtService.deleteRefreshToken(memberId, fakeRefreshToken))
			.isInstanceOf(NotFoundException.class)
			.hasMessage(TOKEN_NOT_FOUND.getMessage());
	}

	@Test
	@DisplayName("통합 테스트 - 다중 디바이스 중 하나의 토큰만 삭제 성공")
	void delete_one_refresh_token_among_multiple_devices_success() {
		// given
		Long memberId = 106L;
		TokenInfo webTokenInfo = jwtService.createAndSaveToken(memberId, MEMBER_ROLE, WEB);
		TokenInfo mobileTokenInfo = jwtService.createAndSaveToken(memberId, MEMBER_ROLE, MOBILE);

		// when
		jwtService.deleteRefreshToken(memberId, webTokenInfo.refreshToken());

		// then
		String webKey = memberId + REFRESH_TOKEN_REDIS_KEY_SUFFIX + ":" + WEB.name();
		String mobileKey = memberId + REFRESH_TOKEN_REDIS_KEY_SUFFIX + ":" + MOBILE.name();

		String webTokenInfoJson = redisTemplate.opsForValue().get(webKey);
		String mobileTokenInfoJson = redisTemplate.opsForValue().get(mobileKey);

		assertThat(webTokenInfoJson).isNull();
		assertThat(mobileTokenInfoJson).isNotNull();

		RefreshTokenInfo remainingTokenInfo = RefreshTokenInfo.fromJson(mobileTokenInfoJson);

		assertThat(remainingTokenInfo.getToken()).isEqualTo(mobileTokenInfo.refreshToken());
		assertThat(remainingTokenInfo.getDeviceType()).isEqualTo(MOBILE);
	}
}
