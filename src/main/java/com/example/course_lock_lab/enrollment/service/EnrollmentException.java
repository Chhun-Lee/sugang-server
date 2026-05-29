package com.example.course_lock_lab.enrollment.service;

import org.springframework.http.HttpStatus;

public class EnrollmentException extends RuntimeException {

	private final HttpStatus status;
	private final String reason;

	private EnrollmentException(HttpStatus status, String reason, String message) {
		super(message);
		this.status = status;
		this.reason = reason;
	}

	public static EnrollmentException courseNotFound(Long courseId) {
		return new EnrollmentException(HttpStatus.NOT_FOUND, "course_not_found", "Course not found: " + courseId);
	}

	public static EnrollmentException duplicate(Long studentId, Long courseId) {
		return new EnrollmentException(
			HttpStatus.CONFLICT,
			"duplicate",
			"Student already enrolled. studentId=" + studentId + ", courseId=" + courseId
		);
	}

	public static EnrollmentException capacityFull(Long courseId) {
		return new EnrollmentException(HttpStatus.CONFLICT, "capacity_full", "Course capacity is full: " + courseId);
	}

	public static EnrollmentException enrollmentNotFound(Long studentId, Long courseId) {
		return new EnrollmentException(
			HttpStatus.NOT_FOUND,
			"enrollment_not_found",
			"Enrollment not found. studentId=" + studentId + ", courseId=" + courseId
		);
	}

	public HttpStatus status() {
		return status;
	}

	public String reason() {
		return reason;
	}
}
