package com.example.lectureservice.controller;

import com.example.lectureservice.service.LectureService;
import com.example.lectureservice.dto.LectureCardDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/lectures")
@RequiredArgsConstructor
public class LectureController {

    private static final Logger logger = LoggerFactory.getLogger(LectureController.class);
    private final LectureService lectureService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createLecture(
            @RequestPart("title") String title,
            @RequestPart("description") String description,
            @RequestPart("category") String category,
            @RequestPart("instructorId") String instructorIdStr,
            @RequestPart("curriculum") String curriculumJson,
            @RequestPart(value = "thumbnailFile", required = false) MultipartFile thumbnailFile,
            @RequestPart(value = "videoFile", required = false) MultipartFile videoFile
    ) {
        try {
            logger.info("📥 요청 수신: title={}, instructorIdStr={}, curriculumJson={}",
                    title, instructorIdStr, curriculumJson);

            if (title.isBlank() || description.isBlank() || category.isBlank() || curriculumJson.isBlank()) {
                return ResponseEntity.badRequest().body("모든 필드를 입력해주세요.");
            }

            // instructorId 숫자 추출
            Long instructorId = 1L;
            if (instructorIdStr != null && !instructorIdStr.isEmpty()) {
                try {
                    instructorId = Long.parseLong(instructorIdStr.replaceAll("\\D+", ""));
                } catch (NumberFormatException e) {
                    logger.error("강사 ID 파싱 실패: {}", instructorIdStr);
                    return ResponseEntity.badRequest().body("유효한 강사 ID가 아닙니다.");
                }
            }

            // 핵심 로직 위임
            lectureService.saveLecture(title, description, category, instructorId,
                    curriculumJson, thumbnailFile, videoFile);

            return ResponseEntity.status(HttpStatus.CREATED).build();

        } catch (Exception e) {
            logger.error("강의 등록 중 오류", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("강의 등록 중 오류 발생: " + e.getMessage());
        }

    }
    @GetMapping
    public ResponseEntity<List<LectureCardDto>> getAllLectures() {
        List<LectureCardDto> lectures = lectureService.getAllLectureCards();
        return ResponseEntity.ok(lectures);
    }






}
