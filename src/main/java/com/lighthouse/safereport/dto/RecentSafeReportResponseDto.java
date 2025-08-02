package com.lighthouse.safereport.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecentSafeReportResponseDto {
    private Long id;
    private String buildingName;//건물명
    private String roadAddress;//도로명주소
    private String resultGrade;//결과 등급(위험/주의/안전)
    private LocalDateTime updatedAt;//등록일/수정일
}
