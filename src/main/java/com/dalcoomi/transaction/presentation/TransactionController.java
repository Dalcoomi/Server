package com.dalcoomi.transaction.presentation;

import static com.dalcoomi.transaction.constant.ReceiptStreamConstants.PROCESSING_KEY;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.dalcoomi.auth.annotation.AuthMember;
import com.dalcoomi.common.util.lock.ReceiptLockKeyGenerator;
import com.dalcoomi.common.util.lock.RedisLockUtil;
import com.dalcoomi.transaction.application.ReceiptStreamProducer;
import com.dalcoomi.transaction.application.TransactionService;
import com.dalcoomi.transaction.domain.Transaction;
import com.dalcoomi.transaction.domain.event.TransactionCreatedEvent;
import com.dalcoomi.transaction.dto.TransactionSearchCriteria;
import com.dalcoomi.transaction.dto.TransactionsInfo;
import com.dalcoomi.transaction.dto.request.ReceiptCallbackRequest;
import com.dalcoomi.transaction.dto.request.SaveReceiptRequest;
import com.dalcoomi.transaction.dto.request.TransactionRequest;
import com.dalcoomi.transaction.dto.response.AsyncReceiptResponse;
import com.dalcoomi.transaction.dto.response.GetTransactionResponse;
import com.dalcoomi.transaction.dto.response.GetTransactionsResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

	private final TransactionService transactionService;
	private final ApplicationEventPublisher applicationEventPublisher;
	private final RedisLockUtil redisLockUtil;
	private final ReceiptLockKeyGenerator receiptLockKeyGenerator;
	private final ReceiptStreamProducer receiptStreamProducer;
	private final StringRedisTemplate stringRedisTemplate;

	@PostMapping
	@ResponseStatus(CREATED)
	public void create(@AuthMember Long memberId, @RequestBody @Valid TransactionRequest request) {
		Transaction transaction = Transaction.from(request);

		transactionService.create(memberId, request.categoryId(), transaction, request.synchronizeTransaction());
	}

	@PostMapping("/receipts/upload")
	@ResponseStatus(OK)
	public AsyncReceiptResponse uploadReceipt(@AuthMember Long memberId, @RequestParam("teamId") @Nullable Long teamId,
		@RequestPart("receipt") @NotNull(message = "영수증 파일이 필요합니다.") MultipartFile receipt) {
		String lockKey = receiptLockKeyGenerator.generateUploadLockKey(memberId, teamId, receipt);

		return redisLockUtil.acquireAndRunLock(lockKey, () -> {
			String taskId = receiptStreamProducer.publishReceiptTask(memberId, teamId, receipt);

			return AsyncReceiptResponse.from(taskId);
		});
	}

	@PostMapping("/receipts/callback")
	@ResponseStatus(OK)
	public void receiptCallback(@RequestBody @Valid ReceiptCallbackRequest request) {
		log.info("AI 서버로부터 영수증 처리 성공 콜백 받음: taskId={}, transactionCount={}", request.taskId(),
			request.transactions() != null ? request.transactions().size() : 0);

		// 처리 완료 플래그 제거 (다음 영수증 처리 가능)
		stringRedisTemplate.delete(PROCESSING_KEY);
		log.info("영수증 처리 완료, 다음 영수증 처리 가능: taskId={}", request.taskId());
	}

	@PostMapping("/receipts/save")
	@ResponseStatus(OK)
	public void saveReceipt(@AuthMember Long memberId, @RequestBody @Valid SaveReceiptRequest request) {
		String lockKey = receiptLockKeyGenerator.generateSaveLockKey(memberId, request.taskId());

		redisLockUtil.acquireAndRunLock(lockKey, () -> {
			List<Transaction> transactions = request.transactions().stream().map(Transaction::from).toList();
			List<Long> categoryIds = request.transactions().stream().map(TransactionRequest::categoryId).toList();

			List<Transaction> savedTransactions = transactionService.create(memberId, categoryIds, transactions);

			applicationEventPublisher.publishEvent(
				new TransactionCreatedEvent(this, request.taskId(), savedTransactions));

			return null;
		});
	}

	@GetMapping
	@ResponseStatus(OK)
	public GetTransactionsResponse get(@AuthMember Long memberId, @RequestParam("teamId") @Nullable Long teamId,
		@RequestParam("year") Integer year, @RequestParam("month") Integer month,
		@RequestParam("categoryName") @Nullable String categoryName,
		@RequestParam("creatorNickname") @Nullable String creatorNickname) {
		TransactionSearchCriteria criteria = TransactionSearchCriteria.of(memberId, teamId, year, month, categoryName,
			creatorNickname);

		TransactionsInfo transactionsInfo = transactionService.get(criteria);

		return GetTransactionsResponse.from(transactionsInfo);
	}

	@GetMapping("/{transactionId}")
	@ResponseStatus(OK)
	public GetTransactionResponse get(@AuthMember Long memberId, @PathVariable("transactionId") Long transactionId,
		@RequestParam("teamId") @Nullable Long teamId) {
		Transaction transaction = transactionService.get(memberId, transactionId, teamId);

		return GetTransactionResponse.from(transaction);
	}

	@PutMapping("/{transactionId}")
	@ResponseStatus(OK)
	public void update(@AuthMember Long memberId, @PathVariable("transactionId") Long transactionId,
		@RequestBody @Valid TransactionRequest request) {
		Transaction transaction = Transaction.from(request);

		transactionService.update(memberId, transactionId, request.categoryId(), transaction);
	}

	@DeleteMapping("/{transactionId}")
	@ResponseStatus(OK)
	public void delete(@AuthMember Long memberId, @PathVariable("transactionId") Long transactionId,
		@RequestParam("teamId") @Nullable Long teamId) {

		transactionService.delete(memberId, transactionId, teamId);
	}
}
