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
		if (!StringUtils.hasText(value) || value.length() < 20) {
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
