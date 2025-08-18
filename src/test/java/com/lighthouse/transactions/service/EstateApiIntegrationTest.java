package com.lighthouse.transactions.service;

import com.lighthouse.config.EnvLoader;
import com.lighthouse.config.RootConfig;
import com.lighthouse.lawdCode.service.LawdCodeService;
import com.lighthouse.security.config.SecurityConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Slf4j
@WebAppConfiguration
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { RootConfig.class, SecurityConfig.class }, initializers = EnvLoader.class)
@ActiveProfiles("local")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Disabled("실제 데이터 호출 통합 테스트 - 빌드 시 실행 제외")
class EstateApiIntegrationTest {

    @Autowired
    private ApiService apiService;
    @Autowired
    private LawdCodeService lawdCodeService;

    /**
     * 1. 단일 시군구코드 + 단일 연월 테스트
     */
    @Test
    @Order(1)
    @DisplayName("단일 시군구코드 단일 연월 Estate API Integration 테스트")
    @Transactional
    @Rollback(true) // 실제 데이터 삽입
    void singleRegionSingleMonthTest() {
        // Given
        List<Integer> singleLawdCd = Arrays.asList(11215); // 거제시:48310, 광진구:11215
        int startYmd = 202009;
        int endYmd = 202009; // 같은 달로 설정

        // When & Then
        assertDoesNotThrow(() -> {
            long startTime = System.currentTimeMillis();
            apiService.insertEstateApiIntgAndSalesTbl(singleLawdCd, startYmd, endYmd);
            long endTime =  System.currentTimeMillis();

            long elapsedMs = endTime - startTime;
            long minutes = elapsedMs / (1000 * 60);
            long seconds = (elapsedMs / 1000) % 60;
            log.info("✅ 단일 시군구코드 단일 연월 테스트 완료 - 소요시간: {}분 {}초", minutes, seconds);
        });
    }

    /**
     * 2. 단일 시군구코드 + 다중 연월 테스트
     */
    @Test
    @Order(2)
    @DisplayName("단일 시군구코드 다중 연월 Estate API Integration 테스트")
    @Transactional
    @Rollback(false) // 실제 데이터 삽입
    void singleRegionMultipleMonthsTest() {
        // Given
        List<Integer> singleLawdCd = Arrays.asList(11215); // 41210:광명시, 11215:광진구
        int startYmd = 202506;
        int endYmd = 202506; //

        // When & Then
        assertDoesNotThrow(() -> {
            long startTime = System.currentTimeMillis();
            apiService.insertEstateApiIntgAndSalesTbl(singleLawdCd, startYmd, endYmd);
            long endTime = System.currentTimeMillis();

            long elapsedMs = endTime - startTime;
            long minutes = elapsedMs / (1000 * 60);
            long seconds = (elapsedMs / 1000) % 60;
            log.info("✅ 단일 시군구코드 다중 연월 테스트 완료 - 소요시간: {}분 {}초", minutes, seconds);
        });
    }

    /**
     * 3. 다중 시군구코드 + 단일 연월 테스트
     */
    @Test
    @Order(3)
    @DisplayName("다중 시군구코드 단일 연월 Estate API Integration 테스트")
    @Transactional
    @Rollback // 테스트 완료 후 데이터 삭제
    void multipleRegionsSingleMonthTest() {
        // Given
        List<Integer> multipleLawdCd = Arrays.asList(
                11110, // 서울특별시 종로구
                41210, // 광명시
                48310  // 거제시
        );
        int startYmd = 202411;
        int endYmd = 202411; // 같은 달

        // When & Then
        assertDoesNotThrow(() -> {
            long startTime = System.currentTimeMillis();
            apiService.insertEstateApiIntgAndSalesTbl(multipleLawdCd, startYmd, endYmd);
            long endTime = System.currentTimeMillis();

            long elapsedMs = endTime - startTime;
            long minutes = elapsedMs / (1000 * 60);
            long seconds = (elapsedMs / 1000) % 60;
            log.info("✅ 다중 시군구코드 단일 연월 테스트 완료 - 소요시간: {}분 {}초", minutes, seconds);
        });
    }

