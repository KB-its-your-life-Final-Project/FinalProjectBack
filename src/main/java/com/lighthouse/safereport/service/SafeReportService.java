package com.lighthouse.safereport.service;

import com.lighthouse.safereport.dto.SafeReportRequestDto;
import com.lighthouse.safereport.mapper.SafeReportMapper;
import com.lighthouse.safereport.vo.BuildingTypeAndPurpose;
import com.lighthouse.safereport.vo.RentalRatioAndBuildyear;
import com.lighthouse.buildingRegister.service.BuildingRegisterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import com.lighthouse.toCoord.service.AddressGeocodeService;

@Slf4j
@Service
@RequiredArgsConstructor
public class SafeReportService {
    private final SafeReportMapper mapper;
    private final BuildingRegisterService buildingRegisterService;
    private final AddressGeocodeService addressGeocodeService; 

    // 건물의 건축연도 점수수, 깡통 전세 점수 계산
    public RentalRatioAndBuildyear generateSafeReport(SafeReportRequestDto dto) {
        RentalRatioAndBuildyear result = mapper.getRealEstateInfo(dto.getLat(), dto.getLng());//거래 금액, 건축 년도 저장
        if(result == null) return null;

        int budget = dto.getBudget();
        int dealAmount = result.getDealAmount();
        int buildYear = result.getBuildYear();

        double ratio = (budget / (double)dealAmount) * 100;
        int ratioScore = (ratio <= 70) ? 0 : (ratio <= 80) ? 1 : (ratio <= 90) ? 2 : 3;

        int currentYear = LocalDate.now().getYear();
        int age = currentYear - buildYear;
        int ageScore = (age <= 10) ? 1 : (age <= 20) ? 2 : (age <= 30) ? 3 : 4;
        result.setBuildyear_score(ageScore); //연식률 점수 저장

        int totalScore = ratioScore + ageScore;
        //역전세율만 고려하는 것으로 수정됨
        result.setReverse_rental_ratio(ratio);
        result.setScore(ratioScore);
        return result;
    }
    // 건출물 용도, 위반 여부 확인
    public BuildingTypeAndPurpose generateSafeBuilding(SafeReportRequestDto dto) {
        BuildingTypeAndPurpose safeBuilding = mapper.getViolateAndPurpose(dto.getLat(), dto.getLng());
        if(safeBuilding == null){
            if(dto.getRoadAddress() != null && !dto.getRoadAddress().trim().isEmpty()){
                try{
                    // 1단계: type=1 (일반)으로 시도
                    System.out.println("1단계 시도: type=1 (일반)");
                    buildingRegisterService.getBuildingRegisterCommon(dto.getRoadAddress(), "1");
                    safeBuilding = mapper.getViolateAndPurpose(dto.getLat(), dto.getLng());

                    if(safeBuilding == null){
                        // 2단계: type=2 (공중)으로 시도
                        System.out.println("1단계 실패, 2단계 시도: type=2 (집합)");
                        buildingRegisterService.getBuildingRegisterCommon(dto.getRoadAddress(), "2");
                        safeBuilding = mapper.getViolateAndPurpose(dto.getLat(), dto.getLng());

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
