package com.dalcoomi.common.encryption;

import org.springframework.stereotype.Component;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;

@Converter
@Component
@RequiredArgsConstructor
public class EncryptedLongConverter implements AttributeConverter<Long, String> {

	private final EncryptionService encryptionService;

	@Override
	public String convertToDatabaseColumn(Long attribute) {
		if (attribute == null) {
			return null;
		}

		return encryptionService.encrypt(attribute.toString());
	}

	@Override
	public Long convertToEntityAttribute(String dbData) {
		if (dbData == null) {
			return null;
		}

		String decryptedData = encryptionService.decrypt(dbData);

		return Long.valueOf(decryptedData);
	}
}
