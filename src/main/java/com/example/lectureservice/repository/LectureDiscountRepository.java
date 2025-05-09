package com.example.lectureservice.repository;

import com.example.lectureservice.entity.LectureDiscount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LectureDiscountRepository extends JpaRepository<LectureDiscount, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM LectureDiscount l WHERE l.productId = :productId")
    Optional<LectureDiscount> findByProductIdForUpdate(@Param("productId") Long productId);
}
