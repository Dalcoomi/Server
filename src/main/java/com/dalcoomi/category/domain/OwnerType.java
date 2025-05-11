package com.dalcoomi.category.domain;

public enum OwnerType {

	ADMIN,
	MEMBER

	// @JsonCreator
	// public static OwnerType of(String value) {
	// 	for (OwnerType type : OwnerType.values()) {
	// 		if (type.name().equalsIgnoreCase(value)) { // 대소문자 구분 없이 처리
	// 			return type;
	// 		}
	// 	}
	// 	throw new DalcoomiException(UNSUPPORTED_OWNER_TYPE);
	// }
}
