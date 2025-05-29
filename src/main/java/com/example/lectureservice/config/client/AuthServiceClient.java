package com.example.lectureservice.config.client;

import com.example.lectureservice.dto.InstructorProfileResponseDTO;
import com.example.lectureservice.dto.TeacherGetNameResponseDTO;
import com.example.lectureservice.dto.UserIdRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class AuthServiceClient {

    private final WebClient authClient;

    public String getTeacherName(String accessToken, Long userId) {
        try {
            return authClient.post()
                    .uri("/auths/teacher/api/teacher-name")
                    .header("Authorization", "Bearer " + accessToken)
                    .bodyValue(UserIdRequestDTO.builder().userId(userId).build())  // ⬅️ JSON body 설정
                    .retrieve()
                    .bodyToMono(TeacherGetNameResponseDTO.class)
                    .block()
                    .getInstructorName();
        } catch (Exception e) {
            throw new RuntimeException("인증 서비스 호출 실패", e);
        }
    }

    public InstructorProfileResponseDTO getInstructorProfile(String accessToken, Long userId) {
        try {
            return authClient.get()
                    .uri("/auths/teacher/api/teachers/{userId}/profile", userId)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(InstructorProfileResponseDTO.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("강사 프로필 조회 실패", e);
        }
    }
}
