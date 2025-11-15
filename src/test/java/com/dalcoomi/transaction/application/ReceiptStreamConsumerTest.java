package com.dalcoomi.transaction.application;

import static com.dalcoomi.transaction.constant.ReceiptStreamConstants.CONSUMER_GROUP;
import static com.dalcoomi.transaction.constant.ReceiptStreamConstants.FIELD_CATEGORY_NAMES;
import static com.dalcoomi.transaction.constant.ReceiptStreamConstants.FIELD_FILE_PATH;
import static com.dalcoomi.transaction.constant.ReceiptStreamConstants.FIELD_TASK_ID;
import static com.dalcoomi.transaction.constant.ReceiptStreamConstants.PROCESSING_KEY;
import static com.dalcoomi.transaction.constant.ReceiptStreamConstants.STREAM_KEY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.times;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import com.dalcoomi.common.util.TempFileUtil;

import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class ReceiptStreamConsumerTest {

	@InjectMocks
	private ReceiptStreamConsumer receiptStreamConsumer;

	@Mock
	private StringRedisTemplate stringRedisTemplate;

	@Mock
	private TempFileUtil tempFileUtil;

	@Mock
	private WebClient webClient;

	@Mock
	private StreamOperations<String, Object, Object> streamOperations;

	@Mock
	private ValueOperations<String, String> valueOperations;

	@Mock
	private WebClient.RequestBodyUriSpec requestBodyUriSpec;

	@Mock
	private WebClient.RequestBodySpec requestBodySpec;

	@Mock
	private WebClient.RequestHeadersSpec requestHeadersSpec;

	@Mock
	private WebClient.ResponseSpec responseSpec;

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(receiptStreamConsumer, "aiServerUrl", "http://localhost:8080");
		ReflectionTestUtils.setField(receiptStreamConsumer, "aiServerApiKey", "test-api-key");
	}

	@Test
	@DisplayName("처리 중인 메시지가 있으면 대기 성공")
	void on_message_when_processing_then_skip_success() {
		// given
		Map<Object, Object> messageBody = new HashMap<>();
		messageBody.put(FIELD_TASK_ID, "receipt-test-123");
		messageBody.put(FIELD_FILE_PATH, "/tmp/receipts/receipt-test-123.jpg");
		messageBody.put(FIELD_CATEGORY_NAMES, "[\"식비\"]");

		MapRecord<String, Object, Object> message = StreamRecords.newRecord()
			.in(STREAM_KEY)
			.ofMap(messageBody)
			.withId(RecordId.of("1234567890-0"));

		given(stringRedisTemplate.hasKey(PROCESSING_KEY)).willReturn(true);

		// when
		receiptStreamConsumer.onMessage(message);

		// then
		then(stringRedisTemplate).should(times(1)).hasKey(PROCESSING_KEY);
		then(stringRedisTemplate).should(times(0)).opsForStream();
	}

	@Test
	@DisplayName("Redis Stream 메시지 처리 성공")
	void consume_messages_success() {
		// given
		String taskId = "receipt-test-123";
		String filePath = "/tmp/receipts/receipt-test-123.jpg";
		String categoryNamesJson = "[\"식비\",\"카페\"]";
		String messageId = "1234567890-0";

		Map<Object, Object> messageBody = new HashMap<>();
		messageBody.put(FIELD_TASK_ID, taskId);
		messageBody.put(FIELD_FILE_PATH, filePath);
		messageBody.put(FIELD_CATEGORY_NAMES, categoryNamesJson);

		MapRecord<String, Object, Object> message = StreamRecords.newRecord()
			.in(STREAM_KEY)
			.ofMap(messageBody)
			.withId(RecordId.of(messageId));

		given(stringRedisTemplate.hasKey(PROCESSING_KEY)).willReturn(false);
		given(stringRedisTemplate.opsForStream()).willReturn(streamOperations);
		given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);
		given(tempFileUtil.readTempFile(filePath)).willReturn("test file content".getBytes());
		willDoNothing().given(tempFileUtil).deleteTempFile(anyString());
		given(streamOperations.acknowledge(anyString(), anyString(), anyString())).willReturn(1L);
		willDoNothing().given(valueOperations).set(anyString(), anyString(), any());

		// WebClient Mock 설정 - 실제 Mono 체인이 동작하도록
		Mono<String> actualMono = Mono.just("{\"success\":true}");
		given(webClient.post()).willReturn(requestBodyUriSpec);
		given(requestBodyUriSpec.uri(anyString())).willReturn(requestBodySpec);
		given(requestBodySpec.header(anyString(), anyString())).willReturn(requestBodySpec);
		given(requestBodySpec.contentType(any())).willReturn(requestBodySpec);
		given(requestBodySpec.bodyValue(any())).willReturn(requestHeadersSpec);
		given(requestHeadersSpec.retrieve()).willReturn(responseSpec);
		given(responseSpec.onStatus(any(), any())).willReturn(responseSpec);
		given(responseSpec.bodyToMono(String.class)).willReturn(actualMono);

		// when
		receiptStreamConsumer.onMessage(message);

		// then
		then(tempFileUtil).should(times(1)).readTempFile(filePath);
		then(tempFileUtil).should(times(1)).deleteTempFile(filePath);
		then(streamOperations).should(times(1)).acknowledge(eq(STREAM_KEY), eq(CONSUMER_GROUP), eq(messageId));
	}

	@Test
	@DisplayName("메시지 처리 중 예외 발생 시에도 ACK 처리 성공")
	void consume_messages_with_exception_still_acks_success() {
		// given
		String taskId = "receipt-test-456";
		String filePath = "/tmp/receipts/receipt-test-456.jpg";
		String categoryNamesJson = "[\"식비\"]";

		Map<Object, Object> messageBody = new HashMap<>();
		messageBody.put(FIELD_TASK_ID, taskId);
		messageBody.put(FIELD_FILE_PATH, filePath);
		messageBody.put(FIELD_CATEGORY_NAMES, categoryNamesJson);

		MapRecord<String, Object, Object> message = StreamRecords.newRecord()
			.in(STREAM_KEY)
			.ofMap(messageBody)
			.withId(RecordId.of("1234567891-0"));

		given(stringRedisTemplate.hasKey(PROCESSING_KEY)).willReturn(false);
		given(stringRedisTemplate.opsForStream()).willReturn(streamOperations);
		given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);
		willThrow(new RuntimeException("파일 읽기 실패")).given(tempFileUtil).readTempFile(filePath);
		willDoNothing().given(tempFileUtil).deleteTempFile(anyString());
		given(streamOperations.acknowledge(anyString(), anyString(), anyString())).willReturn(1L);

		// when
		receiptStreamConsumer.onMessage(message);

		// then
		then(tempFileUtil).should(times(1)).deleteTempFile(filePath); // 예외 발생 시에도 삭제
		then(streamOperations).should(times(1)).acknowledge(eq(STREAM_KEY), eq(CONSUMER_GROUP), anyString()); // ACK 처리
	}

	@Test
	@DisplayName("NOGROUP 에러 발생 시 Consumer Group 재생성 성공")
	void onMessage_nogroup_error_recreate_consumer_group_success() {
		// given
		Map<Object, Object> messageBody = new HashMap<>();
		messageBody.put(FIELD_TASK_ID, "receipt-test-789");
		messageBody.put(FIELD_FILE_PATH, "/tmp/receipts/receipt-test-789.jpg");
		messageBody.put(FIELD_CATEGORY_NAMES, "[\"식비\"]");

		MapRecord<String, Object, Object> message = StreamRecords.newRecord()
			.in(STREAM_KEY)
			.ofMap(messageBody)
			.withId(RecordId.of("1234567892-0"));

		given(stringRedisTemplate.hasKey(PROCESSING_KEY))
			.willThrow(new RuntimeException("NOGROUP No such consumer group"));
		given(stringRedisTemplate.opsForStream()).willReturn(streamOperations);

		// when
		receiptStreamConsumer.onMessage(message);

		// then
		then(streamOperations).should(times(1)).createGroup(eq(STREAM_KEY), any(), eq(CONSUMER_GROUP));
	}

	@Test
	@DisplayName("AI 서버 에러 응답 시에도 ACK 처리 및 임시 파일 삭제 성공")
	void consume_messages_with_ai_server_error_success() {
		// given
		String taskId = "receipt-test-500";
		String filePath = "/tmp/receipts/receipt-test-500.jpg";
		String categoryNamesJson = "[\\\"식비\\\"]";

		Map<Object, Object> messageBody = new HashMap<>();
		messageBody.put(FIELD_TASK_ID, taskId);
		messageBody.put(FIELD_FILE_PATH, filePath);
		messageBody.put(FIELD_CATEGORY_NAMES, categoryNamesJson);

		MapRecord<String, Object, Object> message = StreamRecords.newRecord()
			.in(STREAM_KEY)
			.ofMap(messageBody)
			.withId(RecordId.of("1234567893-0"));

		given(stringRedisTemplate.hasKey(PROCESSING_KEY)).willReturn(false);
		given(stringRedisTemplate.opsForStream()).willReturn(streamOperations);
		given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);
		given(tempFileUtil.readTempFile(filePath)).willReturn("test file content".getBytes());
		willDoNothing().given(tempFileUtil).deleteTempFile(anyString());
		given(streamOperations.acknowledge(anyString(), anyString(), anyString())).willReturn(1L);

		// WebClient Mock 설정 - 에러 응답
		given(webClient.post()).willReturn(requestBodyUriSpec);
		given(requestBodyUriSpec.uri(anyString())).willReturn(requestBodySpec);
		given(requestBodySpec.header(anyString(), anyString())).willReturn(requestBodySpec);
		given(requestBodySpec.contentType(any())).willReturn(requestBodySpec);
		given(requestBodySpec.bodyValue(any())).willReturn(requestHeadersSpec);
		given(requestHeadersSpec.retrieve()).willReturn(responseSpec);
		given(responseSpec.onStatus(any(), any())).willReturn(responseSpec);
		given(responseSpec.bodyToMono(String.class)).willReturn(Mono.error(new RuntimeException("AI 서버 오류")));

		// when
		receiptStreamConsumer.onMessage(message);

		// then
		then(tempFileUtil).should(times(1)).readTempFile(filePath);
		then(tempFileUtil).should(times(1)).deleteTempFile(filePath); // 에러 발생 시에도 삭제
		then(streamOperations).should(times(1)).acknowledge(eq(STREAM_KEY), eq(CONSUMER_GROUP), anyString()); // ACK 처리
	}

	@Test
	@DisplayName("NOGROUP 에러 발생 시 Consumer Group 재생성 실패")
	void onMessage_nogroup_error_recreate_consumer_group_fail() {
		// given
		Map<Object, Object> messageBody = new HashMap<>();
		messageBody.put(FIELD_TASK_ID, "receipt-test-999");
		messageBody.put(FIELD_FILE_PATH, "/tmp/receipts/receipt-test-999.jpg");
		messageBody.put(FIELD_CATEGORY_NAMES, "[\"식비\"]");

		MapRecord<String, Object, Object> message = StreamRecords.newRecord()
			.in(STREAM_KEY)
			.ofMap(messageBody)
			.withId(RecordId.of("1234567894-0"));

		given(stringRedisTemplate.hasKey(PROCESSING_KEY)).willThrow(
			new RuntimeException("NOGROUP No such consumer group"));
		given(stringRedisTemplate.opsForStream()).willReturn(streamOperations);
		willThrow(new RuntimeException("Consumer Group 생성 실패")).given(streamOperations)
			.createGroup(anyString(), any(), anyString());

		// when
		receiptStreamConsumer.onMessage(message);

		// then
		then(streamOperations).should(times(1)).createGroup(eq(STREAM_KEY), any(), eq(CONSUMER_GROUP));
	}

	@Test
	@DisplayName("일반 예외 발생 시 로그만 출력 성공")
	void on_message_general_exception_success() {
		// given
		Map<Object, Object> messageBody = new HashMap<>();
		messageBody.put(FIELD_TASK_ID, "receipt-test-888");
		messageBody.put(FIELD_FILE_PATH, "/tmp/receipts/receipt-test-888.jpg");
		messageBody.put(FIELD_CATEGORY_NAMES, "[\"식비\"]");

		MapRecord<String, Object, Object> message = StreamRecords.newRecord()
			.in(STREAM_KEY)
			.ofMap(messageBody)
			.withId(RecordId.of("1234567895-0"));

		given(stringRedisTemplate.hasKey(PROCESSING_KEY)).willThrow(new RuntimeException("일반 Redis 오류"));

		// when
		receiptStreamConsumer.onMessage(message);

		// then
		then(stringRedisTemplate).should(times(1)).hasKey(PROCESSING_KEY);
	}
}
