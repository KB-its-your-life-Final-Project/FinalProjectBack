package com.lighthouse.safereport.service;

import com.lighthouse.safereport.dto.SafeReportRequestDto;
import com.lighthouse.safereport.mapper.SafeReportMapper;
import com.lighthouse.safereport.vo.BuildingTypeAndPurpose;
import com.lighthouse.safereport.vo.RentalRatioAndBuildyear;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class SafeReportService {
    private final SafeReportMapper mapper;

    // 건물의 건축연도, 깡통 전세 점수 계산
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
        return safeBuilding;
    }


}
