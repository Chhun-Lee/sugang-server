package com.example.course_lock_lab.global.apiPayload;

import com.example.course_lock_lab.enrollment.service.EnrollmentException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(EnrollmentException.class)
	public ResponseEntity<ApiErrorResponse> handleEnrollmentException(EnrollmentException exception) {
		return ResponseEntity
			.status(exception.status())
			.body(ApiErrorResponse.of(exception.reason(), exception.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
		return ResponseEntity.badRequest()
			.body(ApiErrorResponse.of("validation_failed", exception.getMessage()));
	}

	@ExceptionHandler(HandlerMethodValidationException.class)
	public ResponseEntity<ApiErrorResponse> handleHandlerMethodValidation(HandlerMethodValidationException exception) {
		return ResponseEntity.badRequest()
			.body(ApiErrorResponse.of("validation_failed", exception.getMessage()));
	}
}
