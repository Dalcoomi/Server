package com.dalcoomi.batch;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dalcoomi.transaction.application.repository.TransactionRepository;
import com.dalcoomi.transaction.domain.Transaction;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionBatchService {

	private final TransactionRepository transactionRepository;

	@Transactional
	public void deleteExpiredAnonymizedData() {
		LocalDateTime fiveYearsAgo = LocalDateTime.now().minusYears(5);
		log.info("익명화된 데이터 삭제 배치 시작. 기준일: {}", fiveYearsAgo);

		// 5년 경과한 익명화된 개인 거래 내역 삭제 (삭제 요청권 만료)
		List<Transaction> expiredAnonymizedTransactions =
			transactionRepository.findExpiredAnonymizedPersonalTransactions(fiveYearsAgo);

		if (!expiredAnonymizedTransactions.isEmpty()) {
			transactionRepository.deleteAll(expiredAnonymizedTransactions);
		}

		log.info("익명화된 개인 거래 내역 삭제 완료: {}건", expiredAnonymizedTransactions.size());
		log.info("익명화된 데이터 삭제 배치 완료");
	}
}
