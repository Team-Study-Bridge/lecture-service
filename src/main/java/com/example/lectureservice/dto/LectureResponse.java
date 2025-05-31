package com.example.lectureservice.dto;

public record LectureResponse(
        Long id,
        String title,
        String instructorName,
        Integer price
) {
}
