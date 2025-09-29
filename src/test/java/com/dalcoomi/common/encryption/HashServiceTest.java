package com.dalcoomi.common.encryption;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Stream;

import javax.crypto.Mac;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;

import com.dalcoomi.common.error.exception.DalcoomiException;

class HashServiceTest {

	static Stream<Arguments> provideTextsForHashTest() {
		return Stream.of(
			Arguments.of("한글 텍스트", "안녕하세요 테스트입니다"),
			Arguments.of("긴 텍스트", "테스트".repeat(10)),
			Arguments.of("특수문자", "!@#$%^&*()_+-=[]{}|;':\",./<>?`~"),
			Arguments.of("숫자만", "1234567890"),
			Arguments.of("유니코드 이모지", "😀😃😄😁😆😅🤣😂🙂🙃😉😊😇🥰😍🤩"),
			Arguments.of("매우 긴 텍스트", "A".repeat(10000)),
			Arguments.of("한 글자", "A"),
			Arguments.of("개행문자 포함", "line1\nline2\rline3\r\nline4"),
			Arguments.of("탭문자 포함", "col1\tcol2\tcol3")
		);
	}

	@Test
	@DisplayName("해시 생성 성공")
	void hash_success() {
		// given
		HashService hashService = new HashService("test1234567890test1234567890test");
		String plainText = "testEmail@example.com";

		// when
		String hashed = hashService.hash(plainText);

		// then
		assertThat(hashed).isNotEqualTo(plainText).startsWith("HASH_").isNotNull();
	}

	@Test
	@DisplayName("서로 다른 키로 만든 HashService는 다른 해시 생성 성공")
	void different_keys_produce_different_hashes_success() {
		// given
		HashService hashService1 = new HashService("test1234567890test1234567890test");
		HashService hashService2 = new HashService("diff1234567890diff1234567890test");
		String plainText = "testEmail@example.com";

		// when
		String hashed1 = hashService1.hash(plainText);
		String hashed2 = hashService2.hash(plainText);

		// then
		assertThat(hashed1).isNotEqualTo(hashed2).startsWith("HASH_");
		assertThat(hashed2).startsWith("HASH_");
	}

	@Test
	@DisplayName("다른 값은 다른 해시 생성 성공")
	void hash_different_values_success() {
		// given
		HashService hashService = new HashService("test1234567890test1234567890test");
		String plainText1 = "testEmail1@example.com";
		String plainText2 = "testEmail2@example.com";

		// when
		String hashed1 = hashService.hash(plainText1);
		String hashed2 = hashService.hash(plainText2);

		// then
		assertThat(hashed1).isNotEqualTo(hashed2);
	}

	@Test
	@DisplayName("null 값은 해시 생성 실패")
	void hash_null_fail() {
		// given
		HashService hashService = new HashService("test1234567890test1234567890test");

		// when & then
		assertThat(hashService.hash(null)).isNull();
	}

	@Test
	@DisplayName("빈 문자열은 해시 생성 실패")
	void hash_empty_string_fail() {
		// given
		HashService hashService = new HashService("test1234567890test1234567890test");

		// when & then
		assertThat(hashService.hash("")).isEmpty();
	}

	@Test
	@DisplayName("키 없으면 해시 생성 실패")
	void create_without_key_hash_fail() {
		// given & when & then
		assertThatThrownBy(() -> new HashService("")).isInstanceOf(DalcoomiException.class);
	}

	@ParameterizedTest
	@DisplayName("잘못된 키 길이 해시 생성 실패")
	@ValueSource(strings = {"1234567890123456789012345678901", "123456789012345678901234567890123"})
	void invalid_key_length_hash_fail(String invalidKey) {
		// given & when & then
		assertThatThrownBy(() -> new HashService(invalidKey)).isInstanceOf(DalcoomiException.class);
	}

	@ParameterizedTest
	@DisplayName("다양한 텍스트 해시 성공 테스트")
	@MethodSource("provideTextsForHashTest")
	void various_text_hash_success(String plainText) {
		// given
		HashService hashService = new HashService("test1234567890test1234567890test");

		// when
		String hashed = hashService.hash(plainText);

		// then
		assertThat(hashed).isNotEqualTo(plainText).startsWith("HASH_");
	}

	@Test
	@DisplayName("MessageDigest 예외 발생 시 해시 생성 실패")
	void generate_key_message_digest_exception_hash_fail() {
		// given & when & then
		try (MockedStatic<MessageDigest> mockedMessageDigest = mockStatic(MessageDigest.class)) {
			mockedMessageDigest.when(() -> MessageDigest.getInstance("SHA-256"))
				.thenThrow(new NoSuchAlgorithmException("Test exception"));

			assertThatThrownBy(() -> new HashService("test1234567890test1234567890test"))
				.isInstanceOf(DalcoomiException.class);
		}
	}

	@Test
	@DisplayName("Mac 예외 발생 시 해시 생성 실패")
	void mac_exception_hash_fail() {
		// given
		HashService hashService = new HashService("test1234567890test1234567890test");

		// when & then
		try (MockedStatic<Mac> mockedMac = mockStatic(Mac.class)) {
			mockedMac.when(() -> Mac.getInstance(any()))
				.thenThrow(new NoSuchAlgorithmException("Test exception"));

			assertThatThrownBy(() -> hashService.hash("test"))
				.isInstanceOf(DalcoomiException.class);
		}
	}
}
