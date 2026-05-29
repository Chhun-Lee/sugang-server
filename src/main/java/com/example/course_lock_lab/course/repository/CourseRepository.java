package com.example.course_lock_lab.course.repository;

import com.example.course_lock_lab.course.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
}
