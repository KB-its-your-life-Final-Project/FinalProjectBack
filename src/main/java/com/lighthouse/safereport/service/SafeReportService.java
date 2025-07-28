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

@Slf4j
@Service
@RequiredArgsConstructor
public class SafeReportService {
    private final SafeReportMapper safereportmapper;
    private final EstateMapper estateMapper;
    private final BuildingRegisterService buildingRegisterService;

    // 건물의 건축연도 점수수, 깡통 전세 점수 계산
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
        BuildingTypeAndPurpose safeBuilding = safereportmapper.getViolateAndPurpose(dto.getLat(), dto.getLng());
        if(safeBuilding == null){
            if(dto.getRoadAddress() != null && !dto.getRoadAddress().trim().isEmpty()){
                try{
                    // 1단계: type=1 (일반)으로 시도
                    System.out.println("1단계 시도: type=1 (일반)");
                    buildingRegisterService.getBuildingRegisterCommon(dto.getRoadAddress(), "1");
                    safeBuilding = safereportmapper.getViolateAndPurpose(dto.getLat(), dto.getLng());

                    if(safeBuilding == null){
                        // 2단계: type=2 (집합)으로 시도
                        System.out.println("1단계 실패, 2단계 시도: type=2 (집합)");
                        buildingRegisterService.getBuildingRegisterCommon(dto.getRoadAddress(), "2");
                        safeBuilding = safereportmapper.getViolateAndPurpose(dto.getLat(), dto.getLng());

                        if(safeBuilding == null){
                            log.warn("type=1, type=2 모두 실패: {}", dto.getRoadAddress());
                            return new BuildingTypeAndPurpose("정보없음", "정보없음");
                        } else {
                            log.info("type=2 (집합)으로 성공: {}", dto.getRoadAddress());
                        }
                    } else {
                        log.info("type=1 (일반)으로 성공: {}", dto.getRoadAddress());
                    }
                }catch(Exception e){
                    log.error("API 호출 실패: {} - {}", dto.getRoadAddress(), e.getMessage());
                    return new BuildingTypeAndPurpose("정보없음", "정보없음");
                }
            }else{
                log.warn("도로명 주소가 없어서 찾을 수 없음: lat={}, lng={}", dto.getLat(), dto.getLng());
                return new BuildingTypeAndPurpose("정보없음", "정보없음");
            }
        }
        return safeBuilding;
    }


}
