package com.dalcoomi.transaction.application;

import static com.dalcoomi.transaction.constant.ReceiptStreamConstants.CONSUMER_GROUP;
import static com.dalcoomi.transaction.constant.ReceiptStreamConstants.FIELD_CATEGORY_NAMES;
import static com.dalcoomi.transaction.constant.ReceiptStreamConstants.FIELD_FILE_PATH;
import static com.dalcoomi.transaction.constant.ReceiptStreamConstants.FIELD_TASK_ID;
import static com.dalcoomi.transaction.constant.ReceiptStreamConstants.STREAM_KEY;
import static java.util.Objects.requireNonNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.dalcoomi.category.application.CategoryService;
import com.dalcoomi.common.error.exception.DalcoomiException;
import com.dalcoomi.common.util.TempFileUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReceiptStreamProducer {

	private final StringRedisTemplate stringRedisTemplate;
	private final ObjectMapper objectMapper;
	private final TempFileUtil tempFileUtil;
	private final CategoryService categoryService;

	@PostConstruct
	public void init() {
		try {
			// Consumer Group 생성 (스트림이 없으면 자동 생성)
			stringRedisTemplate.opsForStream().createGroup(STREAM_KEY, ReadOffset.from("0"), CONSUMER_GROUP);

			log.info("Redis Streams Consumer Group 생성: stream={}, group={}", STREAM_KEY, CONSUMER_GROUP);
		} catch (Exception e) {
			log.info("Consumer Group 이미 존재: {}", e.getMessage());
		}
	}

	public String publishReceiptTask(Long memberId, @Nullable Long teamId, MultipartFile receipt) {
		String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
		String taskId = "receipt-" + timestamp + "-" + UUID.randomUUID().toString().substring(0, 8);
		String filePath = tempFileUtil.saveTempFile(taskId, receipt);
		List<String> categoryNames = categoryService.fetchCategoryNames(memberId, teamId);

		try {
			Map<String, String> messageData = new HashMap<>();
			messageData.put(FIELD_TASK_ID, taskId);
			messageData.put(FIELD_FILE_PATH, filePath);
			messageData.put(FIELD_CATEGORY_NAMES, objectMapper.writeValueAsString(categoryNames));

			ObjectRecord<String, Map<String, String>> streamRecord = StreamRecords.newRecord()
				.ofObject(messageData)
				.withStreamKey(STREAM_KEY);

			String messageId = requireNonNull(stringRedisTemplate.opsForStream().add(streamRecord)).getValue();

			log.info("Redis Stream에 영수증 작업 발행: taskId={}, messageId={}", taskId, messageId);

			return taskId;
		} catch (Exception e) {
			log.error("Redis Stream 발행 실패: taskId={}", taskId, e);

			throw new DalcoomiException("영수증 작업 발행에 실패했습니다.", e);
		}
	}
}
