package com.dalcoomi.auth.dto;

import com.dalcoomi.auth.domain.DeviceType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenInfo {

	private String token;
	private DeviceType deviceType;

	public static RefreshTokenInfo of(String token, DeviceType deviceType) {
		return new RefreshTokenInfo(token, deviceType);
	}

	public String toJson() {
		try {
			ObjectMapper mapper = new ObjectMapper();

			return mapper.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException("RefreshTokenInfo를 JSON으로 변환할 수 없습니다.", e);
		}
	}

	public static RefreshTokenInfo fromJson(String json) {
		try {
			ObjectMapper mapper = new ObjectMapper();

			return mapper.readValue(json, RefreshTokenInfo.class);
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException("JSON을 RefreshTokenInfo로 변환할 수 없습니다.", e);
		}
	}
}
