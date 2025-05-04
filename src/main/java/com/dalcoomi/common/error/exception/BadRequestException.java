package com.dalcoomi.common.error.exception;

import com.dalcoomi.common.error.model.ErrorMessage;

public class BadRequestException extends DalcoomiException {

	public BadRequestException(ErrorMessage errorMessage) {
		super(errorMessage);
	}

	public BadRequestException(ErrorMessage errorMessage, Throwable cause) {
		super(errorMessage, cause);
	}
}
