package com.dalcoomi.common.encryption;

import java.util.Base64;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;

@Converter
@Component
@RequiredArgsConstructor
public class EncryptedStringConverter implements AttributeConverter<String, String> {

	private final EncryptionService encryptionService;

	@Override
	public String convertToDatabaseColumn(String attribute) {
		return encryptionService.encrypt(attribute);
	}

	@Override
	public String convertToEntityAttribute(String dbData) {
		if (!StringUtils.hasText(dbData)) {
			return dbData;
		}

		if (isEncrypted(dbData)) {
			return encryptionService.decrypt(dbData);
		}

		return dbData;
	}

	private boolean isEncrypted(String value) {
		// AES-GCM 암호문 최소 길이: Base64(IV 12바이트 + 최소 암호문 + Tag 16바이트) = 약 50자 이상
		// 평문 이메일/이름은 대부분 100자 이하이므로 100자 기준으로 판별
		if (!StringUtils.hasText(value) || value.length() < 100) {
			return false;
		}

		try {
			Base64.getDecoder().decode(value);

			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}
}
