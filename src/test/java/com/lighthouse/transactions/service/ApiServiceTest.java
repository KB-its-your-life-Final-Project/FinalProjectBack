package com.lighthouse.transactions.service;

import com.lighthouse.config.EnvLoader;
import com.lighthouse.config.RootConfig;
import com.lighthouse.security.config.SecurityConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = { RootConfig.class, SecurityConfig.class }, initializers = EnvLoader.class)
@Slf4j
@ActiveProfiles("local")
class ApiServiceTest {

    @Autowired
    ApiService service;

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
    @Test
    @DisplayName("아파트 매매 estate_api_integration_tbl 삽입 테스트")
    void insertApartmentTradesToEstateApiIntegrationTest() {
        int lawdCd = 48310;
        int dealYmd = 202502;
        service.insertApartmentTradesToEstateApiIntegration(lawdCd, dealYmd);
    }

    @Test
    @DisplayName("법정동코드 조회 테스트")
    void insertLawdCd() {
        int pageNo = 3;
        int numOfRow = 1000;
        service.insertLawdCd(pageNo, numOfRow);
    }
}