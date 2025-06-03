package com.example.lectureservice.service;

import com.example.lectureservice.config.client.AuthServiceClient;
import com.example.lectureservice.dto.InstructorProfileResponseDTO;
import com.example.lectureservice.dto.LectureCardDTO;
import com.example.lectureservice.dto.LectureDetailResponseDTO;
import com.example.lectureservice.dto.LectureResponse;
import com.example.lectureservice.entity.Lecture;
import com.example.lectureservice.entity.LectureContent;
import com.example.lectureservice.entity.LectureStatus;
import com.example.lectureservice.repository.LectureContentRepository;
import com.example.lectureservice.repository.LectureRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@Slf4j
public class LectureService {

    private final LectureRepository lectureRepository;
    private final LectureContentRepository lectureContentRepository;
    private final FileStorageService fileStorageService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AuthServiceClient authServiceClient;

    public void saveLecture(
            String accessToken,
            Long id,
            String title,
            String description,
            String category,
            String curriculum,
            MultipartFile thumbnailFile,
            MultipartFile videoFile
    ) {
        List<Map<String, Object>> curriculumList;
        try {
            curriculumList = objectMapper.readValue(curriculum, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("\u274c 커리큘럼 JSON 파싱 실패", e);
            throw new IllegalArgumentException("커리큘럼 형식이 올바르지 않습니다.");
        }
        System.out.println("강사id::"+id);

        String instructorName = authServiceClient.getTeacherName(accessToken, id);
        System.out.println("강사이름:: "+instructorName);

        Lecture lecture = Lecture.builder()
                .title(title)
                .instructorName(instructorName)
                .description(description)
                .category(category)
                .instructorId(id)
                .price(4000)
                .status(LectureStatus.PENDING)
                .build();

        Lecture savedLecture = lectureRepository.save(lecture);

        try {
            if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
                String ext = getExtension(thumbnailFile.getOriginalFilename());
                String fileName = String.format("thumbnail_%d_%d.%s", savedLecture.getId(), id, ext);
                String thumbnailUrl = fileStorageService.saveFile(thumbnailFile, "thumbnails", fileName);
                savedLecture.setThumbnailUrl(thumbnailUrl);
            }
            if (videoFile != null && !videoFile.isEmpty()) {
                String ext = getExtension(videoFile.getOriginalFilename());
                String fileName = String.format("lecture_%d_%d.%s", savedLecture.getId(), id, ext);
                String videoUrl = fileStorageService.saveFile(videoFile, "videos", fileName);
                savedLecture.setVideoUrl(videoUrl);
            }

            lectureRepository.save(savedLecture);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 중 오류가 발생했습니다.", e);
        }

        List<LectureContent> contentList = curriculumList.stream().map(item -> {
            String contentTitle = (String) item.get("title");
            String contentBody = (String) item.getOrDefault("content", null);
            return LectureContent.builder()
                    .lectureId(savedLecture.getId())
                    .section(0)
                    .title(contentTitle)
                    .content(contentBody)
                    .build();
        }).collect(Collectors.toList());

        lectureContentRepository.saveAll(contentList);
    }

    private String getExtension(String filename) {
        return filename.substring(filename.lastIndexOf('.') + 1);
    }

    public List<LectureCardDTO> getAllLectureCards() {
        List<Lecture> lectures = lectureRepository.findAll();

        return lectures.stream()
                .map(lecture -> LectureCardDTO.builder()
                        .lectureId(lecture.getId())
                        .title(lecture.getTitle())
                        .category(lecture.getCategory())
                        .thumbnailUrl(lecture.getThumbnailUrl())
                        .instructorName(lecture.getInstructorName())
                        .rating(4.5)
                        .bookmarkCount(0)
                        .price(lecture.getPrice())
                        .build())
                .collect(Collectors.toList());
    }

    public ResponseEntity<LectureDetailResponseDTO> getLectureDetail(String accessToken, Long lectureId) {
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new IllegalArgumentException("해당 강의를 찾을 수 없습니다."));

        List<LectureContent> contentList = lectureContentRepository.findByLectureIdOrderBySectionAsc(lectureId);

        List<LectureDetailResponseDTO.LectureContent> lectureContents = contentList.stream()
                .map(c -> LectureDetailResponseDTO.LectureContent.builder()
                        .title(c.getTitle())
                        .content(c.getContent())
                        .videoUrl(null)
                        .build())
                .collect(Collectors.toList());

        InstructorProfileResponseDTO instructor = authServiceClient.getInstructorProfile(accessToken, lecture.getInstructorId());

        LectureDetailResponseDTO response = LectureDetailResponseDTO.builder()
                .id(lecture.getId())
                .title(lecture.getTitle())
                .description(lecture.getDescription())
                .instructor(instructor.getName())
                .image(lecture.getThumbnailUrl())
                .videoUrl(lecture.getVideoUrl())
                .instructorImage(instructor.getProfileImage())
                .category(lecture.getCategory())
                .rating(instructor.getRating() != null ? instructor.getRating() : 4.5)
                .reviewCount(2)
                .studentCount(500)
                .price(lecture.getPrice())
                .bookmarks(0)
                .duration("18시간")
                .level("초급")
                .isAI(false)
                .isPurchased(true)
                .views(0)
                .lectureContent(List.of(
                        LectureDetailResponseDTO.Section.builder()
                                .section(0)
                                .lectures(lectureContents)
                                .build()
                ))
                .reviews(List.of(
                        LectureDetailResponseDTO.Review.builder().name("이학생").rating(5).comment("좋아요").build(),
                        LectureDetailResponseDTO.Review.builder().name("박개발").rating(4).comment("유익해요").build()
                ))
                .build();

        return ResponseEntity.ok(response);
    }

    public LectureResponse findById(Long id) {
        Lecture lecture = lectureRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "강의를 찾을 수 없습니다."));
        return new LectureResponse(lecture.getId(), lecture.getTitle(), lecture.getInstructorName(), lecture.getPrice());
    }
}
