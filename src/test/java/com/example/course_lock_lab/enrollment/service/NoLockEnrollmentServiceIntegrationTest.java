package com.example.course_lock_lab.enrollment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.course_lock_lab.course.entity.Course;
import com.example.course_lock_lab.course.repository.CourseRepository;
import com.example.course_lock_lab.enrollment.repository.EnrollmentRepository;
import com.example.course_lock_lab.student.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class NoLockEnrollmentServiceIntegrationTest {

	@Autowired
	private EnrollmentService enrollmentService;

	@Autowired
	private CourseRepository courseRepository;

	@Autowired
	private EnrollmentRepository enrollmentRepository;

	@Autowired
	private StudentRepository studentRepository;

	@BeforeEach
	void setUp() {
		enrollmentRepository.deleteAllInBatch();
		courseRepository.deleteAllInBatch();
		studentRepository.deleteAllInBatch();
	}

	@Test
	void enrollCreatesEnrollmentAndIncreasesEnrolledCount() {
		Course course = courseRepository.save(Course.create("popular course", 30));

		enrollmentService.enroll(course.getId(), 1L);

		Course savedCourse = courseRepository.findById(course.getId()).orElseThrow();
		assertThat(savedCourse.getEnrolledCount()).isEqualTo(1);
		assertThat(enrollmentRepository.countByCourseId(course.getId())).isEqualTo(1);
		assertThat(enrollmentRepository.existsByStudentIdAndCourseId(1L, course.getId())).isTrue();
	}

	@Test
	void enrollFailsWhenStudentAlreadyEnrolledInSameCourse() {
		Course course = courseRepository.save(Course.create("popular course", 30));
		enrollmentService.enroll(course.getId(), 1L);

		assertThatThrownBy(() -> enrollmentService.enroll(course.getId(), 1L))
			.isInstanceOf(EnrollmentException.class)
			.hasMessageContaining("Student already enrolled");

		Course savedCourse = courseRepository.findById(course.getId()).orElseThrow();
		assertThat(savedCourse.getEnrolledCount()).isEqualTo(1);
		assertThat(enrollmentRepository.countByCourseId(course.getId())).isEqualTo(1);
	}

	@Test
	void enrollFailsWhenCourseCapacityIsFull() {
		Course course = courseRepository.save(Course.create("popular course", 1));
		enrollmentService.enroll(course.getId(), 1L);

		assertThatThrownBy(() -> enrollmentService.enroll(course.getId(), 2L))
			.isInstanceOf(EnrollmentException.class)
			.hasMessageContaining("Course capacity is full");

		Course savedCourse = courseRepository.findById(course.getId()).orElseThrow();
		assertThat(savedCourse.getEnrolledCount()).isEqualTo(1);
		assertThat(enrollmentRepository.countByCourseId(course.getId())).isEqualTo(1);
	}

	@Test
	void cancelDeletesEnrollmentAndDecreasesEnrolledCount() {
		Course course = courseRepository.save(Course.create("popular course", 30));
		enrollmentService.enroll(course.getId(), 1L);

		enrollmentService.cancel(course.getId(), 1L);

		Course savedCourse = courseRepository.findById(course.getId()).orElseThrow();
		assertThat(savedCourse.getEnrolledCount()).isZero();
		assertThat(enrollmentRepository.countByCourseId(course.getId())).isZero();
		assertThat(enrollmentRepository.existsByStudentIdAndCourseId(1L, course.getId())).isFalse();
	}
}
