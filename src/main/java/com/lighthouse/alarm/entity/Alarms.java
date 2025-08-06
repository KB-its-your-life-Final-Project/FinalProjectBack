package com.lighthouse.alarm.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Alarms {
    private Integer alarmId;
    private Integer userId;
    private Integer type; // 1: 계약만료 30일전, 2: 계약만료 7일전
    private String text; // 알림 내용
    private LocalDateTime time;
    private Integer isChecked; // 0: 미확인, 1: 확인
}
