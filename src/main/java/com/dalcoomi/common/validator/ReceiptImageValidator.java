package com.dalcoomi.common.validator;

import java.util.Arrays;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ReceiptImageValidator implements ConstraintValidator<ValidReceiptImage, MultipartFile> {

	private static final List<String> ALLOWED_FORMATS = Arrays.asList("jpg", "jpeg", "png", "bmp", "tiff", "webp",
		"heic", "heif");

	@Override
	public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
		if (file == null || file.isEmpty()) {
			return true;
		}

		String filename = file.getOriginalFilename();

		if (filename == null) {
			return false;
		}

		String extension = getExtension(filename).toLowerCase();

		return ALLOWED_FORMATS.contains(extension);
	}

	private String getExtension(String filename) {
		int lastDotIndex = filename.lastIndexOf('.');

		if (lastDotIndex == -1) {
			return "";
		}

		return filename.substring(lastDotIndex + 1);
	}
}
