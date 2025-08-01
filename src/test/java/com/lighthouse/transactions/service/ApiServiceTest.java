package com.lighthouse.transactions.service;

import com.lighthouse.config.EnvLoader;
import com.lighthouse.config.RootConfig;
import com.lighthouse.security.config.SecurityConfig;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.BiConsumer;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = { RootConfig.class, SecurityConfig.class }, initializers = EnvLoader.class)
@Slf4j
@ActiveProfiles("local")
class ApiServiceTest {

    @Autowired
    ApiService service;

    @Test
    @DisplayName("법정동코드 조회 테스트")
    void insertLawdCd() {
        int pageNo = 3;
        int numOfRow = 1000;
        service.insertLawdCd(pageNo, numOfRow);
    }

    @Test
    @DisplayName("아파트 매매 조회 테스트")
    void insertApartmentTrades() {
        int lawdCd = 48310;
        int dealYmd = 202411;
        service.insertApartmentTrades(lawdCd, dealYmd);
    }
    @Test
    @DisplayName("아파트 전월세 조회 테스트")
    void insertApartmentRentals() {
        int lawdCd = 48310;
        int dealYmd = 202411;
        service.insertApartmentRentals(lawdCd, dealYmd);
    }
    @Test
    @DisplayName("연립다세대 매매 조회 테스트")
    void insertMultiHouseTrades() {
        int lawdCd = 48310;
        int dealYmd = 202411;
        service.insertMultiHouseTrades(lawdCd, dealYmd);
    }
    @Test
    @DisplayName("연립다세대 전월세 조회 테스트")
    void insertMultiHouseRentals() {
        int lawdCd = 48310;
        int dealYmd = 202411;
        service.insertMultiHouseRentals(lawdCd, dealYmd);
    }
    @Test
    @DisplayName("오피스텔 매매 조회 테스트")
    void insertOfficetelTrade() {
        int lawdCd = 48310;
        int dealYmd = 202411;
        service.insertOfficetelTrade(lawdCd, dealYmd);
    }
    @Test
    @DisplayName("오피스텔 전월세 조회 테스트")
    void insertOfficetelRental() {
        int lawdCd = 48310;
        int dealYmd = 202410;
        service.insertOfficetelRental(lawdCd, dealYmd);
    }
    @Test
    @DisplayName("단독/다가구 매매 조회 테스트")
    void insertSingleHouseTrade() {
        int lawdCd = 48310;
        int dealYmd = 202410;
        service.insertSingleHouseTrade(lawdCd, dealYmd);
    }
    @Test
    @DisplayName("단독/다가구 전월세 조회 테스트")
    void insertSingleHouseRental() {
        int lawdCd = 48310;
        int dealYmd = 202412;
        service.insertSingleHouseRental(lawdCd, dealYmd);
    }

