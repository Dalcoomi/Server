package com.dalcoomi.common.error.exception;

import com.dalcoomi.common.error.model.ErrorMessage;

public class ConflictException extends DalcoomiException {

	public ConflictException(ErrorMessage errorMessage) {
		super(errorMessage);
	}

	public ConflictException(ErrorMessage errorMessage, Throwable cause) {
		super(errorMessage, cause);
	}
}
