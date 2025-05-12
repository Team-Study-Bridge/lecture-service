package com.example.lectureservice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public class TeacherGetNameResponseDTO {
    private boolean success;
    private String message;
    private String instructorName;
}
