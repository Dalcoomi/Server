package com.dalcoomi.common.error.exception;

import com.dalcoomi.common.error.model.ErrorMessage;

public class DalcoomiException extends RuntimeException {

	public DalcoomiException(ErrorMessage errorMessage) {
		super(errorMessage.getMessage());
	}

	public DalcoomiException(ErrorMessage errorMessage, Throwable cause) {
		super(errorMessage.getMessage(), cause);
	}

	public DalcoomiException(String message) {
		super(message);
	}

	public DalcoomiException(String message, Throwable cause) {
		super(message, cause);
	}
}
