package com.dalcoomi.common.validator;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = ProfileImageValidator.class)
public @interface ValidProfileImage {

	String message() default "프로필 이미지는 JPG, PNG 형식을 지원합니다.";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
