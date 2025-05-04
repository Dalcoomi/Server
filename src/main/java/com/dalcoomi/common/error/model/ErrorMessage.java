package com.dalcoomi.common.error.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorMessage {

	SERVER_ERROR("서버 에러가 발생했습니다."),
	S3_UPLOAD_ERROR("S3로 이미지 업로드 중 오류가 발생했습니다."),
	S3_DELETE_ERROR("S3 이미지 삭제 중 오류가 발생했습니다."),
	IMAGE_NOT_FOUND("이미지가 존재하지 않습니다."),
	IMAGE_NOT_SUPPORT("지원하지 않는 이미지 형식입니다. jpg, jpeg, png, svg만 가능합니다."),
	INVALID_REQUEST_BODY("요청 바디가 올바르지 않습니다."),
	MAX_UPLOAD_SIZE_EXCEEDED("파일 업로드 크기 10MB 제한을 초과했습니다."),

	MISSING_REQUIRED_PARAMETER_ERROR("필수 파라미터 '%s'가 누락되었습니다."),
	PARAMETER_FORMAT_NOT_CORRECT("파라미터 '%s'의 형식이 올바르지 않습니다."),
	INPUT_VALUE_IS_INVALID("입력값이 유효하지 않습니다."),

	AUTHORIZATION_HEADER_ERROR("Authorization 헤더가 존재하지 않거나 올바르지 않은 형식입니다."),
	MISSING_MEMBER_ID("AccessToken에 memberId가 존재하지 않습니다."),
	MALFORMED_TOKEN("잘못된 형식의 토큰입니다."),
	TOKEN_HAS_EXPIRED("토큰이 만료되었습니다."),
	INVALID_TOKEN("유효하지 않는 토큰입니다."),

	UNSUPPORTED_SOCIAL_TYPE("지원하지 않는 소셜 서비스입니다."),
	KAKAO_SERVER_ERROR("Kakao 서버 에러가 발생했습니다."),

	MEMBER_NOT_FOUND("존재하지 않는 회원입니다."),
	MEMBER_CONFLICT("이미 존재하는 회원입니다."),
	MEMBER_INVALID_SOCIAL_ID("유효하지 않는 소셜 ID 입니다."),
	MEMBER_INVALID_EMAIL("유효하지 않는 EMAIL 입니다."),
	MEMBER_INVALID_NAME("유효하지 않는 이름입니다."),
	MEMBER_INVALID_PROFILE_IMAGE_URL("유효하지 않는 프로필입니다."),
	MEMBER_UNSUPPORTED_MEMBER_TYPE("지원하지 않는 회원 타입입니다."),
	MEMBER_INVALID_SERVICE_AGREEMENT("서비스 이용 약관 동의 여부는 true 여야 합니다."),
	MEMBER_INVALID_COLLECTION_AGREEMENT("개인 정보 수집 동의 여부는 true 여야 합니다."),
	MEMBER_NULL_ADVERTISEMENT_AGREEMENT("회원 광고성 알림 수신 동의 여부가 null 입니다.");

	private final String message;
}
