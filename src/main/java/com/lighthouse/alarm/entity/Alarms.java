package com.lighthouse.alarm.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Alarms {
    private Integer id; 
    private Integer memberId;
    private Integer type; // 1: 계약만료 30일전, 2: 계약만료 7일전
    private String text; // 알림 내용
    private String regIp;
    private LocalDateTime regDate;
    private Integer isChecked; // 0: 미확인, 1: 확인
    private Integer getAlarm; // 0: 비활성화, 1: 활성화
}
