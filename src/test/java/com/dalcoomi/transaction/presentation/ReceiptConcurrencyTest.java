package com.dalcoomi.transaction.presentation;

import static com.dalcoomi.common.error.model.ErrorMessage.LOCK_EXIST_ERROR;
import static com.dalcoomi.transaction.domain.TransactionType.EXPENSE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.dalcoomi.AbstractContainerBaseTest;
import com.dalcoomi.auth.filter.CustomUserDetails;
import com.dalcoomi.category.application.CategoryService;
import com.dalcoomi.category.application.repository.CategoryRepository;
import com.dalcoomi.category.domain.Category;
import com.dalcoomi.fixture.CategoryFixture;
import com.dalcoomi.fixture.MemberFixture;
import com.dalcoomi.member.application.repository.MemberRepository;
import com.dalcoomi.member.domain.Member;
import com.dalcoomi.transaction.application.TransactionService;
import com.dalcoomi.transaction.dto.ReceiptInfo;
import com.dalcoomi.transaction.dto.request.SaveReceiptRequest;
import com.dalcoomi.transaction.dto.request.TransactionRequest;
import com.dalcoomi.transaction.dto.response.AiReceiptResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

@Transactional
@SpringBootTest
@TestPropertySource("classpath:application-test.properties")
@AutoConfigureMockMvc(addFilters = false)
class ReceiptConcurrencyTest extends AbstractContainerBaseTest {

	private final GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private TransactionService transactionService;

	@MockitoBean
	private CategoryService categoryService;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	private ExecutorService executorService;

	@BeforeEach
	void setUp() {
		executorService = Executors.newFixedThreadPool(10);
	}

	@AfterEach
	void tearDown() {
		executorService.shutdown();
	}

