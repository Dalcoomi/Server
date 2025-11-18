package com.dalcoomi.common.validator;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class ReceiptImageValidatorTest {

	private ReceiptImageValidator validator;

	@BeforeEach
	void setUp() {
		validator = new ReceiptImageValidator();
		validator.initialize(null);
	}

	@Test
	@DisplayName("null 파일은 유효함")
	void null_file_is_valid() {
		// given
		// when
		boolean result = validator.isValid(null, null);

		// then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("비어있는 파일은 유효함")
	void empty_file_is_valid() {
		// given
		MockMultipartFile emptyFile = new MockMultipartFile(
			"file",
			"test.jpg",
			"image/jpeg",
			new byte[0]
		);

		// when
		boolean result = validator.isValid(emptyFile, null);

		// then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("유효한 jpg 파일은 유효함")
	void valid_jpg_file_is_valid() {
		// given
		MockMultipartFile jpgFile = new MockMultipartFile(
			"file",
			"receipt.jpg",
			"image/jpeg",
			"image content".getBytes()
		);

		// when
		boolean result = validator.isValid(jpgFile, null);

		// then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("유효한 png 파일은 유효함")
	void valid_png_file_is_valid() {
		// given
		MockMultipartFile pngFile = new MockMultipartFile(
			"file",
			"receipt.png",
			"image/png",
			"image content".getBytes()
		);

		// when
		boolean result = validator.isValid(pngFile, null);

		// then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("파일명이 null인 파일은 유효하지 않음")
	void file_with_null_filename_is_invalid() {
		// given
		MockMultipartFile fileWithNullName = new MockMultipartFile(
			"file",
			null,
			"image/jpeg",
			"image content".getBytes()
		);

		// when
		boolean result = validator.isValid(fileWithNullName, null);

		// then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("허용되지 않은 확장자는 유효하지 않음")
	void invalid_extension_file_is_invalid() {
		// given
		MockMultipartFile invalidFile = new MockMultipartFile(
			"file",
			"receipt.txt",
			"text/plain",
			"image content".getBytes()
		);

		// when
		boolean result = validator.isValid(invalidFile, null);

		// then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("확장자가 없는 파일은 유효하지 않음")
	void file_without_extension_is_invalid() {
		// given
		MockMultipartFile fileWithoutExtension = new MockMultipartFile(
			"file",
			"receipt",
			"application/octet-stream",
			"image content".getBytes()
		);

		// when
		boolean result = validator.isValid(fileWithoutExtension, null);

		// then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("대문자 확장자도 유효함")
	void uppercase_extension_is_valid() {
		// given
		MockMultipartFile uppercaseFile = new MockMultipartFile(
			"file",
			"receipt.JPG",
			"image/jpeg",
			"image content".getBytes()
		);

		// when
		boolean result = validator.isValid(uppercaseFile, null);

		// then
		assertThat(result).isTrue();
	}
}
