package com.example.course_lock_lab.enrollment.dto;

import java.time.LocalDateTime;

public record EnrollmentResponse(
	Long enrollmentId,
	Long courseId,
	Long studentId,
	String strategy,
	LocalDateTime createdAt
) {

	public static EnrollmentResponse from(EnrollmentResult result) {
		return new EnrollmentResponse(
			result.enrollmentId(),
			result.courseId(),
			result.studentId(),
			result.strategy(),
			result.createdAt()
		);
	}
}
