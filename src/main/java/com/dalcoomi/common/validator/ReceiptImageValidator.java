package com.dalcoomi.common.validator;

import java.util.Arrays;
import java.util.List;

public class ReceiptImageValidator extends AbstractImageValidator<ValidReceiptImage> {

	private static final List<String> ALLOWED_FORMATS = Arrays.asList("jpg", "jpeg", "png", "bmp", "tiff", "webp",
		"heic", "heif");

	@Override
	protected List<String> getAllowedFormats() {
		return ALLOWED_FORMATS;
	}
}
