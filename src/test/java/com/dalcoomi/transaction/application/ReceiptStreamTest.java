package com.dalcoomi.transaction.application;

import static com.dalcoomi.transaction.constant.ReceiptStreamConstants.STREAM_KEY;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import com.dalcoomi.AbstractContainerBaseTest;

@Transactional
@SpringBootTest
@TestPropertySource("classpath:application-test.properties")
@AutoConfigureMockMvc(addFilters = false)
class ReceiptStreamTest extends AbstractContainerBaseTest {

	@Autowired
	private ReceiptStreamProducer receiptStreamProducer;

	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	@Test
	@DisplayName("Redis Streams에 영수증 작업 발행하고 taskId 반환 성공")
	void publish_receipt_task_success() {
		// given
		Long memberId = 1L;
		MockMultipartFile receipt = new MockMultipartFile(
			"receipt",
			"test-receipt.jpg",
			"image/jpeg",
			"test image content".getBytes()
		);

		// when
		String taskId = receiptStreamProducer.publishReceiptTask(memberId, null, receipt);

		// then
		assertThat(taskId).isNotNull().startsWith("receipt-");

		Long streamLength = stringRedisTemplate.opsForStream().size(STREAM_KEY);
		assertThat(streamLength).isGreaterThan(0);
	}
}
