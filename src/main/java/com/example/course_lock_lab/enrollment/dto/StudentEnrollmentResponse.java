package com.example.course_lock_lab.enrollment.dto;

import com.example.course_lock_lab.enrollment.entity.Enrollment;
import java.time.LocalDateTime;

public record StudentEnrollmentResponse(
	Long enrollmentId,
	Long studentId,
	Long courseId,
	LocalDateTime createdAt
) {

	public static StudentEnrollmentResponse from(Enrollment enrollment) {
		return new StudentEnrollmentResponse(
			enrollment.getId(),
			enrollment.getStudentId(),
			enrollment.getCourseId(),
			enrollment.getCreatedAt()
		);
	}
}
