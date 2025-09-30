package com.dalcoomi.common.encryption;

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

		try {
			return encryptionService.decrypt(dbData);
		} catch (Exception e) {
			return dbData;
		}
	}
}
