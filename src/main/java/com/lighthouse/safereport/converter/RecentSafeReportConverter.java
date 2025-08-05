package com.lighthouse.safereport.converter;

import com.lighthouse.safereport.entity.RecentSafeReport;
import com.lighthouse.safereport.dto.RecentSafeReportResponseDto;
import com.lighthouse.estate.entity.Estate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
@Slf4j
public class RecentSafeReportConverter {
    
    //RecentSafeReport를 RecentSafeReportResponseDto로 변환
    public RecentSafeReportResponseDto toResponseDto(RecentSafeReport report, Estate estate, String roadAddress) {
        if (estate == null) {
            log.warn("estate 정보를 찾을 수 없습니다: estateId={}", report.getEstateId());
            return null;
        }
        
        String formattedDate = report.getUpdatedAt() != null ? 
            report.getUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : 
            null;
            
        return RecentSafeReportResponseDto.builder()
            .id(report.getId())
            .buildingName(estate.getBuildingName())
            .budget(report.getBudget())
            .roadAddress(roadAddress)
            .resultGrade(report.getResultGrade())
            .updatedAt(formattedDate)
            .build();
    }
} 