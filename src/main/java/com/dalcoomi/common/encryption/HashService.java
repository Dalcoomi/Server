package com.dalcoomi.common.encryption;

import static com.dalcoomi.common.error.model.ErrorMessage.ENCRYPTION_FAILED;
import static com.dalcoomi.common.error.model.ErrorMessage.ENCRYPTION_KEY_INVALID;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.security.MessageDigest;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.dalcoomi.common.error.exception.DalcoomiException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class HashService {

	private static final String ALGORITHM = "HmacSHA256";
	private static final String PREFIX = "HASH_";

	private final SecretKeySpec secretKey;

	public HashService(@Value("${app.encryption.key:}") String encryptionKey) {
		this.secretKey = generateKey(encryptionKey);
	}

	public String hash(String plainText) {
		if (!StringUtils.hasText(plainText)) {
			return plainText;
		}

		try {
			Mac mac = Mac.getInstance(ALGORITHM);
			mac.init(secretKey);

			byte[] hmacBytes = mac.doFinal(plainText.getBytes(UTF_8));
			String encodedHmac = Base64.getEncoder().encodeToString(hmacBytes);

			return PREFIX + encodedHmac;
		} catch (Exception e) {
			log.error(ENCRYPTION_FAILED.getMessage(), e);

			throw new DalcoomiException(ENCRYPTION_FAILED, e);
		}
	}

	private SecretKeySpec generateKey(String encryptionKey) {
		if (StringUtils.hasText(encryptionKey)) {
			if (encryptionKey.length() != 32) {
				throw new DalcoomiException(ENCRYPTION_KEY_INVALID);
			}

			try {
				MessageDigest digest = MessageDigest.getInstance("SHA-256");
				String hashKey = PREFIX + encryptionKey;
				byte[] keyBytes = digest.digest(hashKey.getBytes(UTF_8));

				return new SecretKeySpec(keyBytes, ALGORITHM);
			} catch (Exception e) {
				throw new DalcoomiException(ENCRYPTION_FAILED, e);
			}
		}

		throw new DalcoomiException(ENCRYPTION_KEY_INVALID);
	}
}
