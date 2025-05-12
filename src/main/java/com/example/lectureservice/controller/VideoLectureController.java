package com.example.lectureservice.controller;

import com.example.lectureservice.service.VideoLectureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lectures/video")
public class VideoLectureController {

    private final VideoLectureService videoLectureService;

    @GetMapping("/{lectureId}/stream")
    public ResponseEntity<StreamingResponseBody> getVideoStreamUrl(@PathVariable Long lectureId) {
        return videoLectureService.proxyVideoStream(lectureId);
    }
}
