package com.dalcoomi.auth.application;

import static com.dalcoomi.common.error.model.ErrorMessage.AUTHORIZATION_HEADER_ERROR;
import static com.dalcoomi.common.error.model.ErrorMessage.MALFORMED_TOKEN;
import static com.dalcoomi.common.error.model.ErrorMessage.TOKEN_HAS_EXPIRED;
import static java.util.Objects.isNull;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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

	private static final String BEARER_PREFIX = "Bearer ";
	private static final String ACCESS_TOKEN_TYPE = "access";
	private static final String REFRESH_TOKEN_TYPE = "refresh";

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
			Claims claims = validateToken(token);
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

	private Claims validateToken(String token) {
		return Jwts.parser()
			.verifyWith(getSignKey())
			.build()
			.parseSignedClaims(token)
			.getPayload();
	}

	private SecretKey getSignKey() {
		byte[] keyBytes = Decoders.BASE64.decode(tokenSecret);

		return Keys.hmacShaKeyFor(keyBytes);
	}

	public String createAccessToken(Long memberId) {
		Date now = new Date();
		Date duration = new Date(now.getTime() + accessTokenDuration);

		return Jwts.builder()
			.issuer(issuer)
			.subject(String.valueOf(memberId))
			.issuedAt(now)
			.expiration(duration)
			.claim("type", ACCESS_TOKEN_TYPE)
			.signWith(getSignKey())
			.compact();
	}
}
