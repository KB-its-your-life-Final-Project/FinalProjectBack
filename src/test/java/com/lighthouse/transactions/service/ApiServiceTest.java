package com.lighthouse.transactions.service;

import com.lighthouse.config.EnvLoader;
import com.lighthouse.config.RootConfig;
import com.lighthouse.lawdCode.dto.LawdCdRequestDTO;
import com.lighthouse.lawdCode.service.LawdCodeService;
import com.lighthouse.security.config.SecurityConfig;
import com.lighthouse.transactions.dto.ApiNameCallDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
import java.util.List;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = { RootConfig.class, SecurityConfig.class }, initializers = EnvLoader.class)
@Slf4j
@ActiveProfiles("local")
class ApiServiceTest {

    @Autowired
    ApiService apiService;
    @Autowired
    LawdCodeService lawdCodeService;

    @Test
    @DisplayName("법정동코드 조회 테스트")
    void insertLawdCd() {
        int pageNo = 3;
        int numOfRow = 1000;
        apiService.insertLawdCd(pageNo, numOfRow);
    }

    @Test
    @DisplayName("아파트 매매 조회 테스트")
    void insertApartmentTrades() {
        int lawdCd = 48310;
        int dealYmd = 202411;
        apiService.insertApartmentTrades(lawdCd, dealYmd);
    }
    @Test
    @DisplayName("아파트 전월세 조회 테스트")
    void insertApartmentRentals() {
        int lawdCd = 48310;
        int dealYmd = 202411;
        apiService.insertApartmentRentals(lawdCd, dealYmd);
    }
    @Test
    @DisplayName("연립다세대 매매 조회 테스트")
    void insertMultiHouseTrades() {
        int lawdCd = 48310;
        int dealYmd = 202411;
        apiService.insertMultiHouseTrades(lawdCd, dealYmd);
    }
    @Test
    @DisplayName("연립다세대 전월세 조회 테스트")
    void insertMultiHouseRentals() {
        int lawdCd = 48310;
        int dealYmd = 202411;
        apiService.insertMultiHouseRentals(lawdCd, dealYmd);
    }
    @Test
    @DisplayName("오피스텔 매매 조회 테스트")
    void insertOfficetelTrade() {
        int lawdCd = 48310;
        int dealYmd = 202411;
        apiService.insertOfficetelTrade(lawdCd, dealYmd);
    }
    @Test
    @DisplayName("오피스텔 전월세 조회 테스트")
    void insertOfficetelRental() {
        int lawdCd = 48310;
        int dealYmd = 202410;
        apiService.insertOfficetelRental(lawdCd, dealYmd);
    }
    @Test
    @DisplayName("단독/다가구 매매 조회 테스트")
    void insertSingleHouseTrade() {
        int lawdCd = 48310;
        int dealYmd = 202410;
        apiService.insertSingleHouseTrade(lawdCd, dealYmd);
    }
    @Test
    @DisplayName("단독/다가구 전월세 조회 테스트")
    void insertSingleHouseRental() {
        int lawdCd = 48310;
        int dealYmd = 202412;
        apiService.insertSingleHouseRental(lawdCd, dealYmd);
    }

