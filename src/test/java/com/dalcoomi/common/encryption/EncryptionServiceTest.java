package com.dalcoomi.common.encryption;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.dalcoomi.common.error.exception.DalcoomiException;

class EncryptionServiceTest {

	@Test
	@DisplayName("암호화/복호화 성공")
	void encrypt_and_decrypt_success() {
		// given
		EncryptionService encryptionService = new EncryptionService("test1234567890test1234567890test");
		String plainText = "testEmail@example.com";

		// when
		String encrypted = encryptionService.encrypt(plainText);
		String decrypted = encryptionService.decrypt(encrypted);

		// then
		assertThat(encrypted).isNotEqualTo(plainText);
		assertThat(decrypted).isEqualTo(plainText);
	}

	@Test
	@DisplayName("IV 랜덤이라 같은 값을 암호화해도 다른 결과 성공")
	void encrypt_same_value_twice_success() {
		// given
		EncryptionService encryptionService = new EncryptionService("test1234567890test1234567890test");
		String plainText = "testEmail@example.com";

		// when
		String encrypted1 = encryptionService.encrypt(plainText);
		String encrypted2 = encryptionService.encrypt(plainText);

		// then
		assertThat(encrypted1).isNotEqualTo(encrypted2);
		assertThat(encryptionService.decrypt(encrypted1)).isEqualTo(plainText);
		assertThat(encryptionService.decrypt(encrypted2)).isEqualTo(plainText);
	}

	@Test
	@DisplayName("null 값 암호화 실패")
	void encrypt_null_fail() {
		// given
		EncryptionService encryptionService = new EncryptionService("test1234567890test1234567890test");

		// when & then
		assertThat(encryptionService.encrypt(null)).isNull();
		assertThat(encryptionService.decrypt(null)).isNull();
	}

	@Test
	@DisplayName("빈 문자열 암호화 실패")
	void encrypt_empty_string_fail() {
		// given
		EncryptionService encryptionService = new EncryptionService("test1234567890test1234567890test");

		// when & then
		assertThat(encryptionService.encrypt("")).isEmpty();
		assertThat(encryptionService.decrypt("")).isEmpty();
	}

	@Test
	@DisplayName("잘못된 키 길이는 암호화 실패")
	void invalid_key_length_encrypt_fail() {
		// given & when & then
		assertThatThrownBy(() -> new EncryptionService("shortkey")).isInstanceOf(DalcoomiException.class);
	}

	@Test
	@DisplayName("잘못된 암호화 데이터 복호화 시 실패")
	void decrypt_invalid_data_fail() {
		// given
		EncryptionService encryptionService = new EncryptionService("test1234567890test1234567890test");
		String invalidEncryptedData = "invalid_base64_data";

		// when & then
		assertThatThrownBy(() -> encryptionService.decrypt(invalidEncryptedData)).isInstanceOf(DalcoomiException.class);
	}

	@Test
	@DisplayName("키 없으면 암호화 실패")
	void create_without_key_fail() {
		// given & when & then
		assertThatThrownBy(() -> new EncryptionService("")).isInstanceOf(DalcoomiException.class);
	}

	@Test
	@DisplayName("한글 텍스트 암호화/복호화 성공")
	void encrypt_korean_text_success() {
		// given
		EncryptionService encryptionService = new EncryptionService("test1234567890test1234567890test");
		String plainText = "안녕하세요 테스트입니다";

		// when
		String encrypted = encryptionService.encrypt(plainText);
		String decrypted = encryptionService.decrypt(encrypted);

		// then
		assertThat(encrypted).isNotEqualTo(plainText);
		assertThat(decrypted).isEqualTo(plainText);
	}

	@Test
	@DisplayName("긴 텍스트 암호화/복호화 성공")
	void encrypt_long_text_success() {
		// given
		EncryptionService encryptionService = new EncryptionService("test1234567890test1234567890test");
		String plainText = "안녕하세요 테스트입니다.".repeat(10);

		// when
		String encrypted = encryptionService.encrypt(plainText);
		String decrypted = encryptionService.decrypt(encrypted);

		// then
		assertThat(encrypted).isNotEqualTo(plainText);
		assertThat(decrypted).isEqualTo(plainText);
	}
}
