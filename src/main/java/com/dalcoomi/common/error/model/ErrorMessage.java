package com.dalcoomi.common.error.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorMessage {

	SERVER_ERROR("서버 에러가 발생했습니다."),

	INVALID_REQUEST_BODY("요청 바디가 올바르지 않습니다."),
	MISSING_REQUIRED_PARAMETER_ERROR("필수 파라미터 '%s'가 누락되었습니다."),
	PARAMETER_FORMAT_NOT_CORRECT("파라미터 '%s'의 형식이 올바르지 않습니다."),
	INPUT_VALUE_IS_INVALID("입력값이 유효하지 않습니다."),

	AUTHORIZATION_HEADER_ERROR("Authorization 헤더가 존재하지 않거나 올바르지 않은 형식입니다."),
	MISSING_MEMBER_ID("AccessToken에 memberId가 존재하지 않습니다."),
	MALFORMED_TOKEN("잘못된 형식의 토큰입니다."),
	TOKEN_HAS_EXPIRED("토큰이 만료되었습니다."),
	INVALID_TOKEN("유효하지 않는 토큰입니다."),
	TOKEN_NOT_FOUND("토큰이 존재하지 않습니다."),

	S3_UPLOAD_ERROR("S3로 이미지 업로드 중 오류가 발생했습니다."),
	S3_DELETE_ERROR("S3 이미지 삭제 중 오류가 발생했습니다."),

	IMAGE_NOT_FOUND("이미지가 존재하지 않습니다."),
	IMAGE_NOT_SUPPORT("지원하지 않는 이미지 형식입니다. jpg, jpeg, png, svg만 가능합니다."),
	MAX_UPLOAD_SIZE_EXCEEDED("파일 업로드 크기 10MB 제한을 초과했습니다."),

	ENCRYPTION_FAILED("데이터 암호화에 실패했습니다."),
	DECRYPTION_FAILED("데이터 복호화에 실패했습니다."),
	ENCRYPTION_KEY_INVALID("암호화 키가 유효하지 않습니다. 32자여야 합니다."),
	ENCRYPTION_KEY_GENERATION_FAILED("암호화 키 생성에 실패했습니다."),

	LOCK_EXIST_ERROR("동일한 요청이 처리 중입니다."),

	MEMBER_NOT_FOUND("존재하지 않는 회원입니다."),
	MEMBER_CONFLICT("이미 존재하는 회원입니다."),
	MEMBER_DORMANT_ACCOUNT("휴면 계정입니다. 계정 활성화 후 이용해 주세요."),
	MEMBER_NICKNAME_CONFLICT("이미 존재하는 닉네임입니다."),
	MEMBER_INVALID_SOCIAL_ID("유효하지 않는 소셜 ID 입니다."),
	MEMBER_INVALID_EMAIL("유효하지 않는 EMAIL 입니다."),
	MEMBER_INVALID_NAME("유효하지 않는 이름입니다."),
	MEMBER_INVALID_NICKNAME("유효하지 않는 닉네임입니다."),
	MEMBER_NICKNAME_IS_NULL("닉네임은 필수입니다."),
	MEMBER_NICKNAME_RANGE_ERROR("닉네임은 2~6자입니다."),
	MEMBER_NICKNAME_INVALID_PATTERN("닉네임은 한글, 영문, 숫자, 언더스코어만 사용 가능합니다."),
	MEMBER_INVALID_GENDER("유효하지 않는 성별입니다."),
	MEMBER_INVALID_PROFILE_IMAGE_URL("유효하지 않는 프로필입니다."),
	MEMBER_INVALID_SERVICE_AGREEMENT("서비스 이용 약관 동의 여부는 true 여야 합니다."),
	MEMBER_INVALID_COLLECTION_AGREEMENT("개인 정보 수집 동의 여부는 true 여야 합니다."),

	UNSUPPORTED_SOCIAL_TYPE("지원하지 않는 소셜 서비스입니다."),
	SOCIAL_CONNECTION_NOT_FOUND("존재하지 않는 소셜 정보입니다."),
	LAST_SOCIAL_CONNECTION("소셜 연결은 최소 1개 이상 있어야 합니다."),

	WITHDRAWAL_NOT_FOUND("탈퇴 데이터가 존재하지 않습니다."),
	WITHDRAWAL_INVALID_OTHER_REASON("유효하지 않는 기타 사유입니다."),

	UNSUPPORTED_WITHDRAWAL_TYPE("지원하지 않는 탈퇴 타입입니다."),
	UNSUPPORTED_TRANSACTION_TYPE("지원하지 않는 거래 타입입니다."),
	UNSUPPORTED_OWNER_TYPE("지원하지 않는 집합 속성입니다."),

	CATEGORY_NOT_FOUND("카테고리가 존재하지 않습니다."),
	CATEGORY_INVALID_TEAM_ID("유효하지 않는 그룹 id입니다."),
	CATEGORY_INVALID_NAME("유효하지 않는 카테고리명입니다."),
	CATEGORY_INVALID_ICON_URL("유효하지 않는 아이콘입니다."),

	TRANSACTION_NOT_FOUND("거래 내역이 존재하지 않습니다."),
	TRANSACTION_INVALID_CONTENT("유효하지 않는 거래 내용입니다."),
	TRANSACTION_INVALID_AMOUNT("유효하지 않는 금액입니다."),
	TRANSACTION_TEAM_INCONSISTENCY("해당 거래 내역의 그룹만 가능합니다."),
	TRANSACTION_CREATOR_INCONSISTENCY("해당 거래 내역 작성자만 가능합니다."),
	DOES_NOT_MATCH_CATEGORY_AND_TRANSACTION("카테고리와 거래 내역의 개수가 일치하지 않습니다."),

	TEAM_NOT_FOUND("존재하지 않는 그룹입니다."),
	TEAM_INVALID_LEADER("그룹 리더만 가능합니다."),
	TEAM_INVALID_INVITATION_CODE("유효하지 않는 초대 코드입니다."),
	TEAM_INVALID_TITLE("유효하지 않는 그룹명입니다."),
	TEAM_INVALID_MEMBER_LIMIT("그룹 인원 수는 1명 이상 %d명 이하여야 합니다."),
	TEAM_INVALID_PURPOSE("유효하지 않는 그룹 목표입니다."),
	TEAM_COUNT_EXCEEDED("최대 3개 그룹까지만 참가할 수 있습니다."),

	TEAM_MEMBER_ALREADY_EXISTS("이미 해당 그룹에 가입된 회원입니다."),
	TEAM_MEMBER_NOT_ENOUGH_MAX_COUNT("최대 그룹 인원이 현재 그룹 인원보다 작습니다."),
	TEAM_MEMBER_COUNT_EXCEEDED("그룹 인원 제한을 초과했습니다."),
	TEAM_MEMBER_NOT_FOUND("해당 그룹 멤버를 찾을 수 없습니다.");

	private final String message;
}
