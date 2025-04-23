package com.example.lectureservice.service;

import com.example.lectureservice.entity.Lecture;
import com.example.lectureservice.entity.LectureStatus;
import com.example.lectureservice.repository.LectureContentRepository;
import com.example.lectureservice.repository.LectureRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LectureServiceTest {

    @InjectMocks
    private LectureService lectureService;

    @Mock
    private LectureRepository lectureRepository;

    @Mock
    private LectureContentRepository lectureContentRepository;

    @Mock
    private FileStorageService fileStorageService;

    // 1. 정상 케이스: 강의 저장이 정상 동작하는 경우
    @Test
    public void testSaveLecture_Success() throws IOException {
        // Arrange
        String name = "Test Lecture";
        String description = "Test Description";
        String category = "Test Category";
        Long instructorId = 1L;
        String curriculumJson = "[{\"section\": 1, \"title\": \"Introduction\", \"content\": \"Intro content\"}]";

        MultipartFile thumbnail = new MockMultipartFile("thumbnail", "thumbnail.jpg", "image/jpeg", "thumbnail content".getBytes());
        MultipartFile video = new MockMultipartFile("video", "video.mp4", "video/mp4", "video content".getBytes());

        String thumbnailUrl = "http://example.com/thumbnail.jpg";
        String videoUrl = "http://example.com/video.mp4";
        when(fileStorageService.saveFile(thumbnail, "thumbnails")).thenReturn(thumbnailUrl);
        when(fileStorageService.saveFile(video, "videos")).thenReturn(videoUrl);

        Lecture lecture = Lecture.builder()
                .instructorId(instructorId)
                .title(name)
                .description(description)
                .price(0.0)
                .category(category)
                .thumbnailUrl(thumbnailUrl)
                .status(LectureStatus.PENDING)
                .build();
        when(lectureRepository.save(any(Lecture.class))).thenReturn(lecture);

        // Act
        lectureService.saveLecture(name, description, category, instructorId, curriculumJson, thumbnail, video);

        // Assert: 각 의존성이 정상적으로 호출되는지 확인
        verify(fileStorageService, times(1)).saveFile(thumbnail, "thumbnails");
        verify(fileStorageService, times(1)).saveFile(video, "videos");
        verify(lectureRepository, times(1)).save(any(Lecture.class));
        verify(lectureContentRepository, times(1)).saveAll(any(List.class));
    }

    // 2. 썸네일 저장 실패 시: 파일 저장에서 IOException 발생하는 경우
    @Test
    public void testSaveLecture_ThumbnailSaveFailure() throws IOException {
        // Arrange
        String name = "Test Lecture";
        String description = "Test Description";
        String category = "Test Category";
        Long instructorId = 1L;
        String curriculumJson = "[{\"section\": 1, \"title\": \"Introduction\", \"content\": \"Intro content\"}]";

        MultipartFile thumbnail = new MockMultipartFile("thumbnail", "thumbnail.jpg", "image/jpeg", "thumbnail content".getBytes());
        MultipartFile video = new MockMultipartFile("video", "video.mp4", "video/mp4", "video content".getBytes());

        when(fileStorageService.saveFile(thumbnail, "thumbnails"))
                .thenThrow(new IOException("File storage error"));

        // Act & Assert
        IOException exception = assertThrows(IOException.class, () ->
                lectureService.saveLecture(name, description, category, instructorId, curriculumJson, thumbnail, video));
        assertEquals("썸네일 파일 저장에 실패했습니다.", exception.getMessage());

        verify(fileStorageService, times(1)).saveFile(thumbnail, "thumbnails");
        verify(fileStorageService, never()).saveFile(video, "videos");
        verify(lectureRepository, never()).save(any());
        verify(lectureContentRepository, never()).saveAll(any());
    }

    // 3. 영상 파일 저장 실패 시: 영상 파일 저장에서 IOException 발생하는 경우
    @Test
    public void testSaveLecture_VideoSaveFailure() throws IOException {
        // Arrange
        String name = "Test Lecture";
        String description = "Test Description";
        String category = "Test Category";
        Long instructorId = 1L;
        String curriculumJson = "[{\"section\": 1, \"title\": \"Introduction\", \"content\": \"Intro content\"}]";

        MultipartFile thumbnail = new MockMultipartFile("thumbnail", "thumbnail.jpg", "image/jpeg", "thumbnail content".getBytes());
        MultipartFile video = new MockMultipartFile("video", "video.mp4", "video/mp4", "video content".getBytes());

        String thumbnailUrl = "http://example.com/thumbnail.jpg";
        when(fileStorageService.saveFile(thumbnail, "thumbnails")).thenReturn(thumbnailUrl);
        when(fileStorageService.saveFile(video, "videos"))
                .thenThrow(new IOException("Video file storage error"));

        // Act & Assert
        IOException exception = assertThrows(IOException.class, () ->
                lectureService.saveLecture(name, description, category, instructorId, curriculumJson, thumbnail, video)
        );
        assertEquals("영상 파일 저장에 실패했습니다.", exception.getMessage());

        verify(fileStorageService, times(1)).saveFile(thumbnail, "thumbnails");
        verify(fileStorageService, times(1)).saveFile(video, "videos");
        verify(lectureRepository, never()).save(any());
        verify(lectureContentRepository, never()).saveAll(any());
    }

    // 4. 잘못된 커리큘럼 JSON: JSON 파싱 실패하는 경우
    @Test
    public void testSaveLecture_InvalidCurriculumJson() throws IOException {
        // Arrange
        String name = "Test Lecture";
        String description = "Test Description";
        String category = "Test Category";
        Long instructorId = 1L;
        String curriculumJson = "invalid json";

        MultipartFile thumbnail = new MockMultipartFile("thumbnail", "thumbnail.jpg", "image/jpeg", "thumbnail content".getBytes());
        MultipartFile video = new MockMultipartFile("video", "video.mp4", "video/mp4", "video content".getBytes());

        // fileStorageService 스텁은 호출되지 않으므로 lenient() 처리
        lenient().when(fileStorageService.saveFile(thumbnail, "thumbnails")).thenReturn("http://example.com/thumbnail.jpg");
        lenient().when(fileStorageService.saveFile(video, "videos")).thenReturn("http://example.com/video.mp4");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                lectureService.saveLecture(name, description, category, instructorId, curriculumJson, thumbnail, video)
        );
        assertEquals("커리큘럼 파싱 오류", exception.getMessage());

        verify(lectureRepository, never()).save(any());
        verify(lectureContentRepository, never()).saveAll(any());
    }

    // 5. 커리큘럼 항목에 제목 누락 시: 예외 발생하는 경우
    @Test
    public void testSaveLecture_MissingTitleInCurriculum() throws IOException {
        // Arrange
        String name = "Test Lecture";
        String description = "Test Description";
        String category = "Test Category";
        Long instructorId = 1L;
        // title이 누락됨
        String curriculumJson = "[{\"section\": 1, \"content\": \"Intro content\"}]";

        MultipartFile thumbnail = new MockMultipartFile("thumbnail", "thumbnail.jpg", "image/jpeg", "thumbnail content".getBytes());
        MultipartFile video = new MockMultipartFile("video", "video.mp4", "video/mp4", "video content".getBytes());

        lenient().when(fileStorageService.saveFile(thumbnail, "thumbnails")).thenReturn("http://example.com/thumbnail.jpg");
        lenient().when(fileStorageService.saveFile(video, "videos")).thenReturn("http://example.com/video.mp4");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                lectureService.saveLecture(name, description, category, instructorId, curriculumJson, thumbnail, video)
        );
        assertEquals("각 커리큘럼 항목은 제목을 포함해야 합니다.", exception.getMessage());

        verify(lectureRepository, never()).save(any(Lecture.class));
        verify(lectureContentRepository, never()).saveAll(any());
    }

    // 6. 커리큘럼 항목의 section 필드가 올바른 형식이 아닐 때: try-catch 문 실행 테스트
    @Test
    public void testSaveLecture_InvalidSectionField() throws IOException {
        // Arrange
        String name = "Test Lecture";
        String description = "Test Description";
        String category = "Test Category";
        Long instructorId = 1L;
        // section 필드에 정수가 아닌 문자열("invalid")이 들어가 오류 발생
        String curriculumJson = "[{\"section\": \"invalid\", \"title\": \"Introduction\", \"content\": \"Intro content\"}]";

        MultipartFile thumbnail = new MockMultipartFile("thumbnail", "thumbnail.jpg", "image/jpeg", "thumbnail content".getBytes());
        MultipartFile video = new MockMultipartFile("video", "video.mp4", "video/mp4", "video content".getBytes());

        String thumbnailUrl = "http://example.com/thumbnail.jpg";
        String videoUrl = "http://example.com/video.mp4";
        when(fileStorageService.saveFile(thumbnail, "thumbnails")).thenReturn(thumbnailUrl);
        when(fileStorageService.saveFile(video, "videos")).thenReturn(videoUrl);

        // Lecture 저장은 정상적으로 진행되도록 스텁 처리
        Lecture lecture = Lecture.builder()
                .instructorId(instructorId)
                .title(name)
                .description(description)
                .price(0.0)
                .category(category)
                .thumbnailUrl(thumbnailUrl)
                .status(LectureStatus.PENDING)
                .build();
        when(lectureRepository.save(any(Lecture.class))).thenReturn(lecture);

        // Act & Assert: section 필드 파싱 오류를 검증
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                lectureService.saveLecture(name, description, category, instructorId, curriculumJson, thumbnail, video)
        );
        assertEquals("커리큘럼의 section 필드 오류", exception.getMessage());

        // 파일 저장 및 Lecture 저장은 호출되었으나, 오류로 LectureContent 저장은 이루어지지 않음
        verify(lectureRepository, times(1)).save(any(Lecture.class));
        verify(lectureContentRepository, never()).saveAll(any());
    }
}
