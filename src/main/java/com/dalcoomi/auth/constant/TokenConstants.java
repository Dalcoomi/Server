package com.dalcoomi.auth.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TokenConstants {

	public static final String BEARER_PREFIX = "Bearer ";
	public static final String ACCESS_TOKEN_TYPE = "ACCESS";
	public static final String REFRESH_TOKEN_TYPE = "REFRESH";
	public static final String REFRESH_TOKEN_REDIS_KEY_SUFFIX = ":refresh";
	public static final String ADMIN_ROLE = "ADMIN";
	public static final String MEMBER_ROLE = "MEMBER";
	public static final String TEST_ROLE = "TEST";
}
