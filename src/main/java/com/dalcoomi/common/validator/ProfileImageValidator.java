package com.dalcoomi.common.validator;

import java.util.Arrays;
import java.util.List;

public class ProfileImageValidator extends AbstractImageValidator<ValidProfileImage> {

	private static final List<String> ALLOWED_FORMATS = Arrays.asList("jpg", "jpeg", "png");

	@Override
	protected List<String> getAllowedFormats() {
		return ALLOWED_FORMATS;
	}
}
