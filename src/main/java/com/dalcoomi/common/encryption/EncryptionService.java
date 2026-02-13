package com.dalcoomi.common.encryption;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.security.MessageDigest;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 복호화 마이그레이션 완료 후 삭제 예정
 */
@Slf4j
@Service
public class EncryptionService {

	private static final String ALGORITHM = "AES";
	private static final String TRANSFORMATION = "AES/GCM/NoPadding";
	private static final int GCM_IV_LENGTH = 12;
	private static final int GCM_TAG_LENGTH = 16;

	private final SecretKey secretKey;

	public EncryptionService(@Value("${app.encryption.key:}") String encryptionKey) {
		this.secretKey = generateOrLoadKey(encryptionKey);
	}

	public String decrypt(String cipherText) {
		if (!StringUtils.hasText(cipherText)) {
			return cipherText;
		}

		try {
			byte[] decodedText = Base64.getDecoder().decode(cipherText);
			byte[] iv = new byte[GCM_IV_LENGTH];

			System.arraycopy(decodedText, 0, iv, 0, GCM_IV_LENGTH);

			byte[] encrypted = new byte[decodedText.length - GCM_IV_LENGTH];

			System.arraycopy(decodedText, GCM_IV_LENGTH, encrypted, 0, encrypted.length);

			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);

			cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec);

			byte[] decryptedText = cipher.doFinal(encrypted);

			return new String(decryptedText, UTF_8);
		} catch (Exception e) {
			log.error("데이터 복호화 실패. 복호화 대상: {}", cipherText);

			throw new RuntimeException("데이터 복호화에 실패했습니다.", e);
		}
	}

	private SecretKey generateOrLoadKey(String encryptionKey) {
		if (StringUtils.hasText(encryptionKey)) {
			if (encryptionKey.length() != 32) {
				throw new IllegalArgumentException("암호화 키가 유효하지 않습니다. 32자여야 합니다.");
			}

			try {
				MessageDigest digest = MessageDigest.getInstance("SHA-256");
				byte[] keyBytes = digest.digest(encryptionKey.getBytes(UTF_8));

				return new SecretKeySpec(keyBytes, ALGORITHM);
			} catch (Exception e) {
				throw new RuntimeException("암호화 키 생성에 실패했습니다.", e);
			}
		}

		throw new IllegalArgumentException("암호화 키가 유효하지 않습니다. 32자여야 합니다.");
	}
}
