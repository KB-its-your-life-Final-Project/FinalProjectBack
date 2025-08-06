package com.lighthouse.alarm.mapper;

import com.lighthouse.alarm.dto.AlarmResponseDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AlarmMapper {
    // 알람 설정 변경
    void updateAlarmSetting(@Param("memberId") Integer memberId,
    @Param("alarmType") Integer alarmType,
    @Param("getAlarm") Integer getAlarm                                );

    // 제공 받을 알림 리스트 조회
    List<AlarmResponseDto> getAlarmList(@Param("memberId") Integer memberId);

    // 알림 읽음 처리
    void setAlarmRead(@Param("memberId")Integer memberId, @Param("alarmId")Integer alarmId);
}
