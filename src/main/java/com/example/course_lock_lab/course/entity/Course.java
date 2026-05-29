package com.example.course_lock_lab.course.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "courses")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Course {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 200)
	private String title;

	@Column(nullable = false)
	private int capacity;

	@Column(nullable = false)
	private int enrolledCount;

	@Column(nullable = false)
	private long version;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(nullable = false)
	private LocalDateTime updatedAt;

	private Course(String title, int capacity) {
		this.title = title;
		this.capacity = capacity;
		this.enrolledCount = 0;
		this.version = 0;
	}

	public static Course create(String title, int capacity) {
		return new Course(title, capacity);
	}

	public boolean isFull() {
		return enrolledCount >= capacity;
	}

	public void increaseEnrolledCount() {
		this.enrolledCount++;
	}

	public void decreaseEnrolledCount() {
		if (this.enrolledCount > 0) {
			this.enrolledCount--;
		}
	}

	@PrePersist
	void prePersist() {
		LocalDateTime now = LocalDateTime.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	void preUpdate() {
		this.updatedAt = LocalDateTime.now();
	}
}
