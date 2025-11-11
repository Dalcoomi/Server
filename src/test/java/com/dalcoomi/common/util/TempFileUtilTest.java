package com.dalcoomi.common.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.dalcoomi.common.error.exception.DalcoomiException;

class TempFileUtilTest {

	private static final String TEMP_DIR = System.getProperty("java.io.tmpdir") + "/receipts";
	private final TempFileUtil tempFileUtil = new TempFileUtil();

	@AfterEach
	void tearDown() throws IOException {
		// 테스트 후 임시 파일 정리
		Path tempPath = Paths.get(TEMP_DIR);

		if (Files.exists(tempPath)) {
			Files.walk(tempPath)
				.filter(Files::isRegularFile)
				.forEach(path -> {
					try {
						Files.deleteIfExists(path);
					} catch (IOException e) {
						// 무시
					}
				});
		}
	}

	@Test
	@DisplayName("임시 파일 저장 성공")
	void save_temp_file_success() {
		// given
		String taskId = "test-task-123";
		MockMultipartFile file = new MockMultipartFile(
			"receipt",
			"test.jpg",
			"image/jpeg",
			"test content".getBytes()
		);

		// when
		String savedPath = tempFileUtil.saveTempFile(taskId, file);

		// then
		assertThat(savedPath).contains("receipts").contains(taskId).endsWith(".jpg");
		assertThat(Files.exists(Paths.get(savedPath))).isTrue();
	}

	@Test
	@DisplayName("확장자가 없는 파일 저장 성공")
	void save_temp_file_without_extension_success() {
		// given
		String taskId = "test-task-456";
		MockMultipartFile file = new MockMultipartFile(
			"receipt",
			"testfile",
			"application/octet-stream",
			"test content".getBytes()
		);

		// when
		String savedPath = tempFileUtil.saveTempFile(taskId, file);

		// then
		assertThat(savedPath).contains(taskId);
		assertThat(Files.exists(Paths.get(savedPath))).isTrue();
	}

	@Test
	@DisplayName("파일명이 null인 경우 저장 성공")
	void save_temp_file_with_null_filename_success() {
		// given
		String taskId = "test-task-789";
		MockMultipartFile file = new MockMultipartFile(
			"receipt",
			null,
			"image/jpeg",
			"test content".getBytes()
		);

		// when
		String savedPath = tempFileUtil.saveTempFile(taskId, file);

		// then
		assertThat(savedPath).contains(taskId);
		assertThat(Files.exists(Paths.get(savedPath))).isTrue();
	}

	@Test
	@DisplayName("같은 taskId로 재저장 시 파일 덮어쓰기 성공")
	void save_temp_file_replace_existing_success() {
		// given
		String taskId = "test-task-replace";
		MockMultipartFile file1 = new MockMultipartFile(
			"receipt",
			"test.jpg",
			"image/jpeg",
			"original content".getBytes()
		);

		MockMultipartFile file2 = new MockMultipartFile(
			"receipt",
			"test.jpg",
			"image/jpeg",
			"new content".getBytes()
		);

		// when
		String savedPath1 = tempFileUtil.saveTempFile(taskId, file1);
		String savedPath2 = tempFileUtil.saveTempFile(taskId, file2);

		// then
		assertThat(savedPath1).isEqualTo(savedPath2);

		byte[] content = tempFileUtil.readTempFile(savedPath2);
		assertThat(new String(content)).isEqualTo("new content");
	}

	@Test
	@DisplayName("임시 파일 읽기 성공")
	void read_temp_file_success() {
		// given
		String taskId = "test-task-read";
		String expectedContent = "test content to read";
		MockMultipartFile file = new MockMultipartFile(
			"receipt",
			"test.jpg",
			"image/jpeg",
			expectedContent.getBytes()
		);

		String savedPath = tempFileUtil.saveTempFile(taskId, file);

		// when
		byte[] content = tempFileUtil.readTempFile(savedPath);

		// then
		assertThat(new String(content)).isEqualTo(expectedContent);
	}

