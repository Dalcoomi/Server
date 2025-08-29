package com.dalcoomi.member.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberConstants {

	public static final int EMAIL_LENGTH = 100;
	public static final int NAME_MIN_LENGTH = 2;
	public static final int NAME_MAX_LENGTH = 30;
	public static final int NICKNAME_MIN_LENGTH = 2;
	public static final int NICKNAME_MAX_LENGTH = 35;
	public static final int GENDER_LENGTH = 2;
	public static final int PROFILE_IMAGE_URL_LENGTH = 255;
	public static final int OTHER_REASON_LENGTH = 50;
}