    // estate_api_integration_tbl, estate_api_integration_sales_tbl 삽입 테스트
    @Test
    @DisplayName("아파트 매매 estate_api_integration_tbl, estate_api_integration_sales_tbl 삽입 테스트")
    void insertAptTradesToEstApiIntgTest() {
        int lawdCd = 48310;
        int dealYmd = 202501;
        apiService.insertAptTradesToEstApiIntg(lawdCd, dealYmd);
    }
    @Test
    @DisplayName("아파트 전월세 estate_api_integration_tbl, estate_api_integration_sales_tbl 삽입 테스트")
    void insertAptRentalsToEstApiIntgTest() {
        int lawdCd = 48310;
        int dealYmd = 202501;
        apiService.insertAptRentalsToEstApiIntg(lawdCd, dealYmd);
    }
    @Test
    @DisplayName("오피스텔 매매 estate_api_integration_tbl, estate_api_integration_sales_tbl 삽입 테스트")
    void insertOffTradesToEstApiIntgTest() {
        int lawdCd = 48310;
        int dealYmd = 202502;
        apiService.insertOffTradesToEstApiIntg(lawdCd, dealYmd);
    }
    @Test
    @DisplayName("오피스텔 전월세 estate_api_integration_tbl, estate_api_integration_sales_tbl 삽입 테스트")
    void insertOffRentalsToEstApiIntgTest() {
        int lawdCd = 48310;
        int dealYmd = 202502;
        apiService.insertOffRentalsToEstApiIntg(lawdCd, dealYmd);
    }
    @Test
    @DisplayName("연립다세대 매매 estate_api_integration_tbl, estate_api_integration_sales_tbl 삽입 테스트")
    void insertMHTradesToEstApiIntgTest() {
        int lawdCd = 48310;
        int dealYmd = 202502;
        apiService.insertMHTradesToEstApiIntg(lawdCd, dealYmd);
    }
    @Test
    @DisplayName("연립다세대 전월세 estate_api_integration_tbl, estate_api_integration_sales_tbl 삽입 테스트")
    void insertMHRentalsToEstApiIntgTest() {
        int lawdCd = 48310;
        int dealYmd = 202502;
        apiService.insertMHRentalsToEstApiIntg(lawdCd, dealYmd);
    }
    @Test
    @DisplayName("단독/다가구 매매 estate_api_integration_tbl, estate_api_integration_sales_tbl 삽입 테스트")
    void insertSHTradesToEstApiIntgTest() {
        int lawdCd = 48310;
        int dealYmd = 202505;
        apiService.insertSHTradesToEstApiIntg(lawdCd, dealYmd);
    }
    @Test
    @DisplayName("단독/다가구 전월세 estate_api_integration_tbl, estate_api_integration_sales_tbl 삽입 테스트")
    void insertSHRentalsToEstApiIntgTest() {
        int lawdCd = 41210;
        int dealYmd = 202501;
        apiService.insertSHRentalsToEstApiIntg(lawdCd, dealYmd);
    }

    @Test
    @DisplayName("모든 API 삽입 테스트:  법정동코드, 시작~종료 연월까지")
    void insertAllApiToEstApiIntgTest() throws InterruptedException{
        int lawdCd = 11110; // 서울특별시:11110, 광명시:41210, 하남시:41450, 거제시:48310
        int startYmd = 202412;
        int endYmd = 202501;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");
        YearMonth start = YearMonth.parse(String.valueOf(startYmd), formatter);
        YearMonth end = YearMonth.parse(String.valueOf(endYmd), formatter);
        // API 함수 목록
        List<ApiNameCallDTO> apiList = List.of(
            new ApiNameCallDTO("아파트 매매", apiService::insertAptTradesToEstApiIntg),
            new ApiNameCallDTO("아파트 전월세", apiService::insertAptRentalsToEstApiIntg),
            new ApiNameCallDTO("오피스텔 매매", apiService::insertOffTradesToEstApiIntg),
            new ApiNameCallDTO("오피스텔 전월세", apiService::insertOffRentalsToEstApiIntg),
            new ApiNameCallDTO("연립다세대 매매", apiService::insertMHTradesToEstApiIntg),
            new ApiNameCallDTO("연립다세대 전월세", apiService::insertMHRentalsToEstApiIntg),
            new ApiNameCallDTO("단독/다가구 매매", apiService::insertSHTradesToEstApiIntg),
            new ApiNameCallDTO("단독/다가구 전월세", apiService::insertSHRentalsToEstApiIntg)
        );

        for (YearMonth current = start; !current.isAfter(end); current = current.plusMonths(1)) {
            int dealYmd = Integer.parseInt(current.format(formatter));
            log.info("법정동코드: {}, 연월: {}", lawdCd, dealYmd);
            for (ApiNameCallDTO api: apiList) {
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

    /**
     * 모든 시군구 코드에 대해 10년치 데이터를 삽입하는 편의 메서드
     */
    @Test
    @DisplayName("모든 시군구코드에 대해 10년치 부동산 데이터 삽입 테스트")
    @Transactional
    @Rollback
    public void insertAllRegionEstateDataFor10YearsTest() throws InterruptedException {
        // 모든 고유 시군구코드 조회
        LawdCdRequestDTO dto = new LawdCdRequestDTO();
        List<Integer> allUniqueLawdCodes = lawdCodeService.getAllUniqueRegionCodesWithPagination();

        // 10년치 데이터 삽입
        int startYmd = 202506;
        int endYmd = 202507;

        log.debug("데이터 삽입 시작 - 기간: {} ~ {}", startYmd, endYmd);

        apiService.insertEstateApiIntgAndSalesTbl(allUniqueLawdCodes, startYmd, endYmd);
    }
}