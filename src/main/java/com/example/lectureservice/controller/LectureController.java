package com.example.lectureservice.controller;

import com.example.lectureservice.dto.LectureCardDTO;
import com.example.lectureservice.dto.LectureDetailResponseDTO;
import com.example.lectureservice.service.LectureService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/lectures")
@RequiredArgsConstructor
public class LectureController {

    private static final Logger logger = LoggerFactory.getLogger(LectureController.class);
    private final LectureService lectureService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> createLecture(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String category,
            @RequestParam String curriculum,
            @RequestParam MultipartFile thumbnailFile,
            @RequestParam MultipartFile videoFile,
            @RequestHeader("X-User-Token") String accessToken,
            @RequestHeader("X-User-Id") Long instructorId
    ) {
        lectureService.saveLecture(accessToken, instructorId, title, description, category, curriculum, thumbnailFile, videoFile);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<LectureCardDTO>> getAllLectures() {
        List<LectureCardDTO> lectures = lectureService.getAllLectureCards();
        return ResponseEntity.ok(lectures);
    }

    @GetMapping("/detail/{lectureId}")
    public ResponseEntity<LectureDetailResponseDTO> getDetailLecture(
            @RequestHeader("X-User-Token") String accessToken,
            @PathVariable Long lectureId
    ) {
        return lectureService.getLectureDetail(accessToken, lectureId);
    }
}