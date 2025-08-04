package com.lighthouse.alarm.dto;

import java.time.LocalDateTime;

//
public class AlarmResponseDto {
    private int alarmId;
    private int type;
    private String text;
    private LocalDateTime time;
    private Boolean isChecked;
}
