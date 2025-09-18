package com.dalcoomi.common.error.exception;

import com.dalcoomi.common.error.model.ErrorMessage;

public class LockedException extends DalcoomiException {

	public LockedException(ErrorMessage errorMessage) {
		super(errorMessage);
	}

	public LockedException(ErrorMessage errorMessage, Throwable cause) {
		super(errorMessage, cause);
	}
}
