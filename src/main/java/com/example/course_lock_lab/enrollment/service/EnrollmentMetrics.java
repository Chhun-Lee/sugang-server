package com.example.course_lock_lab.enrollment.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EnrollmentMetrics {

	private final MeterRegistry meterRegistry;

	public <T> T record(String strategy, Long courseId, Supplier<T> supplier) {
		long start = System.nanoTime();
		try {
			T result = supplier.get();
			record(strategy, courseId, "success", "none", start);
			return result;
		} catch (EnrollmentException exception) {
			record(strategy, courseId, "fail", exception.reason(), start);
			throw exception;
		} catch (RuntimeException exception) {
			record(strategy, courseId, "fail", "unknown", start);
			throw exception;
		}
	}

	public void recordVoid(String strategy, Long courseId, Runnable runnable) {
		record(strategy, courseId, () -> {
			runnable.run();
			return null;
		});
	}

	private void record(String strategy, Long courseId, String result, String reason, long startNanos) {
		String courseIdTag = String.valueOf(courseId);
		meterRegistry.counter(
			"course.enrollment.requests",
			"strategy", strategy,
			"result", result,
			"reason", reason,
			"courseId", courseIdTag
		).increment();
		Timer.builder("course.enrollment.duration")
			.tags(
				"strategy", strategy,
				"result", result,
				"reason", reason,
				"courseId", courseIdTag
			)
			.register(meterRegistry)
			.record(System.nanoTime() - startNanos, java.util.concurrent.TimeUnit.NANOSECONDS);
	}
}
