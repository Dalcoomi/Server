package com.dalcoomi.member.domain.validator;

import static com.dalcoomi.common.error.model.ErrorMessage.MEMBER_NICKNAME_INVALID_PATTERN;
import static com.dalcoomi.common.error.model.ErrorMessage.MEMBER_NICKNAME_IS_NULL;
import static com.dalcoomi.common.error.model.ErrorMessage.MEMBER_NICKNAME_RANGE_ERROR;
import static com.dalcoomi.member.constant.MemberConstants.NICKNAME_MAX_LENGTH;
import static com.dalcoomi.member.constant.MemberConstants.NICKNAME_MIN_LENGTH;

import org.springframework.stereotype.Component;

import com.dalcoomi.common.error.exception.BadRequestException;

@Component
public class NicknameValidator {

	private static final String NICKNAME_PATTERN = "^[가-힣a-zA-Z0-9_]+$";

	public void validate(String nickname) {
		if (nickname == null || nickname.trim().isEmpty()) {
			throw new BadRequestException(MEMBER_NICKNAME_IS_NULL);
		}

		if (nickname.length() < NICKNAME_MIN_LENGTH || nickname.length() > NICKNAME_MAX_LENGTH) {
			throw new BadRequestException(MEMBER_NICKNAME_RANGE_ERROR);
		}

		if (!nickname.matches(NICKNAME_PATTERN)) {
			throw new BadRequestException(MEMBER_NICKNAME_INVALID_PATTERN);
		}
	}
}
