package com.dalcoomi.common.error.exception;

import com.dalcoomi.common.error.model.ErrorMessage;

public class OAuthException extends DalcoomiException {

	public OAuthException(ErrorMessage errorMessage) {
		super(errorMessage);
	}

	public OAuthException(ErrorMessage errorMessage, Throwable cause) {
		super(errorMessage, cause);
	}
}
