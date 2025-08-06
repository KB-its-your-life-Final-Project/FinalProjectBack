package com.lighthouse.alarm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AlarmResponseDto {
    private Integer alarmId;
    private Integer type;
    private String text;
    private LocalDateTime time;
    private Integer isChecked;
}
