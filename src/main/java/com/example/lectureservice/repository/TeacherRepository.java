package com.example.lectureservice.repository;

import com.example.lectureservice.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    // findByUserId 등 필요한 쿼리 정의 가능
}
