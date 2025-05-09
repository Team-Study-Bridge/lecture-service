package com.example.lectureservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Entity
@Table(name = "lecture_discount")
public class LectureDiscount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "discount_stock", nullable = false)
    private Integer discountStock;

    @Column(name = "discount_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal discountRate;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    public boolean isActiveNow() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(startTime) && now.isBefore(endTime);
    }

    public boolean canReserve() {
        return discountStock > 0 && isActiveNow();
    }

    public void reserve() {
        if (!canReserve()) {
            throw new IllegalStateException("할인 불가: 기간이 아니거나 재고 없음");
        }
        this.discountStock -= 1;
    }

    public void increaseStock() {
        this.discountStock += 1;
    }
}
