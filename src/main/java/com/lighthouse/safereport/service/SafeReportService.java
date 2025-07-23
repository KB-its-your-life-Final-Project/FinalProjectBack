package com.lighthouse.safereport.service;

import com.lighthouse.safereport.dto.SafeReportRequestDto;
import com.lighthouse.safereport.mapper.SafeReportMapper;
import com.lighthouse.safereport.vo.FormData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class SafeReportService {
    private final SafeReportMapper safeReportMapper;

    public FormData generateSafeReport(SafeReportRequestDto dto) {
        FormData result = safeReportMapper.selectByCoord(dto.getLat(), dto.getLng());
        if(result == null) return null;

        int budget = dto.getBudget();
        int dealAmount = result.getDealAmount();
        int buildYear = result.getBuildYear();

        double ratio = (budget / (double)dealAmount) * 100;
        int ratioScore = (ratio <= 70) ? 0 : (ratio <= 80) ? 1 : (ratio <= 90) ? 2 : 3;

        int currentYear = LocalDate.now().getYear();
        int age = currentYear - buildYear;
        int ageScore = (age <= 10) ? 1 : (age <= 20) ? 2 : (age <= 30) ? 3 : 4;

        int totalScore = ratioScore + ageScore;
        result.setScore(totalScore);
        return result;
    }
}
