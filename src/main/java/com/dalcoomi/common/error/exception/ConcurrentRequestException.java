package com.dalcoomi.common.error.exception;

import com.dalcoomi.common.error.model.ErrorMessage;

public class ConcurrentRequestException extends DalcoomiException {

	public ConcurrentRequestException(ErrorMessage errorMessage) {
		super(errorMessage);
	}

	public ConcurrentRequestException(ErrorMessage errorMessage, Throwable cause) {
		super(errorMessage, cause);
	}
}
