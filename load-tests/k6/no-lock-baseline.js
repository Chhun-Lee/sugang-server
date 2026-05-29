import http from 'k6/http';
import { check } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const CAPACITY = Number(__ENV.CAPACITY || 30);
const STUDENTS = Number(__ENV.STUDENTS || 1000);
const COURSE_TITLE = __ENV.COURSE_TITLE || `k6 no-lock baseline ${Date.now()}`;

const enrollSuccess = new Counter('enroll_success_total');
const capacityFull = new Counter('enroll_capacity_full_total');
const duplicate = new Counter('enroll_duplicate_total');
const unexpectedFailure = new Counter('enroll_unexpected_failure_total');
const enrollFailed = new Rate('enroll_failed_rate');
const enrollDuration = new Trend('enroll_duration', true);

export const options = {
	scenarios: {
		concurrent_enrollment: {
			executor: 'per-vu-iterations',
			vus: STUDENTS,
			iterations: 1,
			maxDuration: '2m',
		},
	},
	summaryTrendStats: ['avg', 'min', 'med', 'p(90)', 'p(95)', 'p(99)', 'max'],
	thresholds: {
		enroll_success_total: [`count<=${CAPACITY}`],
		enroll_duplicate_total: ['count==0'],
		enroll_unexpected_failure_total: ['count==0'],
	},
};

export function setup() {
	const response = http.post(
		`${BASE_URL}/courses`,
		JSON.stringify({
			title: COURSE_TITLE,
			capacity: CAPACITY,
		}),
		{
			headers: {
				'Content-Type': 'application/json',
			},
		}
	);

	check(response, {
		'course created': (res) => res.status === 201,
	});

	if (response.status !== 201) {
		throw new Error(`Failed to create course. status=${response.status}, body=${response.body}`);
	}

	const course = response.json();
	return {
		courseId: course.id,
		capacity: course.capacity,
		students: STUDENTS,
	};
}

export default function (data) {
	const studentId = __VU;
	const response = http.post(
		`${BASE_URL}/courses/${data.courseId}/enroll`,
		JSON.stringify({
			studentId,
		}),
		{
			headers: {
				'Content-Type': 'application/json',
			},
			tags: {
				api: 'enroll',
				strategy: 'no-lock',
			},
		}
	);

	enrollDuration.add(response.timings.duration);

	const body = parseJson(response);
	const reason = body && body.reason;

	if (response.status === 200) {
		enrollSuccess.add(1);
		enrollFailed.add(false);
	} else if (response.status === 409 && reason === 'capacity_full') {
		capacityFull.add(1);
		enrollFailed.add(true);
	} else if (response.status === 409 && reason === 'duplicate') {
		duplicate.add(1);
		enrollFailed.add(true);
	} else {
		unexpectedFailure.add(1);
		enrollFailed.add(true);
	}

	check(response, {
		'enroll result is expected': (res) =>
			res.status === 200 ||
			(res.status === 409 && (reason === 'capacity_full' || reason === 'duplicate')),
	});
}

export function teardown(data) {
	const course = findCourse(data.courseId);
	const enrollmentRowCount = countEnrollmentsByCourse(data.courseId, data.students);

	const enrolledCount = course ? course.enrolledCount : null;
	const exceedsCapacity = enrolledCount !== null && enrolledCount > data.capacity;
	const countMismatch = enrolledCount !== null && enrolledCount !== enrollmentRowCount;

	console.log('');
	console.log('No Lock baseline verification');
	console.log(`courseId=${data.courseId}`);
	console.log(`capacity=${data.capacity}`);
	console.log(`students=${data.students}`);
	console.log(`course.enrolledCount=${enrolledCount}`);
	console.log(`enrollment row count inferred from student APIs=${enrollmentRowCount}`);
	console.log(`enrolledCount exceeds capacity=${exceedsCapacity}`);
	console.log(`enrolledCount and enrollment row count mismatch=${countMismatch}`);
	console.log('');
}

function findCourse(courseId) {
	const response = http.get(`${BASE_URL}/courses`);
	if (response.status !== 200) {
		console.error(`Failed to fetch courses. status=${response.status}, body=${response.body}`);
		return null;
	}

	const courses = response.json();
	return courses.find((course) => course.id === courseId) || null;
}

function countEnrollmentsByCourse(courseId, students) {
	let count = 0;

	for (let studentId = 1; studentId <= students; studentId += 1) {
		const response = http.get(`${BASE_URL}/students/${studentId}/enrollments`);
		if (response.status !== 200) {
			console.error(`Failed to fetch enrollments. studentId=${studentId}, status=${response.status}`);
			continue;
		}

		const enrollments = response.json();
		count += enrollments.filter((enrollment) => enrollment.courseId === courseId).length;
	}

	return count;
}

function parseJson(response) {
	if (!response.body) {
		return null;
	}

	try {
		return response.json();
	} catch (error) {
		return null;
	}
}
