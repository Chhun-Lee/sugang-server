package com.example.course_lock_lab.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

	@Bean
	public OpenAPI courseLockLabOpenAPI() {
		return new OpenAPI()
			.info(new Info()
				.title("course-lock-lab API")
				.description("Course enrollment concurrency-control lab API")
				.version("v1")
				.license(new License().name("Private Project")));
	}
}
