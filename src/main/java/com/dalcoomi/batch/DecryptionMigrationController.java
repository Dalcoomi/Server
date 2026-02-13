package com.dalcoomi.batch;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@Profile({"local", "dev", "prod"})
@RestController
@RequestMapping("/api/dev/migration")
@RequiredArgsConstructor
public class DecryptionMigrationController {

	private final DecryptionMigrationBatchService decryptionMigrationBatchService;

	@PostMapping("/decrypt")
	public String runDecryptionMigration() {
		decryptionMigrationBatchService.decryptAllData();

		return "복호화 마이그레이션 완료";
	}
}
