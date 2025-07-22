package com.dalcoomi.common.presentation;

import static org.springframework.http.HttpStatus.OK;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class HealthController {

	@GetMapping("/health")
	@ResponseStatus(OK)
	public String health() {
		return "OK";
	}
}
