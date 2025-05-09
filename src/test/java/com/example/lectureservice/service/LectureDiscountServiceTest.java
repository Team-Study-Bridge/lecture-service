package com.example.lectureservice.service;

import com.example.lectureservice.dto.LectureDiscountResponse;
import com.example.lectureservice.entity.LectureDiscount;
import com.example.lectureservice.repository.LectureDiscountRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@DisplayName("LectureDiscountService 일반 테스트")
class LectureDiscountServiceTest {

    @Autowired
    private LectureDiscountService discountService;

    @Autowired
    private LectureDiscountRepository discountRepository;

    @BeforeEach
    void setUp() {
        discountRepository.deleteAll();
        LectureDiscount discount = LectureDiscount.builder()
                .productId(1L)
                .discountStock(3)
                .discountRate(new BigDecimal("50.00"))
                .startTime(LocalDateTime.now().minusDays(1))
                .endTime(LocalDateTime.now().plusDays(1))
                .build();
        discountRepository.save(discount);
    }

    @Test
    @DisplayName("할인 정보가 없을 경우 정가로 결제 진행된다")
    void 할인정보가_없을_경우_정가로_결제된다() {
        LectureDiscountResponse response = discountService.reserveDiscount(999L);
        assertFalse(response.applied());
        assertEquals(BigDecimal.ZERO, response.discountRate());
    }

    @Test
    @DisplayName("할인 수량이 있을 경우 할인 적용되고 수량 차감된다")
    void 할인수량이_있으면_할인적용_및_차감된다() {
        LectureDiscountResponse response = discountService.reserveDiscount(1L);
        assertTrue(response.applied());
        assertEquals(new BigDecimal("50.00"), response.discountRate());

        LectureDiscount updated = discountRepository.findByProductIdForUpdate(1L).orElseThrow();
        assertEquals(2, updated.getDiscountStock());
    }

    @Test
    @DisplayName("할인 수량이 없으면 예외가 발생한다")
    void 할인수량이_없으면_예외가_발생한다() {
        LectureDiscount discount = discountRepository.findByProductIdForUpdate(1L).orElseThrow();
        discount.setDiscountStock(0);
        discountRepository.save(discount);

        assertThrows(IllegalStateException.class, () -> discountService.reserveDiscount(1L));
    }

    @Test
    @DisplayName("할인 수량이 정상적으로 복구된다")
    void 할인수량이_복구된다() {
        // given
        LectureDiscount discount = discountRepository.findByProductIdForUpdate(1L).orElseThrow();
        int beforeStock = discount.getDiscountStock();

        // when
        discountService.restoreDiscountStock(1L);

        // then
        LectureDiscount updated = discountRepository.findByProductIdForUpdate(1L).orElseThrow();
        assertEquals(beforeStock + 1, updated.getDiscountStock());
    }

    @Test // test
    @DisplayName("할인 정보가 없을 경우 예외가 발생한다")
    void 할인정보가_없으면_예외발생() {
        // given
        Long notExistProductId = 999L;

        // then
        assertThrows(EntityNotFoundException.class, () -> discountService.restoreDiscountStock(notExistProductId));
    }
}

