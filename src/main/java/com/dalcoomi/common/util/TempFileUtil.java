package com.dalcoomi.common.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.dalcoomi.common.error.exception.DalcoomiException;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TempFileUtil {

	private static final String TEMP_DIR = System.getProperty("java.io.tmpdir") + "/receipts";

	@PostConstruct
	public void init() {
		try {
			Path tempPath = Paths.get(TEMP_DIR);

			if (!Files.exists(tempPath)) {
				Files.createDirectories(tempPath);

				log.info("임시 파일 디렉토리 생성: {}", TEMP_DIR);
			}
		} catch (IOException e) {
			log.error("임시 파일 디렉토리 생성 실패: {}", TEMP_DIR, e);

			throw new DalcoomiException("임시 파일 디렉토리 생성에 실패했습니다.", e);
		}
	}

	public String saveTempFile(String taskId, MultipartFile file) {
		try {
			String originalFilename = file.getOriginalFilename();
			String extension = "";

			if (originalFilename != null && originalFilename.contains(".")) {
				extension = originalFilename.substring(originalFilename.lastIndexOf("."));
			}

			String fileName = taskId + extension;
			Path filePath = Paths.get(TEMP_DIR, fileName);

			Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

			log.info("임시 파일 저장 완료: {}", filePath);

			return filePath.toString();
		} catch (IOException e) {
			log.error("임시 파일 저장 실패: taskId={}", taskId, e);

			throw new DalcoomiException("임시 파일 저장에 실패했습니다.", e);
		}
	}

	public byte[] readTempFile(String filePath) {
		try {
			Path path = Paths.get(filePath);

			return Files.readAllBytes(path);
		} catch (IOException e) {
			log.error("임시 파일 읽기 실패: {}", filePath, e);

			throw new DalcoomiException("임시 파일을 읽을 수 없습니다.", e);
		}
	}

	public void deleteTempFile(String filePath) {
		try {
			Path path = Paths.get(filePath);

			if (Files.exists(path)) {
				Files.delete(path);

				log.info("임시 파일 삭제 완료: {}", filePath);
			}
		} catch (IOException e) {
			log.warn("임시 파일 삭제 실패: {}", filePath, e);
		}
	}
}
