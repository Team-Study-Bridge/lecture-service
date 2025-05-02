package com.example.lectureservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LectureCardDTO {
    private Long lectureId;         // 강의 ID
    private String title;           // 강의 제목
    private String category;        // 카테고리
    private String thumbnailUrl;    // 썸네일 이미지 URL
    private String instructorName;  // 강사 이름
    private double rating;          // 평점 (하드코딩 또는 나중에 계산)
    private int bookmarkCount;      // 북마크 수 (하드코딩 또는 추후 개발)
    private double price;           // 가격
}
