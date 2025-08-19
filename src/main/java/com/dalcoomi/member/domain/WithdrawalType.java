package com.dalcoomi.member.domain;

import static com.dalcoomi.common.error.model.ErrorMessage.UNSUPPORTED_WITHDRAWAL_TYPE;

import com.dalcoomi.common.error.exception.DalcoomiException;
import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WithdrawalType {

	LOW_USAGE_FREQUENCY("사용 빈도가 낮아요"),
	LACK_OF_FEATURES("기능이 부족하거나 불편했어요"),
	USING_OTHER_SERVICE("다른 가계부 서비스를 이용하고 있어요"),
	DIFFICULT_UI_UX("UI/UX가 복잡하거나 어려웠어요"),
	FREQUENT_BUGS("오류나 버그가 자주 발생했어요"),
	PRIVACY_CONCERN("개인 정보가 걱정돼요"),
	OTHER("기타(직접 입력)");

	private final String description;

	@JsonCreator
	public static WithdrawalType from(String value) {
		for (WithdrawalType type : WithdrawalType.values()) {
			if (type.name().equalsIgnoreCase(value)) { // 대소문자 구분 없이 처리
				return type;
			}
		}
		throw new DalcoomiException(UNSUPPORTED_WITHDRAWAL_TYPE);
	}
}
