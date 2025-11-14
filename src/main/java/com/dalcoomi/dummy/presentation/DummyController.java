package com.dalcoomi.dummy.presentation;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dalcoomi.dummy.application.DummyService;
import com.dalcoomi.dummy.dto.DummyDataResponse;

import lombok.RequiredArgsConstructor;

@Profile({"local", "dev"})
@RestController
@RequestMapping("/api/dev/dummies")
@RequiredArgsConstructor
public class DummyController {

	private final DummyService dummyService;

	/**
	 * 더미 데이터 생성 API
	 * 타입에 따라 회원, 카테고리, 거래 내역을 생성합니다.
	 *
	 * @param type 생성할 데이터 타입 (member, category, transaction)
	 * @param count 생성할 개수 (member, category는 무시되고 1개만 생성, transaction은 지정한 개수만큼 생성)
	 * @return 생성 결과
	 */
	@PostMapping
	public DummyDataResponse generateDummyData(@RequestParam(defaultValue = "member") String type,
		@RequestParam(defaultValue = "1") int count) {
		return dummyService.generateDummyData(type, count);
	}
}