    /**
     * 4. 소규모 다중 시군구코드 + 다중 연월 테스트
     */
    @Test
    @Order(4)
    @DisplayName("소규모 다중 시군구코드 다중 연월 Estate API Integration 테스트")
    @Transactional
    @Rollback // 테스트 완료 후 데이터 삭제
    void smallScaleMultipleRegionsMultipleMonthsTest() {
        // Given
        List<Integer> smallScaleLawdCd = Arrays.asList(
                11110, // 서울특별시 종로구
                41450  // 하남시
        );
        int startYmd = 202409;
        int endYmd = 202410; // 2개월

        // When & Then
        assertDoesNotThrow(() -> {
            long startTime = System.currentTimeMillis();
            apiService.insertEstateApiIntgAndSalesTbl(smallScaleLawdCd, startYmd, endYmd);
            long endTime = System.currentTimeMillis();

            long elapsedMs = endTime - startTime;
            long minutes = elapsedMs / (1000 * 60);
            long seconds = (elapsedMs / 1000) % 60;
            log.info("✅ 소규모 다중 시군구코드 다중 연월 테스트 완료 - 소요시간: {}분 {}초", minutes, seconds);
        });
    }

    /**
     * 5. 중규모 테스트 - 일부 시도의 모든 시군구 (예: 경남 일부)
     */
    @Test
    @Order(5)
    @DisplayName("중규모 Estate API Integration 테스트 - 경남 일부 시군구")
    @Transactional
    @Rollback // 테스트 완료 후 데이터 삭제
    void mediumScaleRegionalDataTest() {
        // Given - 경남 일부 시군구코드들
        List<Integer> mediumScaleLawdCd = Arrays.asList(
                48110, // 창원시 의창구
                48111, // 창원시 성산구
                48121, // 창원시 마산합포구
                48123, // 창원시 마산회원구
                48125, // 창원시 진해구
                48170, // 진주시
                48220, // 통영시
                48240, // 사천시
                48250, // 김해시
                48310  // 거제시
        );
        int startYmd = 202412;
        int endYmd = 202412; // 1개월만

        // When & Then
        assertDoesNotThrow(() -> {
            long startTime = System.currentTimeMillis();
            apiService.insertEstateApiIntgAndSalesTbl(mediumScaleLawdCd, startYmd, endYmd);
            long endTime = System.currentTimeMillis();

            long elapsedMs = endTime - startTime;
            long minutes = elapsedMs / (1000 * 60);
            long seconds = (elapsedMs / 1000) % 60;
            log.info("✅ 중규모 테스트 완료 - 소요시간: {}분 {}초", minutes, seconds);
        });
    }

    /**
     * 6. 통합 테스트 - 전국 모든 시군구 (주의: 1개월 기준 30-40분 걸림)
     */
    @Test
    @Order(6)
    @DisplayName("통합 Estate API Integration & Sales 테스트 - 전국 모든 시군구")
    @Transactional
    @Rollback(false) // 실제 데이터 삽입
    @Timeout(value = 14400) // 4시간 타임아웃
    void allRegionsTest() {
        // Given
        List<Integer> allUniqueLawdCodes = lawdCodeService.getAllUniqueRegionCodesWithPagination();
        log.info("📋 불러온 시군구 리스트: {}", allUniqueLawdCodes);
        int startYmd = 202506;
        int endYmd = 202506; // 1개월만 (전체이므로)
        log.info("🚀 통합 테스트 시작 - 전체 시군구: {} 개", allUniqueLawdCodes.size());

        // When & Then
        assertDoesNotThrow(() -> {
            long startTime = System.currentTimeMillis();
            apiService.insertEstateApiIntgAndSalesTbl(allUniqueLawdCodes, startYmd, endYmd);
            long endTime = System.currentTimeMillis();

            long totalMinutes = (endTime - startTime) / (1000 * 60);
            log.info("✅ 통합 테스트 완료 - 소요시간: {}분", totalMinutes);
        });
    }