    // estate_api_integration_tbl, estate_api_integration_sales_tbl 삽입 테스트
    @Test
    @DisplayName("아파트 매매 estate_api_integration_tbl, estate_api_integration_sales_tbl 삽입 테스트")
    void insertAptTradesToEstApiIntgTest() {
        int lawdCd = 48310;
        int dealYmd = 202501;
        service.insertAptTradesToEstApiIntg(lawdCd, dealYmd);
    }
    @Test
    @DisplayName("아파트 전월세 estate_api_integration_tbl, estate_api_integration_sales_tbl 삽입 테스트")
    void insertAptRentalsToEstApiIntgTest() {
        int lawdCd = 48310;
        int dealYmd = 202501;
        service.insertAptRentalsToEstApiIntg(lawdCd, dealYmd);
    }
    @Test
    @DisplayName("오피스텔 매매 estate_api_integration_tbl, estate_api_integration_sales_tbl 삽입 테스트")
    void insertOffTradesToEstApiIntgTest() {
        int lawdCd = 48310;
        int dealYmd = 202502;
        service.insertOffTradesToEstApiIntg(lawdCd, dealYmd);
    }
    @Test
    @DisplayName("오피스텔 전월세 estate_api_integration_tbl, estate_api_integration_sales_tbl 삽입 테스트")
    void insertOffRentalsToEstApiIntgTest() {
        int lawdCd = 48310;
        int dealYmd = 202502;
        service.insertOffRentalsToEstApiIntg(lawdCd, dealYmd);
    }
    @Test
    @DisplayName("연립다세대 매매 estate_api_integration_tbl, estate_api_integration_sales_tbl 삽입 테스트")
    void insertMHTradesToEstApiIntgTest() {
        int lawdCd = 48310;
        int dealYmd = 202502;
        service.insertMHTradesToEstApiIntg(lawdCd, dealYmd);
    }
    @Test
    @DisplayName("연립다세대 전월세 estate_api_integration_tbl, estate_api_integration_sales_tbl 삽입 테스트")
    void insertMHRentalsToEstApiIntgTest() {
        int lawdCd = 48310;
        int dealYmd = 202502;
        service.insertMHRentalsToEstApiIntg(lawdCd, dealYmd);
    }
    @Test
    @DisplayName("단독/다가구 매매 estate_api_integration_tbl, estate_api_integration_sales_tbl 삽입 테스트")
    void insertSHTradesToEstApiIntgTest() {
        int lawdCd = 48310;
        int dealYmd = 202505;
        service.insertSHTradesToEstApiIntg(lawdCd, dealYmd);
    }
    @Test
    @DisplayName("단독/다가구 전월세 estate_api_integration_tbl, estate_api_integration_sales_tbl 삽입 테스트")
    void insertSHRentalsToEstApiIntgTest() {
        int lawdCd = 48310;
        int dealYmd = 202411;
        service.insertSHRentalsToEstApiIntg(lawdCd, dealYmd);
    }

    // API 명 + 실행 함수 매핑 클래스
    @AllArgsConstructor
    static class ApiNameCall {
        String apiName;
        BiConsumer<Integer, Integer> apiCall;
    }

    @Test
    @DisplayName("모든 API 삽입 테스트:  법정동코드, 시작~종료 연월까지 5초 간격 호출")
    void insertAllApiToEstApiIntgTest() throws InterruptedException{
        int lawdCd = 41210; // 광명시:41210, 하남시:41450, 거제시:48310
        int startYmd = 202401;
        int endYmd = 202412;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");
        YearMonth start = YearMonth.parse(String.valueOf(startYmd), formatter);
        YearMonth end = YearMonth.parse(String.valueOf(endYmd), formatter);
        // API 함수 목록
        List<ApiNameCall> apiList = List.of(
            new ApiNameCall("아파트 매매", service::insertAptTradesToEstApiIntg),
            new ApiNameCall("아파트 전월세", service::insertAptRentalsToEstApiIntg),
            new ApiNameCall("오피스텔 매매", service::insertOffTradesToEstApiIntg),
            new ApiNameCall("오피스텔 전월세", service::insertOffRentalsToEstApiIntg),
            new ApiNameCall("연립다세대 매매", service::insertMHTradesToEstApiIntg),
            new ApiNameCall("연립다세대 전월세", service::insertMHRentalsToEstApiIntg),
            new ApiNameCall("단독/다가구 매매", service::insertSHTradesToEstApiIntg),
            new ApiNameCall("단독/다가구 전월세", service::insertSHRentalsToEstApiIntg)
        );

        for (YearMonth current = start; !current.isAfter(end); current = current.plusMonths(1)) {
            int dealYmd = Integer.parseInt(current.format(formatter));
            log.info("법정동코드: {}, 연월: {}", lawdCd, dealYmd);
            for (ApiNameCall api: apiList) {
                try {
                    log.info("{} 호출", api.apiName);
                    api.apiCall.accept(lawdCd, dealYmd);
                    log.info("{} 완료", api.apiName);
                } catch (Exception e) {
                    log.error("{} 실패 - 법정동코드: {}, 연월: {}", api.apiName, lawdCd, dealYmd, e);
                    throw e;
                }
            }
        }
    }
}