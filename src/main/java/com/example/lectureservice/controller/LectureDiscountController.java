package com.example.lectureservice.controller;

import com.example.lectureservice.dto.LectureDiscountResponse;
import com.example.lectureservice.service.LectureDiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/lecture-discounts")
@RequiredArgsConstructor
public class LectureDiscountController {

    private final LectureDiscountService discountService;

    @PostMapping("/{productId}/reserve")
    public ResponseEntity<LectureDiscountResponse> reserveDiscount(@PathVariable("productId") Long productId) {
        LectureDiscountResponse response = discountService.reserveDiscount(productId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{productId}/rollback")
    public ResponseEntity<Void> rollbackDiscount(@PathVariable("productId") Long productId) {
        discountService.restoreDiscountStock(productId);
        return ResponseEntity.ok().build();
    }
}
