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
	
	@PostMapping
	public DummyDataResponse generateDummyData(@RequestParam(defaultValue = "member") String type,
		@RequestParam(defaultValue = "1") int count) {
		return dummyService.generateDummyData(type, count);
	}
}
