package com.example.course_lock_lab.enrollment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record EnrollmentRequest(
	@NotNull @Positive Long studentId
) {
}
