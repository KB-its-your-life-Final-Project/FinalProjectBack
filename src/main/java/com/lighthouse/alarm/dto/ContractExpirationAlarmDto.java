package com.lighthouse.alarm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContractExpirationAlarmDto {
    private Integer userId;
    private String buildingName;
    private String buildingNumber;
    private LocalDate contractEnd;
    private Integer daysUntilExpiration;
    private String alarmType; // "30일전", "7일전"
    private String alarmMessage;
} 