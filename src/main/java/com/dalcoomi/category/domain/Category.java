package com.dalcoomi.category.domain;

import static com.dalcoomi.category.constant.CategoryConstants.ICON_URL_LENGTH;
import static com.dalcoomi.category.constant.CategoryConstants.NAME_LENGTH;
import static com.dalcoomi.common.error.model.ErrorMessage.CATEGORY_INVALID_ICON_URL;
import static com.dalcoomi.common.error.model.ErrorMessage.CATEGORY_INVALID_NAME;
import static com.dalcoomi.common.error.model.ErrorMessage.CATEGORY_INVALID_TEAM_ID;
import static io.micrometer.common.util.StringUtils.isBlank;
import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

import java.time.LocalDateTime;

import com.dalcoomi.member.domain.Member;
import com.dalcoomi.transaction.domain.TransactionType;

import lombok.Builder;
import lombok.Getter;

@Getter
public class Category {

	private final Long id;
	private final Member creator;
	private final Long teamId;
	private final String name;
	private final String iconUrl;
	private final Boolean isActive;
	private final TransactionType transactionType;
	private final OwnerType ownerType;
	private final LocalDateTime deletedAt;

	@Builder
	public Category(Long id, Member creator, Long teamId, String name, String iconUrl, Boolean isActive,
		TransactionType transactionType, OwnerType ownerType, LocalDateTime deletedAt) {
		this.id = id;
		this.creator = creator;
		this.teamId = validateTeamId(teamId);
		this.name = validateName(name);
		this.iconUrl = validateIconUrl(iconUrl);
		this.isActive = requireNonNull(isActive);
		this.transactionType = requireNonNull(transactionType);
		this.ownerType = requireNonNull(ownerType);
		this.deletedAt = deletedAt;
	}

	private Long validateTeamId(Long teamId) {
		if (!isNull(teamId) && teamId < 1) {
			throw new IllegalArgumentException(CATEGORY_INVALID_TEAM_ID.getMessage());
		}

		return teamId;
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
