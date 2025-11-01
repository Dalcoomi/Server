package com.dalcoomi.auth.domain;

import static com.dalcoomi.common.error.model.ErrorMessage.UNSUPPORTED_DEVICE_TYPE;

import com.dalcoomi.common.error.exception.BadRequestException;
import com.fasterxml.jackson.annotation.JsonCreator;

public enum DeviceType {

	WEB,
	MOBILE;

	@JsonCreator
	public static DeviceType from(String value) {
		for (DeviceType type : DeviceType.values()) {
			if (type.name().equalsIgnoreCase(value)) {
				return type;
			}
		}

		throw new BadRequestException(UNSUPPORTED_DEVICE_TYPE);
	}
}
