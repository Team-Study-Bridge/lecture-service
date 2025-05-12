package com.example.lectureservice.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VideoInfoResponseDTO {
    private String title;
    private String duration;
    private String instructorName;
    private String thumbnailUrl;
}
