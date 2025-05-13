package com.example.lectureservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LectureDetailResponseDTO {
    private boolean success = true;
    private String message = "강의 조회 성공";
    private Long id;
    private String title;
    private String description;
    private String instructor;
    private String image;
    private String instructorImage;
    private String category;
    private double rating;
    private int reviewCount;
    private int studentCount;
    private int price;
    private int bookmarks;
    private String duration;
    private String level;
    private boolean isAI;
    private boolean isPurchased;
    private int views;
    private String videoUrl;
    private List<Section> lectureContent;
    private List<Review> reviews;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Section {
        private int section;
        private List<LectureContent> lectures;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LectureContent {
        private String title;
        private String content;
        private String videoUrl;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Review {
        private String name;
        private int rating;
        private String comment;
    }
}
