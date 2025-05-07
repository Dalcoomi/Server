package com.dalcoomi.common.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TokenConstant {

	public static final String BEARER_PREFIX = "Bearer ";
	public static final String ACCESS_TOKEN_TYPE = "access";
	public static final String REFRESH_TOKEN_TYPE = "refresh";
	public static final String REFRESH_TOKEN_REDIS_KEY_SUFFIX = ":refresh";
}
