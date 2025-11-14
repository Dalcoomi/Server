package com.dalcoomi.common.aop;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * REST API 요청/응답 로깅 AOP
 * 모든 컨트롤러 메서드에 대해 요청 정보, 응답 정보, 실행 시간을 로깅합니다.
 */
@Slf4j
@Aspect
@Component
public class ApiLoggingAspect {

	private static final long SLOW_API_THRESHOLD_MS = 3000L;
	private static final String[] SENSITIVE_FIELDS = {
		// 인증 관련
		"password", "token", "secret", "apiKey", "refreshToken",
		// Member 암호화 필드
		"email", "name", "birthday", "gender",
		// SocialConnection 암호화 필드
		"socialEmail", "socialId", "socialRefreshToken",
		// Transaction 암호화 필드
		"content", "amount"
	};

	/**
	 * 모든 REST 컨트롤러 메서드에 대해 로깅
	 */
	@Around("execution(* com.dalcoomi..presentation..*Controller.*(..))")
	public Object logApiExecution(ProceedingJoinPoint joinPoint) throws Throwable {
		HttpServletRequest request = getCurrentRequest();

		if (request == null) {
			return joinPoint.proceed();
		}

		String httpMethod = request.getMethod();
		String requestUri = request.getRequestURI();
		String className = joinPoint.getTarget().getClass().getSimpleName();
		String methodName = joinPoint.getSignature().getName();

		// 요청 정보 로깅
		logRequest(request, httpMethod, requestUri, className, methodName, joinPoint);

		long startTime = System.currentTimeMillis();
		Object result;
		Exception exception = null;

		try {
			result = joinPoint.proceed();

			return result;
		} catch (Exception e) {
			exception = e;

			throw e;
		} finally {
			long elapsedTime = System.currentTimeMillis() - startTime;

			// 응답 정보 로깅
			logResponse(httpMethod, requestUri, elapsedTime, exception);

			// 느린 API 경고
			if (elapsedTime > SLOW_API_THRESHOLD_MS) {
				log.warn("[SLOW API] {} {} | elapsed: {}ms | threshold: {}ms", httpMethod, requestUri, elapsedTime,
					SLOW_API_THRESHOLD_MS);
			}
		}
	}

	/**
	 * 요청 정보 로깅
	 */
	private void logRequest(HttpServletRequest request, String httpMethod, String requestUri,
		String className, String methodName, ProceedingJoinPoint joinPoint) {

		Map<String, String> params = getRequestParams(request);
		Map<String, Object> args = getMethodArguments(joinPoint);

		// 민감한 정보 마스킹
		maskSensitiveData(params);
		maskSensitiveData(args);

		if (params.isEmpty() && args.isEmpty()) {
			log.info("[REQUEST] {} {} | {}.{}()", httpMethod, requestUri, className, methodName);
		} else if (params.isEmpty()) {
			log.info("[REQUEST] {} {} | {}.{}() | args: {}", httpMethod, requestUri, className, methodName, args);
		} else if (args.isEmpty()) {
			log.info("[REQUEST] {} {} | {}.{}() | params: {}", httpMethod, requestUri, className, methodName, params);
		} else {
			log.info("[REQUEST] {} {} | {}.{}() | params: {} | args: {}", httpMethod, requestUri, className, methodName,
				params, args);
		}
	}

	/**
	 * 응답 정보 로깅
	 */
	private void logResponse(String httpMethod, String requestUri, long elapsedTime, Exception exception) {
		if (exception != null) {
			log.error("[RESPONSE] {} {} | status: ERROR | elapsed: {}ms | exception: {}", httpMethod, requestUri,
				elapsedTime, exception.getClass().getSimpleName());
		} else {
			log.info("[RESPONSE] {} {} | status: SUCCESS | elapsed: {}ms", httpMethod, requestUri, elapsedTime);
		}
	}

	/**
	 * HTTP 요청 파라미터 추출
	 */
	private Map<String, String> getRequestParams(HttpServletRequest request) {
		Map<String, String> params = new HashMap<>();
		Enumeration<String> paramNames = request.getParameterNames();

		while (paramNames.hasMoreElements()) {
			String paramName = paramNames.nextElement();
			String paramValue = request.getParameter(paramName);
			params.put(paramName, paramValue);
		}

		return params;
	}

	/**
	 * 메서드 인자 추출
	 */
	private Map<String, Object> getMethodArguments(ProceedingJoinPoint joinPoint) {
		Map<String, Object> args = new HashMap<>();
		MethodSignature signature = (MethodSignature)joinPoint.getSignature();
		String[] paramNames = signature.getParameterNames();
		Object[] paramValues = joinPoint.getArgs();

		for (int i = 0; i < paramNames.length; i++) {
			// HttpServletRequest, HttpServletResponse 등은 제외
			if (paramValues[i] != null
				&& !paramValues[i].getClass().getName().startsWith("org.springframework")
				&& !paramValues[i].getClass().getName().startsWith("jakarta.servlet")
			) {
				args.put(paramNames[i], paramValues[i]);
			}
		}

		return args;
	}

	/**
	 * 민감한 정보 마스킹 (뒤 3자리만 표시)
	 */
	@SuppressWarnings("unchecked")
	private void maskSensitiveData(Map<String, ?> data) {
		for (String sensitiveField : SENSITIVE_FIELDS) {
			((Map<String, Object>)data).computeIfPresent(sensitiveField, (key, value) -> {
				String strValue = String.valueOf(value);

				if (strValue.length() <= 3) {
					return "***";
				}

				String lastThree = strValue.substring(strValue.length() - 3);

				return "***" + lastThree;
			});
		}
	}

	/**
	 * 현재 HTTP 요청 가져오기
	 */
	private HttpServletRequest getCurrentRequest() {
		ServletRequestAttributes attributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();

		return attributes != null ? attributes.getRequest() : null;
	}
}
