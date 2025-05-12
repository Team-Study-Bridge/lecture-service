package com.example.lectureservice.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@TestConfiguration
@ComponentScan(
        basePackages = "com.example.lectureservice",
        excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*(AwsS3Config|WebClientConfig|FileStorageService).*")
)
public class TestConfig {
}
