package com.lighthouse.localinfo.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 데이터베이스나 외부 API로부터 받은 원본 날씨 및 지역 정보를 담는 객체(Value Object)입니다.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WeatherVO {

    // --- 지역 정보 ---
    private String regionCd;      // 지역 코드
    private String region;        // 지역 이름 (예: 서울특별시)
    private int gridX;            // 기상청 격자 X 좌표
    private int gridY;            // 기상청 격자 Y 좌표
    private String locataddNm;    // 전체 주소명

    // --- 날씨 정보 (기상청 API 응답 필드) ---

    private String skyCondition;
    private Integer temperature;
    private Integer maxTemperature;
    private Integer minTemperature;
}