    /**
     * 7. 성능 테스트 - 시간 측정 및 통계
     */
    @Test
    @Order(7)
    @DisplayName("성능 테스트 - API 호출 시간 측정")
    @Transactional
    @Rollback // 테스트 완료 후 데이터 삭제
    void performanceMetricsTest() {
        // Given
        List<Integer> testLawdCd = Arrays.asList(11110, 41450); // 11110:서울 종로구, 41450:하남시
        int startYmd = 202412;
        int endYmd = 202501; // 2개월

        // 예상 API 호출 수 계산
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");
        YearMonth start = YearMonth.parse(String.valueOf(startYmd), formatter);
        YearMonth end = YearMonth.parse(String.valueOf(endYmd), formatter);
        int monthCount = (int) start.until(end.plusMonths(1), java.time.temporal.ChronoUnit.MONTHS);
        int expectedApiCalls = testLawdCd.size() * monthCount * 8; // 8개 API 타입

        log.info("📊 성능 테스트 시작");
        log.info("예상 API 호출 수: {} 건", expectedApiCalls);

        // When
        long startTime = System.currentTimeMillis();
        assertDoesNotThrow(() -> {
            apiService.insertEstateApiIntgAndSalesTbl(testLawdCd, startYmd, endYmd);
        });
        long endTime = System.currentTimeMillis();

        // Then - 성능 지표 출력
        long totalTimeMs = endTime - startTime;
        double avgTimePerCall = (double) totalTimeMs / expectedApiCalls;
        double callsPerSecond = expectedApiCalls / ((double) totalTimeMs / 1000);

        log.info("📈 성능 테스트 결과:");
        log.info("총 소요시간: {} ms ({} 초)", totalTimeMs, String.format("%.3f", totalTimeMs / 1000.0));
        log.info("호출당 평균시간: {} ms ({} 초)", String.format("%.2f", avgTimePerCall), String.format("%.2f", avgTimePerCall / 1000.0));
        log.info("초당 처리량: {} 건/초", String.format("%.2f", callsPerSecond));

        // 성능 임계값 검증 (예시)
        Assertions.assertTrue(avgTimePerCall < 5000,
                "API 호출당 평균 시간이 5초를 초과했습니다: " + avgTimePerCall + "ms");
    }

    /**
     * 8. 예외 상황 테스트 - 잘못된 시군구코드
     */
    @Test
    @Order(8)
    @DisplayName("예외 상황 테스트 - 존재하지 않는 시군구코드")
    @Transactional
    @Rollback // 테스트 완료 후 데이터 삭제
    void invalidLawdCodeTest() {
        // Given - 존재하지 않는 시군구코드
        List<Integer> invalidLawdCd = Arrays.asList(99999, 88888);
        int startYmd = 202412;
        int endYmd = 202412;
        log.info("🧪 예외 상황 테스트 - 잘못된 시군구코드로 테스트");

        // When & Then - 예외가 발생하더라도 전체 프로세스는 계속 진행되어야 함
        assertDoesNotThrow(() -> {
            apiService.insertEstateApiIntgAndSalesTbl(invalidLawdCd, startYmd, endYmd);
        }, "잘못된 시군구코드가 있어도 전체 프로세스는 중단되지 않아야 합니다.");
    }

    /**
     * 9. 빈 목록 테스트
     */
    @Test
    @Order(9)
    @DisplayName("빈 시군구코드 목록 테스트")
    @Transactional
    @Rollback // 테스트 완료 후 데이터 삭제
    void emptyLawdCodeListTest() {
        // Given
        List<Integer> emptyLawdCd = Arrays.asList();
        int startYmd = 202412;
        int endYmd = 202412;
        log.info("🧪 빈 목록 테스트");

        // When & Then
        assertDoesNotThrow(() -> {
            long startTime = System.currentTimeMillis();
            apiService.insertEstateApiIntgAndSalesTbl(emptyLawdCd, startYmd, endYmd);
            long endTime = System.currentTimeMillis();
            log.info("✅ 빈 목록 테스트 완료 - 소요시간: {}ms", (endTime - startTime));
        });
    }

    /**
     * 테스트 완료 표시
     */
    @AfterEach
    void afterEach(TestInfo testInfo) {
        log.info("🧹 테스트 완료: {}", testInfo.getDisplayName());
    }

    /**
     * 모든 테스트 완료 표시
     */
    @AfterAll
    static void afterAll() {
        log.info("🎉 모든 Estate API Integration 테스트 완료");
    }

    /**
     * 헬퍼 메소드
     */
    private void assertDoesNotThrow(Runnable runnable) {
        Assertions.assertDoesNotThrow(runnable::run);
    }

    private void assertDoesNotThrow(Runnable runnable, String message) {
        Assertions.assertDoesNotThrow(runnable::run, message);
    }
}