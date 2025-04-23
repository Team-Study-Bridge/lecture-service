package com.example.lectureservice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TranscriptRequest {
    private String videoPath;  // 비디오 경로 추가
    private String transcript; // 텍스트
   }
