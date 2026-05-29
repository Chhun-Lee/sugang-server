package com.example.course_lock_lab.enrollment.dto;

import com.example.course_lock_lab.enrollment.entity.Enrollment;
import java.time.LocalDateTime;

public record EnrollmentResult(
	Long enrollmentId,
	Long courseId,
	Long studentId,
	String strategy,
	LocalDateTime createdAt
) {

	public static EnrollmentResult from(Enrollment enrollment, String strategy) {
		return new EnrollmentResult(
			enrollment.getId(),
			enrollment.getCourseId(),
			enrollment.getStudentId(),
			strategy,
			enrollment.getCreatedAt()
		);
	}
}
