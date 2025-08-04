package com.lighthouse.localinfo.entity;

import lombok.Data;

@Data
public class Weather {
    private String regionCd;      // 지역 코드
    private String region;        // 지역 이름 (예: 서울특별시)
    private Integer gridX;        // 기상청 격자 X 좌표
    private Integer gridY;        // 기상청 격자 Y 좌표
    private String locataddNm;    // 전체 주소명
    private String skyCondition;  // 하늘 상태
    private Integer temperature;  // 현재 기온
    private Integer maxTemperature; // 최고 기온
    private Integer minTemperature; // 최저 기온
}