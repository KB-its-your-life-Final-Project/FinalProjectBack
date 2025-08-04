package com.lighthouse.localinfo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WeatherDTO {
    private String regionCd;
    private String region;
    private int gridX;
    private int gridY;
    private String locataddNm;

    // 기상청 API 응답 필드들 추가
    private String skyCondition; // SKY: 하늘상태
    private Integer temperature; // TMP: 1시간 기온
    private Integer maxTemperature; // TMX: 일 최고기온
    private Integer minTemperature; // TMN: 일 최저기온
}