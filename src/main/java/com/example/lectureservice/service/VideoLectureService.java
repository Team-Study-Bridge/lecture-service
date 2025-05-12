package com.example.lectureservice.service;

import com.example.lectureservice.entity.Lecture;
import com.example.lectureservice.repository.LectureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
@RequiredArgsConstructor
public class VideoLectureService {

    private final LectureRepository lectureRepository;

    public String getVideoStreamUrl(Long id) {
        Lecture lecture = lectureRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 강의를 찾을 수 없습니다."));

        if (lecture.getVideoUrl() == null || lecture.getVideoUrl().isBlank()) {
            throw new IllegalStateException("해당 강의에 영상이 등록되어 있지 않습니다.");
        }

        return lecture.getVideoUrl();
    }

    public ResponseEntity<StreamingResponseBody> proxyVideoStream(Long lectureId) {
        String s3Url = getVideoStreamUrl(lectureId);

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(s3Url).openConnection();
            connection.setRequestProperty("Range", "bytes=0-"); // Seek 지원
            connection.connect();

            String contentType = connection.getContentType();
            long contentLength = connection.getContentLengthLong();
            String contentRange = connection.getHeaderField("Content-Range");
            InputStream inputStream = connection.getInputStream();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentLength(contentLength);
            headers.set("Accept-Ranges", "bytes");

            StreamingResponseBody responseBody = outputStream -> {
                inputStream.transferTo(outputStream); // Core: 실시간 스트리밍
                inputStream.close();
            };

            if (contentRange != null) {
                headers.set("Content-Range", contentRange);
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                        .headers(headers)
                        .body(responseBody);
            }

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(responseBody);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
