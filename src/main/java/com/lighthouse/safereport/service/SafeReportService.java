package com.lighthouse.safereport.service;

import com.lighthouse.safereport.dto.SafeReportRequestDto;
import com.lighthouse.safereport.mapper.SafeReportMapper;
import com.lighthouse.estate.mapper.EstateMapper;
import com.lighthouse.estate.dto.RealEstateDTO;
import com.lighthouse.estate.dto.RealEstateSalesDTO;
import com.lighthouse.safereport.vo.BuildingTypeAndPurpose;
import com.lighthouse.safereport.vo.RentalRatioAndBuildyear;
import com.lighthouse.buildingRegister.service.BuildingRegisterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import com.lighthouse.buildingRegister.dto.BuildingResponseDTO;

@Slf4j
@Service
@RequiredArgsConstructor
public class SafeReportService {
    private final SafeReportMapper safereportmapper;
    private final EstateMapper estateMapper;
    private final BuildingRegisterService buildingRegisterService;

    // 건물의 건축연도 점수, 깡통 전세 점수 계산
    public RentalRatioAndBuildyear generateSafeReport(SafeReportRequestDto dto) {
        // 1단계: 위도/경도로 건물 정보 조회
        RealEstateDTO realEstate = estateMapper.getRealEstateByLocation(dto.getLat(), dto.getLng());
        log.info("realEstate: {}", realEstate);
        
        if(realEstate == null) {
            return null;
        }
        
        // 2단계: 건물 ID 추출
        Integer estateId = realEstate.getId();
        
        // 3단계: 해당 건물의 매매 정보 조회
        List<RealEstateSalesDTO> salesList = estateMapper.getSalesByEstateId(estateId);
        log.info("salesList: {}", salesList);
        
        if(salesList == null || salesList.isEmpty()) {
            return null;
        }
        
        // 4단계: 가장 최근 거래 중 dealAmount가 있는 데이터 선택
        RealEstateSalesDTO latestSaleWithDealAmount = salesList.stream()
            .filter(sale -> sale.getDealYear() != null && sale.getDealMonth() != null && sale.getDealDay() != null)
            .filter(sale -> sale.getDealAmount() != null)
            .sorted((a, b) -> {
                int yearCompare = Integer.compare(b.getDealYear(), a.getDealYear());
                if (yearCompare != 0) return yearCompare;
                int monthCompare = Integer.compare(b.getDealMonth(), a.getDealMonth());
                if (monthCompare != 0) return monthCompare;
                return Integer.compare(b.getDealDay(), a.getDealDay());
            })
            .findFirst()
            .orElse(null);
        log.info("latestSaleWithDealAmount: {}", latestSaleWithDealAmount);

        RealEstateSalesDTO latestSale = latestSaleWithDealAmount;

        if (latestSale == null) {
            // dealAmount가 있는 거래가 없으면, 가장 최근 거래(deposit만 있는 경우) 선택
            latestSale = salesList.stream()
                .filter(sale -> sale.getDealYear() != null && sale.getDealMonth() != null && sale.getDealDay() != null)
                .sorted((a, b) -> {
                    int yearCompare = Integer.compare(b.getDealYear(), a.getDealYear());
                    if (yearCompare != 0) return yearCompare;
                    int monthCompare = Integer.compare(b.getDealMonth(), a.getDealMonth());
                    if (monthCompare != 0) return monthCompare;
                    return Integer.compare(b.getDealDay(), a.getDealDay());
                })
                .findFirst()
                .orElse(null);
        }
        log.info("최종 latestSale: {}", latestSale);

        if (latestSale == null) {
            return null;
        }
        
        // 5단계: 건축년도 추출 (첫 번째 건물 정보 사용)
        Integer buildYear = realEstate.getBuildYear();
        
        // 6단계: 거래 금액 추출 (dealAmount가 null이면 deposit 사용)
        Integer dealAmount = latestSale.getDealAmount();
        Integer deposit = latestSale.getDeposit();
        int finalDealAmount = (dealAmount != null) ? dealAmount : (deposit != null ? deposit : 0);
        log.info("buildYear: {}", buildYear);
        log.info("dealAmount: {}, deposit: {}, finalDealAmount: {}", dealAmount, deposit, finalDealAmount);
        
        if(finalDealAmount == 0 || buildYear == null) {
            return null;
        }
        
        // 7단계: RentalRatioAndBuildyear 객체 생성
        RentalRatioAndBuildyear result = new RentalRatioAndBuildyear();
        result.setDealAmount(finalDealAmount);
        result.setBuildYear(buildYear);
        
        // 비즈니스 로직 처리
        int budget = dto.getBudget();
        double ratio = (budget / (double)finalDealAmount) * 100;
        int ratioScore = (ratio <= 70) ? 0 : (ratio <= 80) ? 1 : (ratio <= 90) ? 2 : 3;

        int currentYear = LocalDate.now().getYear();
        int age = currentYear - buildYear;
        int ageScore = (age <= 10) ? 1 : (age <= 20) ? 2 : (age <= 30) ? 3 : 4;
        result.setBuildyear_score(ageScore); //연식률 점수 저장

        //역전세율만 고려하는 것으로 수정
        result.setReverse_rental_ratio(ratio);
        result.setScore(ratioScore);
        return result;
    }

    
    // 건출물 용도, 위반 여부 확인
    public BuildingTypeAndPurpose generateSafeBuilding(SafeReportRequestDto dto) {
        String address = dto.getRoadAddress();

        // 1단계: DB에서 기존 데이터 확인
        BuildingTypeAndPurpose safeBuilding = safereportmapper.getViolateAndPurpose(dto.getLat(), dto.getLng());
        if(safeBuilding != null) {
            return safeBuilding;
        }

        // 2단계: DB에 데이터가 없으면 토지대장 API 병렬로 호출(type=1(일반주택), type=2(집합주택))
        if(address != null && !address.trim().isEmpty()) {
            try {
                log.info("토지대장 API 병렬 호출 시작: {}", address);
                
                // type=1과 type=2를 병렬로 호출
                CompletableFuture<BuildingResponseDTO> type1Future = 
                    CompletableFuture.supplyAsync(() -> buildingRegisterService.getBuildingRegisterCommon(address, "1"));
                
                CompletableFuture<BuildingResponseDTO> type2Future = 
                    CompletableFuture.supplyAsync(() -> buildingRegisterService.getBuildingRegisterCommon(address, "2"));
                
                // 먼저 성공하는 결과 확인
                BuildingResponseDTO firstSuccess = type1Future.applyToEither(type2Future, result -> result).get();
                
                if(firstSuccess != null) {
                    log.info("토지대장 API 호출 성공: {}", address);
                    
                    // API 호출 성공 시 DB에서 데이터 조회
                    BuildingTypeAndPurpose result = safereportmapper.getViolateAndPurpose(dto.getLat(), dto.getLng());
                    if(result != null) {
                        log.info("DB에서 데이터 조회 성공: {}", result);
                        return result;
                    } else {
                        log.warn("API 호출 성공했지만 DB에 데이터 없음: {}", address);
                    }
                } else {
                    log.warn("토지대장 API 호출 실패: {}", address);
                }

            } catch (Exception e) {
                log.error("토지대장 API 병렬 호출 실패: {} - {}", address, e.getMessage());
            }
        }

        log.warn("모든 시도 실패: {}", address);
        return null;
    }
}
