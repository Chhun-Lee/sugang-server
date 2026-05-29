package com.example.course_lock_lab.course.service;

import com.example.course_lock_lab.course.entity.Course;
import com.example.course_lock_lab.course.repository.CourseRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseService {

	private final CourseRepository courseRepository;

	@Transactional
	public Course create(String title, int capacity) {
		return courseRepository.save(Course.create(title, capacity));
	}

	@Transactional(readOnly = true)
	public List<Course> findAll() {
		return courseRepository.findAll();
	}
}
