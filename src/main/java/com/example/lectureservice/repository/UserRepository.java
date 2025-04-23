package com.example.lectureservice.repository;

import com.example.lectureservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    // 닉네임 가져오는 전용 메서드도 추가 가능
}
