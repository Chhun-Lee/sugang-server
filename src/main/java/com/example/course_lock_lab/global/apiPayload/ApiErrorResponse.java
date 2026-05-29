package com.example.course_lock_lab.global.apiPayload;

import java.time.LocalDateTime;

public record ApiErrorResponse(
	String reason,
	String message,
	LocalDateTime timestamp
) {

	public static ApiErrorResponse of(String reason, String message) {
		return new ApiErrorResponse(reason, message, LocalDateTime.now());
	}
}