	@Test
	@DisplayName("존재하지 않는 파일 읽기 시 예외 발생")
	void read_non_existent_file_throws_exception_fail() {
		// given
		String nonExistentPath = TEMP_DIR + "/non-existent-file.jpg";

		// when & then
		assertThatThrownBy(() -> tempFileUtil.readTempFile(nonExistentPath))
			.isInstanceOf(DalcoomiException.class)
			.hasMessageContaining("임시 파일을 읽을 수 없습니다");
	}

	@Test
	@DisplayName("임시 파일 삭제 성공")
	void delete_temp_file_success() {
		// given
		String taskId = "test-task-delete";
		MockMultipartFile file = new MockMultipartFile(
			"receipt",
			"test.jpg",
			"image/jpeg",
			"test content".getBytes()
		);

		String savedPath = tempFileUtil.saveTempFile(taskId, file);
		assertThat(Files.exists(Paths.get(savedPath))).isTrue();

		// when
		tempFileUtil.deleteTempFile(savedPath);

		// then
		assertThat(Files.exists(Paths.get(savedPath))).isFalse();
	}

	@Test
	@DisplayName("초기화 시 디렉토리 생성 성공")
	void init_creates_directory_success() {
		// given
		TempFileUtil newTempFileUtil = new TempFileUtil();

		// when
		newTempFileUtil.init();

		// then
		Path tempPath = Paths.get(TEMP_DIR);
		assertThat(Files.exists(tempPath)).isTrue();
		assertThat(Files.isDirectory(tempPath)).isTrue();
	}

	@Test
	@DisplayName("파일 읽기 후 삭제 성공")
	void save_read_delete_success() {
		// given
		String taskId = "full-flow-test";
		String content = "full flow test content";
		MockMultipartFile file = new MockMultipartFile(
			"receipt",
			"flow.jpg",
			"image/jpeg",
			content.getBytes()
		);

		// when
		String savedPath = tempFileUtil.saveTempFile(taskId, file);
		byte[] readContent = tempFileUtil.readTempFile(savedPath);
		tempFileUtil.deleteTempFile(savedPath);

		// then
		assertThat(new String(readContent)).isEqualTo(content);
		assertThat(Files.exists(Paths.get(savedPath))).isFalse();
	}

	@Test
	@DisplayName("디렉토리가 이미 존재하는 경우 init 성공")
	void init_when_directory_already_exists_success() throws IOException {
		// given
		Path tempPath = Paths.get(TEMP_DIR);

		if (!Files.exists(tempPath)) {
			Files.createDirectories(tempPath);
		}

		TempFileUtil newTempFileUtil = new TempFileUtil();

		// when & then
		newTempFileUtil.init();
		assertThat(Files.exists(tempPath)).isTrue();
	}

	@Test
	@DisplayName("파일 저장 중 IOException 발생 시 예외 처리")
	void save_temp_file_throws_exception_on_io_error_fail() throws IOException {
		// given
		String taskId = "exception-test";
		MultipartFile mockFile = mock(MultipartFile.class);

		given(mockFile.getOriginalFilename()).willReturn("test.jpg");
		given(mockFile.getInputStream()).willThrow(new IOException("File read error"));

		// when & then
		assertThatThrownBy(() -> tempFileUtil.saveTempFile(taskId, mockFile))
			.isInstanceOf(DalcoomiException.class)
			.hasMessageContaining("임시 파일 저장에 실패했습니다");
	}

	@Test
	@DisplayName("임시 파일 읽기 중 IOException 발생 시 예외 처리")
	void read_temp_file_throws_exception_on_io_error_fail() {
		// given
		String invalidPath = TEMP_DIR + "/invalid/../../../etc/passwd"; // 경로 조작 시도

		// when & then
		assertThatThrownBy(() -> tempFileUtil.readTempFile(invalidPath))
			.isInstanceOf(DalcoomiException.class)
			.hasMessageContaining("임시 파일을 읽을 수 없습니다");
	}
}
