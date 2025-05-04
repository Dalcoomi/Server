package com.dalcoomi.common.error.exception;

import com.dalcoomi.common.error.model.ErrorMessage;

public class UnauthorizedException extends DalcoomiException {

	public UnauthorizedException(ErrorMessage errorMessage) {
		super(errorMessage);
	}

	public UnauthorizedException(ErrorMessage errorMessage, Throwable cause) {
		super(errorMessage, cause);
	}
}
