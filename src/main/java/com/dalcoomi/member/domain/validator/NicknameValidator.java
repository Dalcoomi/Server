package com.dalcoomi.member.domain.validator;

import static com.dalcoomi.member.constant.MemberConstants.NICKNAME_MAX_LENGTH;
import static com.dalcoomi.member.constant.MemberConstants.NICKNAME_MIN_LENGTH;

import org.springframework.stereotype.Component;

@Component
public class NicknameValidator {

	private static final String NICKNAME_PATTERN = "^[가-힣a-zA-Z0-9_]+$";

	public void validate(String nickname) {
		if (nickname == null || nickname.trim().isEmpty()) {
			throw new IllegalArgumentException("닉네임은 필수입니다.");
		}

		if (nickname.length() < NICKNAME_MIN_LENGTH || nickname.length() > NICKNAME_MAX_LENGTH) {
			throw new IllegalArgumentException("닉네임은 2~15자입니다.");
		}

		if (!nickname.matches(NICKNAME_PATTERN)) {
			throw new IllegalArgumentException("닉네임은 한글, 영문, 숫자, 언더스코어만 사용 가능합니다.");
		}
	}
}
