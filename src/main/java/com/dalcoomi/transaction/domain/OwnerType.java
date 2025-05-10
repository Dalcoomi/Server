package com.dalcoomi.transaction.domain;

import static com.dalcoomi.common.error.model.ErrorMessage.UNSUPPORTED_OWNER_TYPE;

import com.dalcoomi.common.error.exception.DalcoomiException;
import com.fasterxml.jackson.annotation.JsonCreator;

public enum OwnerType {

	ADMIN,
	MEMBER,
	GROUP;

	@JsonCreator
	public static OwnerType of(String value) {
		for (OwnerType type : OwnerType.values()) {
			if (type.name().equalsIgnoreCase(value)) { // 대소문자 구분 없이 처리
				return type;
			}
		}
		throw new DalcoomiException(UNSUPPORTED_OWNER_TYPE);
	}
}
