package com.dalcoomi.transaction.application;

import static com.dalcoomi.transaction.constant.ReceiptStreamConstants.CONSUMER_GROUP;
import static com.dalcoomi.transaction.constant.ReceiptStreamConstants.FIELD_CATEGORY_NAMES;
import static com.dalcoomi.transaction.constant.ReceiptStreamConstants.FIELD_FILE_PATH;
import static com.dalcoomi.transaction.constant.ReceiptStreamConstants.FIELD_TASK_ID;
import static com.dalcoomi.transaction.constant.ReceiptStreamConstants.PROCESSING_KEY;
import static com.dalcoomi.transaction.constant.ReceiptStreamConstants.STREAM_KEY;
import static java.util.Objects.requireNonNull;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import com.dalcoomi.common.util.TempFileUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@DependsOn("receiptStreamProducer")
@RequiredArgsConstructor
public class ReceiptStreamConsumer implements StreamListener<String, MapRecord<String, Object, Object>> {

	private final StringRedisTemplate stringRedisTemplate;
	private final TempFileUtil tempFileUtil;
	private final WebClient webClient;

	@Value("${ai.server.url}")
	private String aiServerUrl;

	@Value("${ai.server.api-key}")
	private String aiServerApiKey;

	@Override
	public void onMessage(MapRecord<String, Object, Object> message) {
		try {
			// 현재 처리 중인 영수증이 있는지 체크
			boolean isProcessing = requireNonNull(stringRedisTemplate.hasKey(PROCESSING_KEY));

			if (isProcessing) {
				log.debug("이미 처리 중인 영수증이 있어 대기: messageId={}", message.getId().getValue());
				return;
			}

			processMessage(message);
		} catch (Exception e) {
			// Consumer Group이 없는 경우 재생성 시도
			if (e.getMessage().contains("NOGROUP")) {
				log.warn("Consumer Group이 존재하지 않아 재생성 시도");

				try {
					stringRedisTemplate.opsForStream().createGroup(STREAM_KEY, ReadOffset.from("0"), CONSUMER_GROUP);

					log.info("Consumer Group 재생성 완료: stream={}, group={}", STREAM_KEY, CONSUMER_GROUP);
				} catch (Exception ex) {
					log.error("Consumer Group 재생성 실패", ex);
				}
			} else {
				log.error("Redis Stream 메시지 소비 중 오류 발생", e);
			}
		}
	}

	private void processMessage(MapRecord<String, Object, Object> message) {
		String messageId = message.getId().getValue();
		String taskId = (String)message.getValue().get(FIELD_TASK_ID);
		String filePath = (String)message.getValue().get(FIELD_FILE_PATH);
		String categoryNamesJson = (String)message.getValue().get(FIELD_CATEGORY_NAMES);

		try {
			// 처리 시작 플래그 설정 (taskId 저장, 1분 TTL)
			stringRedisTemplate.opsForValue().set(PROCESSING_KEY, taskId, Duration.ofMinutes(1));
			log.info("영수증 처리 시작: taskId={}, messageId={}", taskId, messageId);

			int lastSeparator = Math.max(filePath.lastIndexOf('/'), filePath.lastIndexOf('\\'));
			String originalFilename = lastSeparator >= 0 ? filePath.substring(lastSeparator + 1) : filePath;

			byte[] receiptBytes = tempFileUtil.readTempFile(filePath);
			ByteArrayResource receiptResource = new ByteArrayResource(receiptBytes) {
				@Override
				public String getFilename() {
					return originalFilename;
				}
			};

			MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
			parts.add("taskId", taskId);
			parts.add("receipt", receiptResource);
			parts.add("categories", categoryNamesJson);

			String response = webClient.post()
				.uri(aiServerUrl + "/receipt")
				.header("X-API-Key", aiServerApiKey)
				.contentType(MULTIPART_FORM_DATA)
				.bodyValue(parts)
				.retrieve()
				.onStatus(HttpStatusCode::isError, clientResponse -> {
					log.error("AI 서버 요청 실패: status={}", clientResponse.statusCode());

					return Mono.error(new RuntimeException("AI 서버 처리 중 오류가 발생했습니다."));
				})
				.bodyToMono(String.class)
				.timeout(Duration.ofSeconds(90))
				.block();

			log.info("AI 서버 전송 완료: taskId={}, response={}", taskId, response);

			tempFileUtil.deleteTempFile(filePath);

			stringRedisTemplate.opsForStream().acknowledge(STREAM_KEY, CONSUMER_GROUP, messageId);

			log.info("메시지 ACK 완료: taskId={}, messageId={}", taskId, messageId);
		} catch (Exception e) {
			log.error("영수증 처리 실패: taskId={}, messageId={}", taskId, messageId, e);

			if (filePath != null) {
				tempFileUtil.deleteTempFile(filePath);
			}

			stringRedisTemplate.opsForStream().acknowledge(STREAM_KEY, CONSUMER_GROUP, messageId);

			// 실패 시 플래그 제거 (다음 메시지 처리 가능하도록)
			stringRedisTemplate.delete(PROCESSING_KEY);
		}
	}
}
