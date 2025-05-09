package com.example.lectureservice.dto;

import java.math.BigDecimal;

public record LectureDiscountResponse(
        boolean applied,
        BigDecimal discountRate
) {}
