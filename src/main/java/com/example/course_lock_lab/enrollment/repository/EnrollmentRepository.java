package com.example.course_lock_lab.enrollment.repository;

import com.example.course_lock_lab.enrollment.entity.Enrollment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

	boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

	long countByCourseId(Long courseId);

	Optional<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);

	List<Enrollment> findByStudentIdOrderByCreatedAtDesc(Long studentId);
}
