package com.example.course_lock_lab.enrollment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.course_lock_lab.course.entity.Course;
import com.example.course_lock_lab.course.repository.CourseRepository;
import com.example.course_lock_lab.enrollment.dto.EnrollmentResult;
import com.example.course_lock_lab.enrollment.entity.Enrollment;
import com.example.course_lock_lab.enrollment.repository.EnrollmentRepository;
import com.example.course_lock_lab.student.entity.Student;
import com.example.course_lock_lab.student.repository.StudentRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NoLockEnrollmentServiceTest {

	@Mock
	private CourseRepository courseRepository;

	@Mock
	private StudentRepository studentRepository;

	@Mock
	private EnrollmentRepository enrollmentRepository;

	private NoLockEnrollmentService enrollmentService;

	@BeforeEach
	void setUp() {
		enrollmentService = new NoLockEnrollmentService(
			courseRepository,
			studentRepository,
			enrollmentRepository,
			new EnrollmentMetrics(new SimpleMeterRegistry())
		);
	}

	@Test
	void enrollIncreasesCountAndCreatesEnrollment() {
		Course course = Course.create("popular course", 30);
		Student student = Student.create(1L, "student-1");

		when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
		when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
		when(enrollmentRepository.existsByStudentIdAndCourseId(1L, 10L)).thenReturn(false);
		when(enrollmentRepository.saveAndFlush(any(Enrollment.class))).thenAnswer(invocation -> invocation.getArgument(0));

		EnrollmentResult result = enrollmentService.enroll(10L, 1L);

		assertThat(result.courseId()).isEqualTo(10L);
		assertThat(result.studentId()).isEqualTo(1L);
		assertThat(result.strategy()).isEqualTo("no-lock");
		assertThat(course.getEnrolledCount()).isEqualTo(1);
	}

	@Test
	void enrollFailsWhenCapacityIsFull() {
		Course course = Course.create("popular course", 1);
		course.increaseEnrolledCount();

		when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
		when(studentRepository.findById(1L)).thenReturn(Optional.of(Student.create(1L, "student-1")));
		when(enrollmentRepository.existsByStudentIdAndCourseId(1L, 10L)).thenReturn(false);

		assertThatThrownBy(() -> enrollmentService.enroll(10L, 1L))
			.isInstanceOf(EnrollmentException.class)
			.hasMessageContaining("Course capacity is full");

		verify(enrollmentRepository, never()).saveAndFlush(any(Enrollment.class));
	}
}