	@Test
	@DisplayName("동시성 테스트 - 동일한 영수증 업로드 요청 시 하나만 성공")
	void upload_one_receipt_success() {
		// given
		Long memberId = 1L;
		String taskId = "1-1";
		List<ReceiptInfo> mockReceiptInfos = Arrays.asList(
			ReceiptInfo.builder()
				.date(LocalDate.of(2025, 1, 23))
				.categoryName("카페")
				.content("커피")
				.amount(4800L)
				.build(),
			ReceiptInfo.builder()
				.date(LocalDate.of(2025, 1, 23))
				.categoryName("식비")
				.content("칼국수")
				.amount(12000L)
				.build()
		);
		AiReceiptResponse response = AiReceiptResponse.builder().taskId(taskId).transactions(mockReceiptInfos).build();

		// Mock 설정
		given(categoryService.fetchCategoryNames(eq(memberId), isNull())).willReturn(Arrays.asList("카페", "식비"));
		given(transactionService.analyseReceipt(any(MultipartFile.class), any(List.class))).willReturn(response);

		MockMultipartFile receipt = new MockMultipartFile(
			"receipt",
			"test-receipt.jpg",
			"image/jpeg",
			"test receipt content".getBytes()
		);

		// 인증 설정
		setAuthentication(memberId);

		int threadCount = 5;
		CountDownLatch startLatch = new CountDownLatch(1); // 시작 신호
		CountDownLatch readyLatch = new CountDownLatch(threadCount); // 준비 완료 신호
		List<Future<ResultActions>> futures = new ArrayList<>();

		// when
		for (int i = 0; i < threadCount; i++) {
			Future<ResultActions> future = executorService.submit(() -> {
				try {
					readyLatch.countDown(); // 준비 완료 신호
					startLatch.await(); // 모든 스레드가 동시에 시작하도록 대기

					return mockMvc.perform(multipart("/api/transactions/receipts/upload")
						.file(receipt)
						.contentType(MULTIPART_FORM_DATA));
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			});
			futures.add(future);
		}

		await().atMost(Duration.ofSeconds(5)).until(() -> readyLatch.getCount() == 0);

		// 모든 스레드 동시 시작
		startLatch.countDown();

		// then
		List<Integer> statusCodes = await()
			.atMost(Duration.ofSeconds(10))
			.until(() -> futures.stream()
				.map(future -> {
					try {
						return future.get(100, TimeUnit.MILLISECONDS)
							.andReturn().getResponse().getStatus();
					} catch (Exception e) {
						return null;
					}
				})
				.filter(Objects::nonNull)
				.toList(), hasSize(threadCount));

		long successCount = statusCodes.stream().filter(status -> status == 200).count();
		long conflictCount = statusCodes.stream().filter(status -> status == 409).count();

		assertThat(successCount).isEqualTo(1);
		assertThat(conflictCount).isEqualTo(threadCount - 1);
	}

	@Test
	@DisplayName("동시성 테스트 - 동일한 taskId 저장 요청 시 하나만 성공")
	void save_one_task_id_success() {
		// given
		Member member = MemberFixture.getMember1();
		member = memberRepository.save(member);

		Category category = CategoryFixture.getCategory1(member);
		category = categoryRepository.save(category);

		String taskId = "1-1";

		List<TransactionRequest> transactionRequests = Arrays.asList(
			new TransactionRequest(null, 4800L, "커피", LocalDateTime.of(2025, 1, 23, 10, 30), EXPENSE, category.getId()),
			new TransactionRequest(null, 12000L, "칼국수", LocalDateTime.of(2025, 1, 23, 12, 0), EXPENSE, category.getId())
		);

		SaveReceiptRequest saveRequest = SaveReceiptRequest.builder()
			.taskId(taskId)
			.transactions(transactionRequests)
			.build();

		// 인증 설정
		setAuthentication(member.getId());

		int threadCount = 5;
		CountDownLatch latch = new CountDownLatch(threadCount);
		List<Future<ResultActions>> futures = new ArrayList<>();

		// when
		for (int i = 0; i < threadCount; i++) {
			Future<ResultActions> future = executorService.submit(() -> {
				try {
					latch.countDown();
					latch.await();

					return mockMvc.perform(post("/api/transactions/receipts/save")
						.content(objectMapper.writeValueAsString(saveRequest))
						.contentType(APPLICATION_JSON));
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			});

			futures.add(future);
		}

		// then
		List<Integer> statusCodes = futures.stream()
			.map(future -> {
				try {
					return future.get().andReturn().getResponse().getStatus();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}).toList();

		long successCount = statusCodes.stream().filter(status -> status == 200).count();
		long conflictCount = statusCodes.stream().filter(status -> status == 409).count();

		assertThat(successCount).isEqualTo(1);
		assertThat(conflictCount).isEqualTo(threadCount - 1);
	}

	@Test
	@DisplayName("동시성 테스트 - 서로 다른 영수증 업로드 모두 성공")
	void upload_different_receipts_success() {
		// given
		Long memberId = 1L;
		String taskId = "1-1";
		List<ReceiptInfo> mockReceiptInfos = Arrays.asList(
			ReceiptInfo.builder()
				.date(LocalDate.of(2025, 1, 23))
				.categoryName("카페")
				.content("커피")
				.amount(4800L)
				.build(),
			ReceiptInfo.builder()
				.date(LocalDate.of(2025, 1, 23))
				.categoryName("식비")
				.content("칼국수")
				.amount(12000L)
				.build()
		);
		AiReceiptResponse response = AiReceiptResponse.builder().taskId(taskId).transactions(mockReceiptInfos).build();

		// Mock 설정
		given(categoryService.fetchCategoryNames(eq(memberId), isNull())).willReturn(Arrays.asList("카페", "식비"));
		given(transactionService.analyseReceipt(any(MultipartFile.class), any(List.class))).willReturn(response);

		// 인증 설정
		setAuthentication(memberId);

		int threadCount = 5;
		CountDownLatch latch = new CountDownLatch(threadCount);
		List<Future<ResultActions>> futures = new ArrayList<>();

		// when
		for (int i = 0; i < threadCount; i++) {
			final int index = i;

			Future<ResultActions> future = executorService.submit(() -> {
				try {
					MockMultipartFile receipt = new MockMultipartFile(
						"receipt",
						"test-receipt-" + index + ".jpg",
						"image/jpeg",
						("test receipt content " + index).getBytes()
					);

					latch.countDown();
					latch.await();

					return mockMvc.perform(multipart("/api/transactions/receipts/upload")
						.file(receipt)
						.contentType(MULTIPART_FORM_DATA));
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			});

			futures.add(future);
		}

		// then
		List<Integer> statusCodes = futures.stream()
			.map(future -> {
				try {
					return future.get().andReturn().getResponse().getStatus();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}).toList();

		long successCount = statusCodes.stream().filter(status -> status == 200).count();
		assertThat(successCount).isEqualTo(threadCount);
	}

	@Test
	@DisplayName("동시성 테스트 - 동일한 그룹 영수증 업로드 요청 시 하나만 성공")
	void upload_one_team_receipt_success() {
		// given
		Long memberId = 1L;
		Long teamId = 2L;
		String taskId = "1-1";
		List<ReceiptInfo> mockReceiptInfos = Collections.singletonList(
			ReceiptInfo.builder()
				.date(LocalDate.of(2025, 1, 23))
				.categoryName("회식")
				.content("삼겹살")
				.amount(25000L)
				.build()
		);
		AiReceiptResponse response = AiReceiptResponse.builder().taskId(taskId).transactions(mockReceiptInfos).build();

		// Mock 설정
		given(categoryService.fetchCategoryNames(memberId, teamId)).willReturn(Arrays.asList("회식", "대관"));
		given(transactionService.analyseReceipt(any(MultipartFile.class), any(List.class))).willReturn(response);

		// 동일한 영수증 파일 생성
		MockMultipartFile receipt = new MockMultipartFile(
			"receipt",
			"team-receipt.jpg",
			"image/jpeg",
			"team receipt content".getBytes()
		);

		// 인증 설정
		setAuthentication(memberId);

		int threadCount = 3;
		CountDownLatch latch = new CountDownLatch(threadCount);
		List<Future<ResultActions>> futures = new ArrayList<>();

		// when
		for (int i = 0; i < threadCount; i++) {
			Future<ResultActions> future = executorService.submit(() -> {
				try {
					latch.countDown();
					latch.await();

					return mockMvc.perform(multipart("/api/transactions/receipts/upload")
						.file(receipt)
						.param("teamId", String.valueOf(teamId))
						.contentType(MULTIPART_FORM_DATA));
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			});

			futures.add(future);
		}

		// then
		List<Integer> statusCodes = futures.stream()
			.map(future -> {
				try {
					return future.get().andReturn().getResponse().getStatus();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}).toList();

		long successCount = statusCodes.stream().filter(status -> status == 200).count();
		long conflictCount = statusCodes.stream().filter(status -> status == 409).count();

		assertThat(successCount).isEqualTo(1);
		assertThat(conflictCount).isEqualTo(threadCount - 1);
	}

	@Test
	@DisplayName("동시성 테스트 - 서로 다른 taskId로 영수증 데이터 저장 시 모두 성공")
	void save_different_task_id_success() {
		// given
		Member member = MemberFixture.getMember1();
		member = memberRepository.save(member);

		Category category = CategoryFixture.getCategory1(member);
		category = categoryRepository.save(category);

		// 인증 설정
		setAuthentication(member.getId());

		int threadCount = 5;
		CountDownLatch latch = new CountDownLatch(threadCount);
		List<Future<ResultActions>> futures = new ArrayList<>();

		// when - 동시에 5개의 서로 다른 taskId로 저장 요청
		for (int i = 0; i < threadCount; i++) {
			final int index = i + 1;
			Long categoryId = category.getId();

			Future<ResultActions> future = executorService.submit(() -> {
				try {
					List<TransactionRequest> transactionRequests = List.of(
						new TransactionRequest(null, 1000L * index, "테스트" + index,
							LocalDateTime.of(2025, 1, 23, 10, 30), EXPENSE, categoryId));

					SaveReceiptRequest saveRequest = SaveReceiptRequest.builder()
						.taskId(index + "-2")
						.transactions(transactionRequests)
						.build();

					latch.countDown();
					latch.await();

					return mockMvc.perform(post("/api/transactions/receipts/save")
						.content(objectMapper.writeValueAsString(saveRequest))
						.contentType(APPLICATION_JSON));
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			});

			futures.add(future);
		}

		// then
		List<Integer> statusCodes = futures.stream()
			.map(future -> {
				try {
					return future.get().andReturn().getResponse().getStatus();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}).toList();

		long successCount = statusCodes.stream().filter(status -> status == 200).count();
		assertThat(successCount).isEqualTo(threadCount);
	}

	@Test
	@DisplayName("동시성 테스트 - 락 타임아웃 검증 성공")
	void lock_timeout_success() throws Exception {
		// given
		Member member = MemberFixture.getMember1();
		member = memberRepository.save(member);

		Category category = CategoryFixture.getCategory1(member);
		category = categoryRepository.save(category);

		// 인증 설정
		setAuthentication(member.getId());

		String taskId = "1-1";

		// 먼저 락을 수동으로 획득 (타임아웃 시뮬레이션)
		String lockKey = "receipt:save:" + member.getId() + ":" + taskId;
		redisTemplate.opsForValue().set(lockKey, "lock", Duration.ofSeconds(10));

		List<TransactionRequest> transactionRequests = List.of(
			new TransactionRequest(null, 5000L, "타임아웃 테스트", LocalDateTime.of(2025, 1, 23, 10, 30), EXPENSE,
				category.getId()));

		SaveReceiptRequest saveRequest = SaveReceiptRequest.builder()
			.taskId(taskId)
			.transactions(transactionRequests)
			.build();

		// when & then
		mockMvc.perform(post("/api/transactions/receipts/save")
				.content(objectMapper.writeValueAsString(saveRequest))
				.contentType(APPLICATION_JSON))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.message").value(LOCK_EXIST_ERROR.getMessage()))
			.andDo(print());
	}

	private void setAuthentication(Long memberId) {
		CustomUserDetails memberUserDetails = new CustomUserDetails(memberId, memberId.toString(),
			authoritiesMapper.mapAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))));

		Authentication authentication = new UsernamePasswordAuthenticationToken(memberUserDetails, null,
			authoritiesMapper.mapAuthorities(memberUserDetails.getAuthorities()));

		SecurityContextHolder.getContext().setAuthentication(authentication);
	}
}
