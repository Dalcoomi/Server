package com.dalcoomi.common.jpa;

import static java.util.Objects.isNull;

import java.util.function.Function;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.SimpleExpression;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class DynamicQuery {

	public static <T> BooleanExpression generateEq(T value, Function<T, BooleanExpression> function) {
		if (isNull(value)) {
			return null;
		}

		return function.apply(value);
	}

	public static <T> BooleanExpression generateEqOrIsNull(T value, Function<T, BooleanExpression> function,
		BooleanExpression nullCondition) {
		if (isNull(value)) {
			return nullCondition;
		}

		return function.apply(value);
	}

	@SuppressWarnings("rawtypes")
	public static <T extends SimpleExpression> BooleanExpression generateIsNull(Boolean value, T field) {
		if (isNull(value)) {
			return null;
		}

		if (value) {
			return field.isNull();
		}

		return field.isNotNull();
	}
}
