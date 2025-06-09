package com.dalcoomi.common.error.exception;

import com.dalcoomi.common.error.model.ErrorMessage;

public class ImageException extends DalcoomiException {

	public ImageException(ErrorMessage errorMessage) {
		super(errorMessage);
	}

	public ImageException(ErrorMessage errorMessage, Throwable cause) {
		super(errorMessage, cause);
	}
}
