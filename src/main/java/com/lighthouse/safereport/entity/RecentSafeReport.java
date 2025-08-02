package com.lighthouse.safereport.entity;

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
public class RecentSafeReport {
    
    private Long id;
    private Long userId;
    private Long estateId; // estate_api_integration_tbl 참조
    private String buildingName;
    private String roadAddress;
    private Integer budget; // 사용자가 입력한 예산
    private String resultGrade;
    private LocalDateTime updatedAt;
} 