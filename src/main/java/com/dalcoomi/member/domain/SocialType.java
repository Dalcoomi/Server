package com.dalcoomi.member.domain;

import static com.dalcoomi.common.error.model.ErrorMessage.UNSUPPORTED_SOCIAL_TYPE;

import com.dalcoomi.common.error.exception.DalcoomiException;
import com.fasterxml.jackson.annotation.JsonCreator;

public enum SocialType {

	NAVER,
	KAKAO;

	@JsonCreator
	public static SocialType from(String value) {
		for (SocialType type : SocialType.values()) {
			if (type.name().equalsIgnoreCase(value)) { // 대소문자 구분 없이 처리
				return type;
			}
		}
		throw new DalcoomiException(UNSUPPORTED_SOCIAL_TYPE);
	}
}
