package com.dalcoomi.common.encryption;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;

@Converter
@Component
@RequiredArgsConstructor
public class EncryptedLocalDateConverter implements AttributeConverter<LocalDate, String> {

	private final EncryptionService encryptionService;

	@Override
	public String convertToDatabaseColumn(LocalDate attribute) {
		if (attribute == null) {
			return null;
		}

		return encryptionService.encrypt(attribute.toString());
	}

	@Override
	public LocalDate convertToEntityAttribute(String dbData) {
		if (dbData == null) {
			return null;
		}

		String decryptedData = encryptionService.decrypt(dbData);

		return LocalDate.parse(decryptedData);
	}
}
