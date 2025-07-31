package com.lighthouse.localinfo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacilityDTO {
    private String regionCd;       // 법정동 코드
    private String regionName;     // 법정동 이름
    private String locataddNm;     // 풀 주소명

    private Long totalBicycleCount;         // 총 자전거 대수
    // 추가?
    // private Long totalConvenienceStoreCount; // 총 편의점 수
    // private Long totalRestaurantCount;       // 총 식당 수
    // private Long totalCctvCount;             // 총 CCTV 수
}