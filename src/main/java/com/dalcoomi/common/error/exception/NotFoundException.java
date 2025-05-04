package com.dalcoomi.common.error.exception;

import com.dalcoomi.common.error.model.ErrorMessage;

public class NotFoundException extends DalcoomiException {

	public NotFoundException(ErrorMessage errorMessage) {
		super(errorMessage);
	}

	public NotFoundException(ErrorMessage errorMessage, Throwable cause) {
		super(errorMessage, cause);
	}
}
