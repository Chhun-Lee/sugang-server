package com.example.course_lock_lab.course.dto;

import com.example.course_lock_lab.course.entity.Course;
import java.time.LocalDateTime;

public record CourseResponse(
	Long id,
	String title,
	int capacity,
	int enrolledCount,
	long version,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {

	public static CourseResponse from(Course course) {
		return new CourseResponse(
			course.getId(),
			course.getTitle(),
			course.getCapacity(),
			course.getEnrolledCount(),
			course.getVersion(),
			course.getCreatedAt(),
			course.getUpdatedAt()
		);
	}
}
