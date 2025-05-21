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
import com.dalcoomi.transaction.dto.TransactionsInfo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService {

	private final TransactionRepository transactionRepository;
	private final MemberRepository memberRepository;
	private final CategoryRepository categoryRepository;

	@Transactional
	public void createTransaction(Long memberId, Long categoryId, Transaction transaction) {
		Member member = memberRepository.findById(memberId);
		Category category = categoryRepository.findById(categoryId);

		transaction.updateMember(member);
		transaction.updateCategory(category);

		transactionRepository.save(transaction);
	}

	@Transactional(readOnly = true)
	public TransactionsInfo getTransactionsByMemberIdAndYearAndMonth(Long memberId, int year, int month) {
		List<Transaction> transactions = transactionRepository.findByMemberIdAndYearAndMonth(memberId, year, month);

		return TransactionsInfo.from(transactions);
	}

	@Transactional(readOnly = true)
	public Transaction getTransactionsById(Long memberId, Long transactionId) {
		return transactionRepository.findByIdAndMemberId(transactionId, memberId);
	}

	@Transactional
	public void updateTransaction(Long memberId, Long transactionId, Long categoryId, Transaction transaction) {
		Transaction currentTransaction = transactionRepository.findByIdAndMemberId(transactionId, memberId);
		Category category = categoryRepository.findById(categoryId);

		currentTransaction.updateCategory(category);
		currentTransaction.updateAmount(transaction.getAmount());
		currentTransaction.updateContent(transaction.getContent());
		currentTransaction.updateTransactionDate(transaction.getTransactionDate());
		currentTransaction.updateTransactionType(transaction.getTransactionType());

		transactionRepository.save(currentTransaction);
	}

	@Transactional
	public void deleteTransaction(Long memberId, Long transactionId) {
		Transaction transaction = transactionRepository.findByIdAndMemberId(transactionId, memberId);

		transaction.softDelete();

		transactionRepository.save(transaction);
	}
}
