package com.example.course_lock_lab.enrollment.service;

import com.example.course_lock_lab.course.entity.Course;
import com.example.course_lock_lab.course.repository.CourseRepository;
import com.example.course_lock_lab.enrollment.dto.EnrollmentResult;
import com.example.course_lock_lab.enrollment.entity.Enrollment;
import com.example.course_lock_lab.enrollment.repository.EnrollmentRepository;
import com.example.course_lock_lab.student.entity.Student;
import com.example.course_lock_lab.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NoLockEnrollmentService implements EnrollmentService {

	private static final String STRATEGY = "no-lock";

	private final CourseRepository courseRepository;
	private final StudentRepository studentRepository;
	private final EnrollmentRepository enrollmentRepository;
	private final EnrollmentMetrics enrollmentMetrics;

	@Override
	@Transactional
	public EnrollmentResult enroll(Long courseId, Long studentId) {
		return enrollmentMetrics.record(STRATEGY, courseId, () -> enrollNoLock(courseId, studentId));
	}

	private EnrollmentResult enrollNoLock(Long courseId, Long studentId) {
		Course course = courseRepository.findById(courseId)
			.orElseThrow(() -> EnrollmentException.courseNotFound(courseId));

		studentRepository.findById(studentId)
			.orElseGet(() -> studentRepository.save(Student.create(studentId, "student-" + studentId)));

		if (enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
			throw EnrollmentException.duplicate(studentId, courseId);
		}
		if (course.isFull()) {
			throw EnrollmentException.capacityFull(courseId);
		}

		course.increaseEnrolledCount();
		try {
			Enrollment enrollment = enrollmentRepository.saveAndFlush(Enrollment.create(studentId, courseId));
			return EnrollmentResult.from(enrollment, STRATEGY);
		} catch (DataIntegrityViolationException exception) {
			throw EnrollmentException.duplicate(studentId, courseId);
		}
	}

	@Override
	@Transactional
	public void cancel(Long courseId, Long studentId) {
		enrollmentMetrics.recordVoid(STRATEGY, courseId, () -> cancelNoLock(courseId, studentId));
	}

	private void cancelNoLock(Long courseId, Long studentId) {
		Course course = courseRepository.findById(courseId)
			.orElseThrow(() -> EnrollmentException.courseNotFound(courseId));
		Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
			.orElseThrow(() -> EnrollmentException.enrollmentNotFound(studentId, courseId));

		enrollmentRepository.delete(enrollment);
		course.decreaseEnrolledCount();
	}

	@Override
	public String strategy() {
		return STRATEGY;
	}
}
