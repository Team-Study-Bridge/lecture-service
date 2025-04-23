package com.example.lectureservice.service;

import com.example.lectureservice.dto.LectureCardDto;
import com.example.lectureservice.entity.Lecture;
import com.example.lectureservice.entity.LectureContent;
import com.example.lectureservice.entity.LectureStatus;
import com.example.lectureservice.entity.Teacher;
import com.example.lectureservice.entity.User;
import com.example.lectureservice.repository.LectureContentRepository;
import com.example.lectureservice.repository.LectureRepository;
import com.example.lectureservice.repository.TeacherRepository;
import com.example.lectureservice.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LectureService {

    private final LectureRepository lectureRepository;
    private final LectureContentRepository lectureContentRepository;
    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void saveLecture(
            String title,
            String description,
            String category,
            Long instructorId,
            String curriculumJson,
            MultipartFile thumbnailFile,
            MultipartFile videoFile
    ) {
        // ✅ 1. 커리큘럼 파싱
        List<Map<String, Object>> curriculumList;
        try {
            curriculumList = objectMapper.readValue(curriculumJson, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("❌ 커리큘럼 JSON 파싱 실패", e);
            throw new IllegalArgumentException("커리큘럼 형식이 올바르지 않습니다.");
        }

        // ✅ 2. 각 커리큘럼 항목 필드 확인
        for (Map<String, Object> item : curriculumList) {
            if (!item.containsKey("title") || ((String) item.get("title")).isBlank()) {
                throw new IllegalArgumentException("각 커리큘럼 항목은 제목(title)을 포함해야 합니다.");
            }
        }

        // ✅ 3. 파일 저장
        String thumbnailUrl = null;
        String videoUrl = null;

        try {
            if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
                thumbnailUrl = fileStorageService.saveFile(thumbnailFile, "thumbnails");
            }
            if (videoFile != null && !videoFile.isEmpty()) {
                videoUrl = fileStorageService.saveFile(videoFile, "videos");
            }
        } catch (IOException e) {
            log.error("❌ 파일 저장 실패", e);
            throw new RuntimeException("파일 저장 중 오류가 발생했습니다.");
        }

        // ✅ 4. Lecture 저장
        Lecture lecture = Lecture.builder()
                .title(title)
                .description(description)
                .category(category)
                .instructorId(instructorId)
                .price(0.0)
                .thumbnailUrl(thumbnailUrl)
                .status(LectureStatus.PENDING)
                .build();

        Lecture savedLecture = lectureRepository.save(lecture);

        // ✅ 5. 커리큘럼 → LectureContent 변환 및 저장
        List<LectureContent> contentList = new ArrayList<>();
        for (Map<String, Object> item : curriculumList) {
            int section = (Integer) item.getOrDefault("section", 0);
            String contentTitle = (String) item.get("title");
            String contentBody = (String) item.getOrDefault("content", null);

            LectureContent content = LectureContent.builder()
                    .lecture(savedLecture)
                    .section(section)
                    .title(contentTitle)
                    .content(contentBody)
                    .videoUrl(null) // 섹션별 영상은 추후 확장
                    .build();

            contentList.add(content);
        }

        lectureContentRepository.saveAll(contentList);
    }

    public List<LectureCardDto> getAllLectureCards() {
        List<Lecture> lectures = lectureRepository.findAll();

        return lectures.stream()
                .map(lecture -> {
                    try {
                        Teacher teacher = teacherRepository.findById(lecture.getInstructorId())
                                .orElseThrow(() -> new IllegalArgumentException("강사 정보가 없습니다."));

                        User user = userRepository.findById(teacher.getUserId())
                                .orElseThrow(() -> new IllegalArgumentException("사용자 정보가 없습니다."));

                        return LectureCardDto.builder()
                                .lectureId(lecture.getId())
                                .title(lecture.getTitle())
                                .category(lecture.getCategory())
                                .thumbnailUrl(lecture.getThumbnailUrl())
                                .instructorName(user.getNickname())
                                .rating(4.5)
                                .bookmarkCount(0)
                                .price(lecture.getPrice())
                                .build();
                    } catch (Exception e) {
                        log.warn("강사 정보 누락 - 강의 ID: {}, instructorId: {}", lecture.getId(), lecture.getInstructorId());
                        return null;
                    }
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

}
