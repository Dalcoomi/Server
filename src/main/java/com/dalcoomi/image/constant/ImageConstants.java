package com.dalcoomi.image.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ImageConstants {

	public static final int MAX_WIDTH = 1200;
	public static final int MAX_HEIGHT = 1200;
	public static final double IMAGE_QUALITY = 0.9;

	public static final String DEFAULT_PROFILE_IMAGE_1 = "https://dalcoomi.s3.ap-northeast-2.amazonaws.com/profile/%EA%B8%B0%EB%B3%B8_%ED%94%84%EB%A1%9C%ED%95%84_%EC%82%AC%EC%A7%84_1.png";
	public static final String DEFAULT_PROFILE_IMAGE_2 = "https://dalcoomi.s3.ap-northeast-2.amazonaws.com/profile/%EA%B8%B0%EB%B3%B8_%ED%94%84%EB%A1%9C%ED%95%84_%EC%82%AC%EC%A7%84_2.png";
	public static final String DEFAULT_PROFILE_IMAGE_3 = "https://dalcoomi.s3.ap-northeast-2.amazonaws.com/profile/%EA%B8%B0%EB%B3%B8_%ED%94%84%EB%A1%9C%ED%95%84_%EC%82%AC%EC%A7%84_3.png";
	public static final String DEFAULT_PROFILE_IMAGE_4 = "https://dalcoomi.s3.ap-northeast-2.amazonaws.com/profile/%EA%B8%B0%EB%B3%B8_%ED%94%84%EB%A1%9C%ED%95%84_%EC%82%AC%EC%A7%84_4.png";

	public static final String S3_PROFILE_IMAGE_PATH = "profile";

	public static final String S3_CATEGORY_ADMIN_IMAGE_PATH = "category/admin";
	public static final String S3_CATEGORY_MEMBER_IMAGE_PATH = "category/member";
}
