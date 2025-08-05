package com.lighthouse.safereport.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecentSafeReport {
    
    private Integer id;
    private Integer userId;
    private Integer estateId; // estate_api_integration_tbl 참조
    private Integer budget; // 사용자가 입력한 예산
    private String resultGrade; // 안심레포트 결과 등급
    private Integer isDelete;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 