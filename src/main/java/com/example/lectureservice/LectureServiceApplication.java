package com.example.lectureservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.lectureservice.repository")
@EntityScan(basePackages = "com.example.lectureservice.entity")
public class LectureServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LectureServiceApplication.class, args);
    }

}
