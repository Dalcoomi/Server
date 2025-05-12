package com.dalcoomi.transaction.presentation;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.dalcoomi.auth.config.AuthMember;
import com.dalcoomi.transaction.application.TransactionService;
import com.dalcoomi.transaction.domain.Transaction;
import com.dalcoomi.transaction.dto.TransactionsInfo;
import com.dalcoomi.transaction.dto.request.TransactionRequest;
import com.dalcoomi.transaction.dto.response.GetMyTransactionResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/transaction")
@RequiredArgsConstructor
public class TransactionController {

	private final TransactionService transactionService;

	@PostMapping("/my")
	@ResponseStatus(CREATED)
	public void createMyTransaction(@AuthMember Long memberId, @RequestBody TransactionRequest request) {
		Transaction transaction = Transaction.from(request);

		transactionService.createTransaction(memberId, request.categoryId(), transaction);
	}

	@GetMapping("/my")
	@ResponseStatus(OK)
	public GetMyTransactionResponse getMyTransactionsWithYearAndMonth(@AuthMember Long memberId,
		@RequestParam("year") Integer year, @RequestParam("month") Integer month) {
		TransactionsInfo transactionsInfo = transactionService.getTransactionsByMemberIdAndYearAndMonth(memberId, year,
			month);

		return GetMyTransactionResponse.from(transactionsInfo);
	}
}
