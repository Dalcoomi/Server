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
@Constraint(validatedBy = ReceiptImageValidator.class)
public @interface ValidReceiptImage {

	String message() default "영수증은 JPG, PNG, BMP, TIFF, WEBP, HEIC, HEIF 형식을 지원합니다.";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
