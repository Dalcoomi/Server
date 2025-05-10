package com.dalcoomi.transaction.presentation;

import static org.springframework.http.HttpStatus.OK;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.dalcoomi.auth.config.AuthMember;
import com.dalcoomi.transaction.application.TransactionService;
import com.dalcoomi.transaction.dto.TransactionInfo;
import com.dalcoomi.transaction.dto.response.GetMyTransactionResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/transaction")
@RequiredArgsConstructor
public class TransactionController {

	private final TransactionService transactionService;

	@GetMapping("/my")
	@ResponseStatus(OK)
	public GetMyTransactionResponse getMyTransactionsWithYearAndMonth(@AuthMember Long memberId,
		@RequestParam("year") Integer year, @RequestParam("month") Integer month) {
		List<TransactionInfo> transactionInfos = transactionService.getMyTransactionsWithYearAndMonth(memberId, year,
			month);

		return GetMyTransactionResponse.from(transactionInfos);
	}
}
