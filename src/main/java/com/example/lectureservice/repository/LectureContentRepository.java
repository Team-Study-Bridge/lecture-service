package com.example.lectureservice.repository;

import com.example.lectureservice.entity.LectureContent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LectureContentRepository extends JpaRepository<LectureContent, Long> {
    List<LectureContent> findByLectureIdOrderBySectionAsc(Long lectureId);
}
