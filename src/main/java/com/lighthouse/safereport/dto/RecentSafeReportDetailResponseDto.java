package com.lighthouse.safereport.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecentSafeReportDetailResponseDto {
    private Integer id;
    private String buildingName;
    private String dongName;
    private String jibunAddress;
    private String roadAddress;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer budget;
    private String reportData; // JSON 형태의 리포트 데이터
    private String diagnosisStatus;
    private LocalDateTime diagnosisDate;
    private LocalDateTime createdAt;
} 