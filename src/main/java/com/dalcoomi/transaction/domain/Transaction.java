package com.dalcoomi.transaction.domain;

import static com.dalcoomi.common.error.model.ErrorMessage.TRANSACTION_INVALID_AMOUNT;
import static com.dalcoomi.common.error.model.ErrorMessage.TRANSACTION_INVALID_CONTENT;
import static io.micrometer.common.util.StringUtils.isBlank;
import static java.time.LocalDateTime.now;
import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

import java.time.LocalDateTime;

import com.dalcoomi.category.domain.Category;
import com.dalcoomi.member.domain.Member;
import com.dalcoomi.transaction.dto.request.TransactionRequest;

import lombok.Builder;
import lombok.Getter;

@Getter
public class Transaction {

	public static final int CONTENT_LENGTH = 100;

	private final Long id;
	private final Long teamId;
	private Member member;
	private Category category;
	private Long amount;
	private String content;
	private LocalDateTime transactionDate;
	private TransactionType transactionType;
	private LocalDateTime deletedAt;

	@Builder
	public Transaction(Long id, Member member, Category category, Long teamId, LocalDateTime transactionDate,
		String content, Long amount, TransactionType transactionType, LocalDateTime deletedAt) {
		this.id = id;
		this.member = member;
		this.category = category;
		this.teamId = teamId;
		this.amount = validateAmount(amount);
		this.content = validateContent(content);
		this.transactionDate = requireNonNull(transactionDate);
		this.transactionType = requireNonNull(transactionType);
		this.deletedAt = deletedAt;
	}

	public static Transaction from(TransactionRequest request) {
		return Transaction.builder()
			.amount(request.amount())
			.content(request.content())
			.transactionDate(request.transactionDate())
			.transactionType(request.transactionType())
			.build();
	}

	public void updateMember(Member member) {
		this.member = member;
	}

	public void updateCategory(Category category) {
		this.category = category;
	}

	public void updateAmount(Long amount) {
		this.amount = validateAmount(amount);
	}

	public void updateContent(String content) {
		this.content = validateContent(content);
	}

	public void updateTransactionDate(LocalDateTime transactionDate) {
		this.transactionDate = requireNonNull(transactionDate);
	}

	public void updateTransactionType(TransactionType transactionType) {
		this.transactionType = requireNonNull(transactionType);
	}

	public void softDelete() {
		this.deletedAt = now();
	}

	private String validateContent(String content) {
		if (!isBlank(content) && content.length() > CONTENT_LENGTH) {
			throw new IllegalArgumentException(TRANSACTION_INVALID_CONTENT.getMessage());
		}

		return content;
	}

	private Long validateAmount(Long amount) {
		if (isNull(amount) || amount < 1) {
			throw new IllegalArgumentException(TRANSACTION_INVALID_AMOUNT.getMessage());
		}

		return amount;
	}
}
