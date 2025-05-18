package com.dalcoomi.common.util;

import static java.lang.String.format;

import java.security.SecureRandom;
import java.util.Random;

public class NicknameProvider {

	private static final String SEPARATOR = " #";

	private final Random random;

	public NicknameProvider() {
		this.random = new SecureRandom();
	}

	public String generateUniqueNickname(String name, int digits) {
		int maxValue = (int)Math.pow(10, digits);
		int randomNum = random.nextInt(maxValue);

		@SuppressWarnings("java:S3457")
		String formattedNumber = format("%0" + digits + "d", randomNum);

		return name + SEPARATOR + formattedNumber;
	}
}
