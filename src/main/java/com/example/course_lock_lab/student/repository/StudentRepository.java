package com.example.course_lock_lab.student.repository;

import com.example.course_lock_lab.student.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {
}
