package com.dalcoomi.common.util.lock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class ReceiptLockKeyGeneratorTest {

	private final ReceiptLockKeyGenerator receiptLockKeyGenerator = new ReceiptLockKeyGenerator();

	@Test
	@DisplayName("개인 영수증 업로드 락 키 생성 성공")
	void generate_personal_upload_lock_key_with_hash_success() {
		// given
		Long memberId = 1L;
		MockMultipartFile receipt = new MockMultipartFile(
			"receipt",
			"test.jpg",
			"image/jpeg",
			"test content".getBytes()
		);

		// when
		String lockKey1 = receiptLockKeyGenerator.generateUploadLockKey(memberId, null, receipt);
		String lockKey2 = receiptLockKeyGenerator.generateUploadLockKey(memberId, null, receipt);

		// then
		assertThat(lockKey1).startsWith("receipt:upload:1:personal:").isEqualTo(lockKey2);
	}

	@Test
	@DisplayName("그룹 영수증 업로드 락 키 생성 성공")
	void generate_team_upload_lock_key_with_hash_success() {
		// given
		Long memberId = 1L;
		Long teamId = 10L;
		MockMultipartFile receipt = new MockMultipartFile(
			"receipt",
			"team-receipt.jpg",
			"image/jpeg",
			"team content".getBytes()
		);

		// when
		String lockKey = receiptLockKeyGenerator.generateUploadLockKey(memberId, teamId, receipt);

		// then
		assertThat(lockKey).startsWith("receipt:upload:1:10:").contains("10");
	}

	@Test
	@DisplayName("동일한 내용의 파일은 동일한 락 키 생성 성공")
	void same_file_content_generates_same_key_success() {
		// given
		Long memberId = 1L;
		byte[] content = "identical content".getBytes();

		MockMultipartFile receipt1 = new MockMultipartFile(
			"receipt",
			"file1.jpg",
			"image/jpeg",
			content
		);

		MockMultipartFile receipt2 = new MockMultipartFile(
			"receipt",
			"file2.jpg", // 다른 파일명
			"image/jpeg",
			content
		);

		// when
		String lockKey1 = receiptLockKeyGenerator.generateUploadLockKey(memberId, null, receipt1);
		String lockKey2 = receiptLockKeyGenerator.generateUploadLockKey(memberId, null, receipt2);

		// then
		assertThat(lockKey1).isEqualTo(lockKey2); // 파일명이 달라도 내용이 같으면 같은 키
	}

	@Test
	@DisplayName("다른 내용의 파일은 다른 락 키 생성 성공")
	void different_file_content_generates_different_key_success() {
		// given
		Long memberId = 1L;

		MockMultipartFile receipt1 = new MockMultipartFile(
			"receipt",
			"test.jpg",
			"image/jpeg",
			"content1".getBytes()
		);

		MockMultipartFile receipt2 = new MockMultipartFile(
			"receipt",
			"test.jpg",
			"image/jpeg",
			"content2".getBytes()
		);

		// when
		String lockKey1 = receiptLockKeyGenerator.generateUploadLockKey(memberId, null, receipt1);
		String lockKey2 = receiptLockKeyGenerator.generateUploadLockKey(memberId, null, receipt2);

		// then
		assertThat(lockKey1).isNotEqualTo(lockKey2);
	}

	@Test
	@DisplayName("다른 회원은 같은 파일이어도 다른 락 키 생성 성공")
	void different_member_generates_different_key_success() {
		// given
		Long memberId1 = 1L;
		Long memberId2 = 2L;

		MockMultipartFile receipt = new MockMultipartFile(
			"receipt",
			"test.jpg",
			"image/jpeg",
			"same content".getBytes()
		);

		// when
		String lockKey1 = receiptLockKeyGenerator.generateUploadLockKey(memberId1, null, receipt);
		String lockKey2 = receiptLockKeyGenerator.generateUploadLockKey(memberId2, null, receipt);

		// then
		assertThat(lockKey1).isNotEqualTo(lockKey2);
	}

	@Test
	@DisplayName("영수증 저장 락 키 생성 성공")
	void generate_save_lock_key_success() {
		// given
		Long memberId = 1L;
		String taskId = "receipt-123-456";

		// when
		String lockKey = receiptLockKeyGenerator.generateSaveLockKey(memberId, taskId);

		// then
		assertThat(lockKey).isEqualTo("receipt:save:1:receipt-123-456");
	}

	@Test
	@DisplayName("다른 taskId는 다른 저장 락 키 생성 성공")
	void different_task_id_generates_different_save_key_success() {
		// given
		Long memberId = 1L;
		String taskId1 = "receipt-123";
		String taskId2 = "receipt-456";

		// when
		String lockKey1 = receiptLockKeyGenerator.generateSaveLockKey(memberId, taskId1);
		String lockKey2 = receiptLockKeyGenerator.generateSaveLockKey(memberId, taskId2);

		// then
		assertThat(lockKey1).isNotEqualTo(lockKey2);
	}

	@Test
	@DisplayName("개인과 그룹 영수증은 다른 락 키 생성 성공")
	void personal_and_team_receipt_generate_different_keys_success() {
		// given
		Long memberId = 1L;
		Long teamId = 10L;

		MockMultipartFile receipt = new MockMultipartFile(
			"receipt",
			"test.jpg",
			"image/jpeg",
			"content".getBytes()
		);

		// when
		String personalKey = receiptLockKeyGenerator.generateUploadLockKey(memberId, null, receipt);
		String teamKey = receiptLockKeyGenerator.generateUploadLockKey(memberId, teamId, receipt);

		// then
		assertThat(personalKey).contains("personal");
		assertThat(teamKey).contains("10");
		assertThat(personalKey).isNotEqualTo(teamKey);
	}

	@Test
	@DisplayName("개인 영수증 업로드 기능 파일 해시 생성 실패 시 파일명과 크기로 대체 키 생성 성공")
	void fallback_key_generation_for_personal_receipt_success() throws IOException {
		// given
		Long memberId = 1L;
		MultipartFile mockFile = mock(MultipartFile.class);

		given(mockFile.getBytes()).willThrow(new IOException("File read error"));
		given(mockFile.getOriginalFilename()).willReturn("fallback-test.jpg");
		given(mockFile.getSize()).willReturn(1024L);

		// when
		String lockKey = receiptLockKeyGenerator.generateUploadLockKey(memberId, null, mockFile);

		// then
		assertThat(lockKey).isEqualTo("receipt:upload:1:personal:fallback-test.jpg:1024");
	}

	@Test
	@DisplayName("그룹 영수증 업로드 기능 그룹 파일 해시 생성 실패 시 파일명과 크기로 대체 키 생성 성공")
	void fallback_key_generation_for_team_receipt_success() throws IOException {
		// given
		Long memberId = 1L;
		Long teamId = 5L;
		MultipartFile mockFile = mock(MultipartFile.class);

		given(mockFile.getBytes()).willThrow(new IOException("File read error"));
		given(mockFile.getOriginalFilename()).willReturn("team-fallback.jpg");
		given(mockFile.getSize()).willReturn(2048L);

		// when
		String lockKey = receiptLockKeyGenerator.generateUploadLockKey(memberId, teamId, mockFile);

		// then
		assertThat(lockKey).isEqualTo("receipt:upload:1:5:team-fallback.jpg:2048");
	}
}
