package com.example.lectureservice.service;

import com.example.lectureservice.dto.LectureDiscountResponse;
import com.example.lectureservice.entity.LectureDiscount;
import com.example.lectureservice.repository.LectureDiscountRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LectureDiscountService {

    private final LectureDiscountRepository repository;

    @Transactional
    public LectureDiscountResponse reserveDiscount(Long productId) {
        Optional<LectureDiscount> optionalDiscount = repository.findByProductIdForUpdate(productId);

        if (optionalDiscount.isEmpty()) {
            // 할인 정보가 없을 때 정가 결제
            return new LectureDiscountResponse(false, BigDecimal.ZERO);
        }

        LectureDiscount discount = optionalDiscount.get();

        if (!discount.canReserve()) {
            // 할인 수량 부족 or 기간 아님
            throw new IllegalStateException("남은 할인 수량이 없습니다. 또는 할인 기간이 아닙니다.");
        }

        discount.reserve();
        repository.save(discount);

        return new LectureDiscountResponse(true, discount.getDiscountRate());
    }

    @Transactional
    public void restoreDiscountStock(Long productId) {
        Optional<LectureDiscount> optionalDiscount = repository.findByProductIdForUpdate(productId);

        if (optionalDiscount.isEmpty()) {
            throw new EntityNotFoundException("할인 정보가 존재하지 않습니다.");
        }

        LectureDiscount discount = optionalDiscount.get();
        discount.increaseStock();
        repository.save(discount);
    }
}
