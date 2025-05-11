package com.dalcoomi.transaction.domain;

import static com.dalcoomi.common.error.model.ErrorMessage.TRANSACTION_INVALID_CONTENT;
import static io.micrometer.common.util.StringUtils.isBlank;
import static java.util.Objects.requireNonNull;

import java.time.LocalDateTime;

import com.dalcoomi.category.domain.Category;
import com.dalcoomi.member.domain.Member;
import com.dalcoomi.transaction.dto.TransactionInfo;

import lombok.Builder;
import lombok.Getter;

@Getter
public class Transaction {

	public static final int CONTENT_LENGTH = 100;

	private final Long id;
	private final Member member;
	private final Category category;
	private final Long groupId;
	private final LocalDateTime transactionDate;
	private final String content;
	private final Long amount;
	private final TransactionType transactionType;
	private final LocalDateTime deletedAt;

	@Builder
	public Transaction(Long id, Member member, Category category, Long groupId, LocalDateTime transactionDate,
		String content, Long amount, TransactionType transactionType, LocalDateTime deletedAt) {
		this.id = id;
		this.member = requireNonNull(member);
		this.category = requireNonNull(category);
		this.groupId = groupId;
		this.transactionDate = requireNonNull(transactionDate);
		this.content = validateContent(content);
		this.amount = requireNonNull(amount);
		this.transactionType = requireNonNull(transactionType);
		this.deletedAt = deletedAt;
	}

	public static Transaction of(Member member, Category category, TransactionInfo transactionInfo) {
		return Transaction.builder()
			.member(member)
			.category(category)
			.transactionDate(transactionInfo.transactionDate())
			.content(transactionInfo.content())
			.amount(transactionInfo.amount())
			.transactionType(transactionInfo.transactionType())
			.build();
	}

	private String validateContent(String content) {
		if (!isBlank(content) && content.length() > CONTENT_LENGTH) {
			throw new IllegalArgumentException(TRANSACTION_INVALID_CONTENT.getMessage());
		}

		return content;
	}
}
