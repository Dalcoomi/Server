package com.dalcoomi.transaction.domain;

import static com.dalcoomi.common.error.model.ErrorMessage.UNSUPPORTED_TRANSACTION_TYPE;

import com.dalcoomi.common.error.exception.DalcoomiException;
import com.fasterxml.jackson.annotation.JsonCreator;

public enum TransactionType {

	INCOME,
	EXPENSE;

	@JsonCreator
	public static TransactionType of(String value) {
		for (TransactionType type : TransactionType.values()) {
			if (type.name().equalsIgnoreCase(value)) { // 대소문자 구분 없이 처리
				return type;
			}
		}
		throw new DalcoomiException(UNSUPPORTED_TRANSACTION_TYPE);
	}
}
