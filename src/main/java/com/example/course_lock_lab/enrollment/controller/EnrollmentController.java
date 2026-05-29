package com.example.course_lock_lab.enrollment.controller;

import com.example.course_lock_lab.enrollment.dto.EnrollmentRequest;
import com.example.course_lock_lab.enrollment.dto.EnrollmentResponse;
import com.example.course_lock_lab.enrollment.dto.StudentEnrollmentResponse;
import com.example.course_lock_lab.enrollment.repository.EnrollmentRepository;
import com.example.course_lock_lab.enrollment.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "Enrollment", description = "수강신청, 취소, 학생별 신청 내역 API")
public class EnrollmentController {

	private final EnrollmentService enrollmentService;
	private final EnrollmentRepository enrollmentRepository;

	@Operation(summary = "수강신청", description = "지정한 강의에 학생을 수강신청합니다. 현재 기본 전략은 no-lock입니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "수강신청 성공"),
		@ApiResponse(responseCode = "400", description = "요청 값 검증 실패"),
		@ApiResponse(responseCode = "404", description = "강의 없음"),
		@ApiResponse(responseCode = "409", description = "정원 초과 또는 중복 신청")
	})
	@PostMapping("/courses/{courseId}/enroll")
	public EnrollmentResponse enroll(
		@Parameter(description = "수강신청할 강의 ID", example = "1")
		@PathVariable @Positive Long courseId,
		@Valid @RequestBody EnrollmentRequest request
	) {
		return EnrollmentResponse.from(enrollmentService.enroll(courseId, request.studentId()));
	}

	@Operation(summary = "수강신청 취소", description = "지정한 강의에서 학생의 수강신청을 취소합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "204", description = "수강신청 취소 성공"),
		@ApiResponse(responseCode = "400", description = "요청 값 검증 실패"),
		@ApiResponse(responseCode = "404", description = "강의 또는 수강신청 없음")
	})
	@DeleteMapping("/courses/{courseId}/enroll")
	public ResponseEntity<Void> cancel(
		@Parameter(description = "수강신청을 취소할 강의 ID", example = "1")
		@PathVariable @Positive Long courseId,
		@Parameter(description = "학생 ID", example = "1")
		@RequestParam @Positive Long studentId
	) {
		enrollmentService.cancel(courseId, studentId);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "학생별 수강신청 내역 조회", description = "학생 ID로 수강신청 내역을 최신순으로 조회합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "수강신청 내역 조회 성공"),
		@ApiResponse(responseCode = "400", description = "요청 값 검증 실패")
	})
	@GetMapping("/students/{studentId}/enrollments")
	public List<StudentEnrollmentResponse> findEnrollments(
		@Parameter(description = "학생 ID", example = "1")
		@PathVariable @Positive Long studentId
	) {
		return enrollmentRepository.findByStudentIdOrderByCreatedAtDesc(studentId).stream()
			.map(StudentEnrollmentResponse::from)
			.toList();
	}
}
