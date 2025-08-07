package com.lighthouse.alarm.mapper;

import com.lighthouse.alarm.dto.AlarmResponseDto;
import com.lighthouse.alarm.entity.Alarms;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

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
    
    // 새로운 알림 삽입
    void insertAlarm(@Param("alarm") Alarms alarm);
    
    // 특정 사용자의 만료 예정 계약 조회
    List<Map<String, Object>> getExpiringContractsByUser(@Param("memberId") Integer memberId, @Param("daysLeft") int daysLeft);
    
    // 특정 사용자의 관심 지역 시세 변화 조회
    List<Map<String, Object>> getInterestAreaPriceChangesByUser(@Param("memberId") Integer memberId);
    
    // 모든 사용자 목록 조회 (알림 생성 대상)
    List<Integer> getAllUserIds();
}
