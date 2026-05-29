package com.example.course_lock_lab.course.controller;

import com.example.course_lock_lab.course.dto.CourseResponse;
import com.example.course_lock_lab.course.dto.CreateCourseRequest;
import com.example.course_lock_lab.course.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
@Tag(name = "Course", description = "강의 생성 및 조회 API")
public class CourseController {

	private final CourseService courseService;

	@Operation(summary = "강의 생성", description = "수강신청 대상 강의를 생성합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "201", description = "강의 생성 성공"),
		@ApiResponse(responseCode = "400", description = "요청 값 검증 실패")
	})
	@PostMapping
	public ResponseEntity<CourseResponse> createCourse(@Valid @RequestBody CreateCourseRequest request) {
		CourseResponse response = CourseResponse.from(courseService.create(request.title(), request.capacity()));
		return ResponseEntity.created(URI.create("/courses/" + response.id())).body(response);
	}

	@Operation(summary = "강의 목록 조회", description = "현재 등록된 강의 목록을 조회합니다.")
	@ApiResponse(responseCode = "200", description = "강의 목록 조회 성공")
	@GetMapping
	public List<CourseResponse> findCourses() {
		return courseService.findAll().stream()
			.map(CourseResponse::from)
			.toList();
	}
}
