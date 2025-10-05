package com.dalcoomi.auth.application;

import static com.dalcoomi.auth.constant.TokenConstants.ACCESS_TOKEN_TYPE;
import static com.dalcoomi.auth.constant.TokenConstants.BEARER_PREFIX;
import static com.dalcoomi.auth.constant.TokenConstants.REFRESH_TOKEN_REDIS_KEY_SUFFIX;
import static com.dalcoomi.auth.constant.TokenConstants.REFRESH_TOKEN_TYPE;
import static com.dalcoomi.auth.constant.TokenConstants.TEST_ROLE;
import static com.dalcoomi.common.error.model.ErrorMessage.AUTHORIZATION_HEADER_ERROR;
import static com.dalcoomi.common.error.model.ErrorMessage.MALFORMED_TOKEN;
import static com.dalcoomi.common.error.model.ErrorMessage.TOKEN_HAS_EXPIRED;
import static com.dalcoomi.common.error.model.ErrorMessage.TOKEN_NOT_FOUND;
import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

import java.time.Duration;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.dalcoomi.auth.dto.TokenInfo;
import com.dalcoomi.common.error.exception.NotFoundException;
import com.dalcoomi.common.error.exception.UnauthorizedException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtService {

	private final StringRedisTemplate redisTemplate;

	@Value("${jwt.issuer}")
	private String issuer;

	@Value("${jwt.secret-key}")
	private String tokenSecret;

	@Value("${jwt.access.duration}")
	private long accessTokenDuration;

	@Value("${jwt.refresh.duration}")
	private long refreshTokenDuration;

	public String authenticate(String header) {
		if (isNull(header) || !header.startsWith(BEARER_PREFIX)) {
			log.error("Authorization Header Error: [{}]", header);

			throw new UnauthorizedException(AUTHORIZATION_HEADER_ERROR);
		}

		String token = header.substring(BEARER_PREFIX.length());

		try {
			Claims claims = Jwts.parser().verifyWith(getSignKey())
				.build()
				.parseSignedClaims(token)
				.getPayload();
			String memberId = claims.getSubject();

			log.info("토큰 인증 성공 - memberId: {}", memberId);

			return memberId;
		} catch (MalformedJwtException e) {
			log.error("잘못된 형식의 토큰: {}", e.getMessage());

			throw new UnauthorizedException(MALFORMED_TOKEN);
		} catch (ExpiredJwtException e) {
			log.error("만료된 토큰: {}", e.getMessage());

			throw new UnauthorizedException(TOKEN_HAS_EXPIRED);
		}
	}

	public TokenInfo createAndSaveToken(Long memberId, String role) {
		String accessToken;

		if (role.equals(TEST_ROLE)) {
			accessToken = createToken(memberId, refreshTokenDuration, ACCESS_TOKEN_TYPE, role);
		} else {
			accessToken = createToken(memberId, accessTokenDuration, ACCESS_TOKEN_TYPE, role);
		}

		String refreshToken = createToken(memberId, refreshTokenDuration, REFRESH_TOKEN_TYPE, role);

		redisTemplate.opsForSet()
			.add(memberId + REFRESH_TOKEN_REDIS_KEY_SUFFIX, refreshToken);
		redisTemplate.expire(memberId + REFRESH_TOKEN_REDIS_KEY_SUFFIX, Duration.ofMillis(refreshTokenDuration));

		return new TokenInfo(accessToken, refreshToken);
	}

	public void deleteRefreshToken(Long memberId, String refreshToken) {
		String key = memberId + REFRESH_TOKEN_REDIS_KEY_SUFFIX;
		Long removed = redisTemplate.opsForSet().remove(key, refreshToken);

		if (removed == null || removed == 0) {
			throw new NotFoundException(TOKEN_NOT_FOUND);
		}
	}

	public void deleteAllRefreshTokens(Long memberId) {
		String key = memberId + REFRESH_TOKEN_REDIS_KEY_SUFFIX;
		boolean deleted = requireNonNull(redisTemplate.delete(key));

		if (!deleted) {
			throw new NotFoundException(TOKEN_NOT_FOUND);
		}
	}

	public Long validateRefreshToken(String refreshToken) {
		try {
			Claims claims = Jwts.parser().verifyWith(getSignKey())
				.build()
				.parseSignedClaims(refreshToken)
				.getPayload();

			String tokenType = claims.get("type", String.class);

			if (!REFRESH_TOKEN_TYPE.equals(tokenType)) {
				log.error("Refresh Token이 아닌 토큰 사용 시도: {}", tokenType);

				throw new UnauthorizedException(MALFORMED_TOKEN);
			}

			Long memberId = Long.valueOf(claims.getSubject());
			String key = memberId + REFRESH_TOKEN_REDIS_KEY_SUFFIX;
			Boolean isMember = redisTemplate.opsForSet().isMember(key, refreshToken);

			if (isMember == null || !isMember) {
				log.error("Redis에 저장된 Refresh Token이 없거나 불일치 - memberId: {}", memberId);

				throw new UnauthorizedException(TOKEN_NOT_FOUND);
			}

			log.info("Refresh Token 검증 성공 - memberId: {}", memberId);

			return memberId;
		} catch (MalformedJwtException e) {
			log.error("잘못된 형식의 Refresh Token: {}", e.getMessage());

			throw new UnauthorizedException(MALFORMED_TOKEN);
		} catch (ExpiredJwtException e) {
			log.error("만료된 Refresh Token: {}", e.getMessage());

			throw new UnauthorizedException(TOKEN_HAS_EXPIRED);
		}
	}

	public String createToken(Long memberId, long duration, String tokenType, String role) {
		Date now = new Date();
		Date expireDate = new Date(now.getTime() + duration);

		return Jwts.builder()
			.issuer(issuer)
			.subject(String.valueOf(memberId))
			.issuedAt(now)
			.expiration(expireDate)
			.claim("type", tokenType)
			.claim("role", role)
			.id(randomUUID().toString())
			.signWith(getSignKey())
			.compact();
	}

	private SecretKey getSignKey() {
		byte[] keyBytes = Decoders.BASE64.decode(tokenSecret);

		return Keys.hmacShaKeyFor(keyBytes);
	}
}
