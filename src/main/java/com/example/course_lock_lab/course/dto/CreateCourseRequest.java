package com.example.course_lock_lab.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CreateCourseRequest(
	@NotBlank String title,
	@Positive int capacity
) {
}
