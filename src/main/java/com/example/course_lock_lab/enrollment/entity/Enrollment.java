package com.example.course_lock_lab.enrollment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
	name = "enrollments",
	uniqueConstraints = @UniqueConstraint(
		name = "uk_enrollments_student_course",
		columnNames = {"student_id", "course_id"}
	)
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Enrollment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long studentId;

	@Column(nullable = false)
	private Long courseId;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	private Enrollment(Long studentId, Long courseId) {
		this.studentId = studentId;
		this.courseId = courseId;
	}

	public static Enrollment create(Long studentId, Long courseId) {
		return new Enrollment(studentId, courseId);
	}

	@PrePersist
	void prePersist() {
		this.createdAt = LocalDateTime.now();
	}
}
