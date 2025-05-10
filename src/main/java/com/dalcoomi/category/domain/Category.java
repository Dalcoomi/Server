package com.dalcoomi.category.domain;

import static com.dalcoomi.common.error.model.ErrorMessage.CATEGORY_INVALID_ICON_URL;
import static com.dalcoomi.common.error.model.ErrorMessage.CATEGORY_INVALID_NAME;
import static io.micrometer.common.util.StringUtils.isBlank;
import static java.util.Objects.requireNonNull;

import java.time.LocalDateTime;

import com.dalcoomi.member.domain.Member;
import com.dalcoomi.transaction.domain.OwnerType;
import com.dalcoomi.transaction.domain.TransactionType;

import lombok.Builder;
import lombok.Getter;

@Getter
public class Category {

	private static final int NAME_LENGTH = 100;
	private static final int ICON_URL_LENGTH = 255;

	private final Long id;
	private final Member member;
	private final Long ownerId;
	private final String name;
	private final String iconUrl;
	private final Boolean isActive;
	private final TransactionType transactionType;
	private final OwnerType ownerType;
	private final LocalDateTime deletedAt;

	@Builder
	public Category(Long id, Member member, Long ownerId, String name, String iconUrl, Boolean isActive,
		TransactionType transactionType, OwnerType ownerType, LocalDateTime deletedAt) {
		this.id = id;
		this.member = member;
		this.ownerId = requireNonNull(ownerId) < 0 ? null : ownerId;
		this.name = validateName(name);
		this.iconUrl = validateIconUrl(iconUrl);
		this.isActive = requireNonNull(isActive);
		this.transactionType = requireNonNull(transactionType);
		this.ownerType = requireNonNull(ownerType);
		this.deletedAt = deletedAt;
	}

	private String validateName(String name) {
		if (isBlank(name) || name.length() > NAME_LENGTH) {
			throw new IllegalArgumentException(CATEGORY_INVALID_NAME.getMessage());
		}

		return name;
	}

	private String validateIconUrl(String iconUrl) {
		if (!isBlank(iconUrl) && iconUrl.length() > ICON_URL_LENGTH) {
			throw new IllegalArgumentException(CATEGORY_INVALID_ICON_URL.getMessage());
		}

		return iconUrl;
	}
}
