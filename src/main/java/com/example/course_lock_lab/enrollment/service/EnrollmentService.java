package com.example.course_lock_lab.enrollment.service;

import com.example.course_lock_lab.enrollment.dto.EnrollmentResult;

public interface EnrollmentService {

	EnrollmentResult enroll(Long courseId, Long studentId);

	void cancel(Long courseId, Long studentId);

	String strategy();
}
