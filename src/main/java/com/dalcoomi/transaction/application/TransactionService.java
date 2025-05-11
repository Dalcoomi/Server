package com.dalcoomi.transaction.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dalcoomi.category.application.repository.CategoryRepository;
import com.dalcoomi.category.domain.Category;
import com.dalcoomi.member.application.repository.MemberRepository;
import com.dalcoomi.member.domain.Member;
import com.dalcoomi.transaction.application.repository.TransactionRepository;
import com.dalcoomi.transaction.domain.Transaction;
import com.dalcoomi.transaction.dto.TransactionInfo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService {

	private final TransactionRepository transactionRepository;
	private final MemberRepository memberRepository;
	private final CategoryRepository categoryRepository;

	public void createTransaction(Long memberId, TransactionInfo transactionInfo) {
		Member member = memberRepository.findById(memberId);

		Category category = categoryRepository.findById(transactionInfo.categoryId());

		Transaction transaction = Transaction.of(member, category, transactionInfo);

		transactionRepository.save(transaction);
	}

	@Transactional
	public List<TransactionInfo> getTransactionsByMemberIdAndYearAndMonth(Long memberId, int year, int month) {
		List<Transaction> transactions = transactionRepository.findByMemberIdAndYearAndMonth(memberId, year, month);

		return transactions.stream().map(TransactionInfo::from).toList();
	}
}
