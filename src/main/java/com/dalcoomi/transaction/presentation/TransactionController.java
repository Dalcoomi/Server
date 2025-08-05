package com.dalcoomi.transaction.presentation;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
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
import com.dalcoomi.category.application.CategoryService;
import com.dalcoomi.common.util.lock.ReceiptLockKeyGenerator;
import com.dalcoomi.common.util.lock.RedisLockUtil;
import com.dalcoomi.transaction.application.TransactionService;
import com.dalcoomi.transaction.domain.Transaction;
import com.dalcoomi.transaction.domain.event.TransactionCreatedEvent;
import com.dalcoomi.transaction.dto.TransactionSearchCriteria;
import com.dalcoomi.transaction.dto.TransactionsInfo;
import com.dalcoomi.transaction.dto.request.SaveReceiptRequest;
import com.dalcoomi.transaction.dto.request.TransactionRequest;
import com.dalcoomi.transaction.dto.response.AiReceiptResponse;
import com.dalcoomi.transaction.dto.response.GetTransactionResponse;
import com.dalcoomi.transaction.dto.response.GetTransactionsResponse;
import com.dalcoomi.transaction.dto.response.UploadReceiptResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

	private final TransactionService transactionService;
	private final CategoryService categoryService;
	private final ApplicationEventPublisher applicationEventPublisher;
	private final RedisLockUtil redisLockUtil;
	private final ReceiptLockKeyGenerator lockKeyGenerator;

	@PostMapping
	@ResponseStatus(CREATED)
	public void create(@AuthMember Long memberId, @RequestBody @Valid TransactionRequest request) {
		Transaction transaction = Transaction.from(request);

		transactionService.create(memberId, request.categoryId(), transaction, request.synchronizeTransaction());
	}

	@PostMapping("/receipts/upload")
	@ResponseStatus(OK)
	public UploadReceiptResponse uploadReceipt(@AuthMember Long memberId, @RequestParam("teamId") @Nullable Long teamId,
		@RequestPart("receipt") @NotNull(message = "영수증 파일이 필요합니다.") MultipartFile receipt) {
		String lockKey = lockKeyGenerator.generateUploadLockKey(memberId, teamId, receipt);

		return redisLockUtil.acquireAndRunLock(lockKey, () -> {
			List<String> categoryNames = categoryService.fetchCategoryNames(memberId, teamId);
			AiReceiptResponse aiResponse = transactionService.analyseReceipt(receipt, categoryNames);

			return UploadReceiptResponse.from(aiResponse);
		});
	}

	@PostMapping("/receipts/save")
	@ResponseStatus(OK)
	public void saveReceipt(@AuthMember Long memberId, @RequestBody SaveReceiptRequest request) {
		String lockKey = lockKeyGenerator.generateSaveLockKey(memberId, request.taskId());

		redisLockUtil.acquireAndRunLock(lockKey, () -> {
			List<Transaction> transactions = request.transactions().stream().map(Transaction::from).toList();
			List<Long> categoryIds = request.transactions().stream().map(TransactionRequest::categoryId).toList();

			List<Transaction> savedTransactions = transactionService.create(memberId, categoryIds, transactions);

			applicationEventPublisher.publishEvent(
				new TransactionCreatedEvent(this, request.taskId(), savedTransactions)
			);

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
		@RequestBody TransactionRequest request) {
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
