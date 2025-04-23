package com.example.lectureservice.repository;

import com.example.lectureservice.entity.LectureContent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LectureContentRepository extends JpaRepository<LectureContent, Long> {
}
