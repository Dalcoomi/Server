package com.dalcoomi.common.encryption;

import static com.dalcoomi.common.error.model.ErrorMessage.DECRYPTION_FAILED;
import static com.dalcoomi.common.error.model.ErrorMessage.ENCRYPTION_FAILED;
import static com.dalcoomi.common.error.model.ErrorMessage.ENCRYPTION_KEY_GENERATION_FAILED;
import static com.dalcoomi.common.error.model.ErrorMessage.ENCRYPTION_KEY_INVALID;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.dalcoomi.common.error.exception.DalcoomiException;

import lombok.extern.slf4j.Slf4j;

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

	public String encrypt(String plainText) {
		if (!StringUtils.hasText(plainText)) {
			return plainText;
		}

		try {
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);

			byte[] iv = new byte[GCM_IV_LENGTH];

			new SecureRandom().nextBytes(iv);

			GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);

			cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec);

			byte[] encryptedText = cipher.doFinal(plainText.getBytes(UTF_8));
			byte[] encryptedWithIv = new byte[GCM_IV_LENGTH + encryptedText.length];

			System.arraycopy(iv, 0, encryptedWithIv, 0, GCM_IV_LENGTH);
			System.arraycopy(encryptedText, 0, encryptedWithIv, GCM_IV_LENGTH, encryptedText.length);

			return Base64.getEncoder().encodeToString(encryptedWithIv);
		} catch (Exception e) {
			log.error(ENCRYPTION_FAILED.getMessage(), e);

			throw new DalcoomiException(ENCRYPTION_FAILED, e);
		}
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
			log.error(DECRYPTION_FAILED.getMessage(), e);

			throw new DalcoomiException(DECRYPTION_FAILED, e);
		}
	}

	private SecretKey generateOrLoadKey(String encryptionKey) {
		if (StringUtils.hasText(encryptionKey)) {
			if (encryptionKey.length() != 32) {
				throw new DalcoomiException(ENCRYPTION_KEY_INVALID);
			}

			return new SecretKeySpec(encryptionKey.getBytes(UTF_8), ALGORITHM);
		}

		try {
			KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);

			keyGenerator.init(256);

			return keyGenerator.generateKey();
		} catch (Exception e) {
			throw new DalcoomiException(ENCRYPTION_KEY_GENERATION_FAILED, e);
		}
	}
}