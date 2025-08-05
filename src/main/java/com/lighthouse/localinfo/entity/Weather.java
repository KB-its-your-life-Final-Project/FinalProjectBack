package com.lighthouse.localinfo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Weather {
    private Long id;
    private String sidoCd;
    private String sggCd;
    private String region;
    private int gridX;
    private int gridY;
    private String skyCondition;
    private Integer temperature;
    private Integer maxTemperature;
    private Integer minTemperature;
    private String baseDate;
    private String baseTime;
    private String fcstDate;
    private String fcstTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}