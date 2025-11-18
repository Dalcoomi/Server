package com.dalcoomi.common.validator;

import java.lang.annotation.Annotation;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public abstract class AbstractImageValidator<A extends Annotation> implements ConstraintValidator<A, MultipartFile> {

	protected abstract List<String> getAllowedFormats();

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

		return getAllowedFormats().contains(extension);
	}

	private String getExtension(String filename) {
		int lastDotIndex = filename.lastIndexOf('.');

		if (lastDotIndex == -1) {
			return "";
		}

		return filename.substring(lastDotIndex + 1);
	}
}
