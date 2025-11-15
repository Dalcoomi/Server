package com.dalcoomi.dummy.dto;

public record DummyDataResponse(
	boolean success,
	String message,
	String dataType,
	int count,
	Object data
) {

}
