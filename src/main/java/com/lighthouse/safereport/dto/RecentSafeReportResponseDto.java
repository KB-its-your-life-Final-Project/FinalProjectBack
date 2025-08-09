package com.lighthouse.safereport.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecentSafeReportResponseDto {
    private Integer id;
    private String buildingName;//건물명
    private Integer budget;
    private String roadAddress;//도로명주소
    private String resultGrade;//결과 등급(위험/주의/안전)
    private String updatedAt;//등록일/수정일 (문자열로 변환)
    private int score; //최종 점수 = 전세가율점수 + 위반점수수
}
