package com.dalcoomi.transaction.application;

import static com.dalcoomi.common.error.model.ErrorMessage.TEAM_MEMBER_NOT_FOUND;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dalcoomi.category.application.repository.CategoryRepository;
import com.dalcoomi.category.domain.Category;
import com.dalcoomi.common.error.exception.NotFoundException;
import com.dalcoomi.member.application.repository.MemberRepository;
import com.dalcoomi.member.domain.Member;
import com.dalcoomi.team.application.repository.TeamMemberRepository;
import com.dalcoomi.transaction.application.repository.TransactionRepository;
import com.dalcoomi.transaction.domain.Transaction;
import com.dalcoomi.transaction.dto.TransactionSearchCriteria;
import com.dalcoomi.transaction.dto.TransactionsInfo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService {

	private final TransactionRepository transactionRepository;
	private final MemberRepository memberRepository;
	private final CategoryRepository categoryRepository;
	private final TeamMemberRepository teamMemberRepository;

	@Transactional
	public void createTransaction(Long memberId, Long categoryId, Transaction transaction) {
		validateTeamMember(transaction.getTeamId(), memberId);

		Member member = memberRepository.findById(memberId);
		Category category = categoryRepository.findById(categoryId);

		transaction.updateCreator(member);
		transaction.updateCategory(category);

		transactionRepository.save(transaction);
	}

	@Transactional(readOnly = true)
	public TransactionsInfo getTransactions(TransactionSearchCriteria criteria) {
		validateTeamMember(criteria.teamId(), criteria.requesterId());

		List<Transaction> transactions = transactionRepository.findTransactions(criteria);

		return TransactionsInfo.from(transactions);
	}

	@Transactional(readOnly = true)
	public Transaction getMyTransaction(Long memberId, Long transactionId) {
		return transactionRepository.findByIdAndCreatorId(transactionId, memberId);
	}

	@Transactional
	public void updateTransaction(Long memberId, Long transactionId, Long categoryId, Transaction transaction) {
		Transaction currentTransaction = transactionRepository.findByIdAndCreatorId(transactionId, memberId);
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
		Transaction transaction = transactionRepository.findByIdAndCreatorId(transactionId, memberId);

		transaction.softDelete();

		transactionRepository.save(transaction);
	}

	private void validateTeamMember(Long teamId, Long memberId) {
		if (teamId == null) {
			return;
		}

		if (!teamMemberRepository.existsByTeamIdAndMemberId(teamId, memberId)) {
			throw new NotFoundException(TEAM_MEMBER_NOT_FOUND);
		}
	}
}
