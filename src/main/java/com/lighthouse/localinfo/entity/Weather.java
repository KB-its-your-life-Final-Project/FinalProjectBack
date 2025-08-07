package com.lighthouse.localinfo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Weather {
    private Long id;  // 롬복으로 getter/setter 자동 생성

    private String sidoCd;
    private String sggCd;
    private String region;
    private Integer gridX;
    private Integer gridY;
    private String skyCondition;
    private Integer temperature;
    private Integer maxTemperature;
    private Integer minTemperature;
    private String baseDate;
    private String baseTime;
    private String fcstDate;
    private String fcstTime;
}