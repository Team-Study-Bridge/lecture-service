package com.example.lectureservice.service;

import com.example.lectureservice.LectureServiceApplication;
import com.example.lectureservice.dto.LectureDiscountResponse;
import com.example.lectureservice.entity.LectureDiscount;
import com.example.lectureservice.repository.LectureDiscountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = LectureServiceApplication.class)
@ActiveProfiles("test")
@DisplayName("LectureDiscountService 동시성 테스트")
@TestPropertySource(locations = "classpath:application-test.yml")
class LectureDiscountConcurrencyTest {

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
    @DisplayName("동시 요청이 들어와도 수량이 초과 차감되지 않는다")
    void 동시요청이_들어와도_수량이_초과차감되지_않는다() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<Future<LectureDiscountResponse>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(() -> {
                try {
                    return discountService.reserveDiscount(1L);
                } catch (Exception e) {
                    return null;
                } finally {
                    latch.countDown();
                }
            }));
        }

        latch.await();

        long successCount = futures.stream().filter(f -> {
            try {
                return f.get() != null && f.get().applied();
            } catch (Exception e) {
                return false;
            }
        }).count();

        assertEquals(3, successCount);
    }
}
