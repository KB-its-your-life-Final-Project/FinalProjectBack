package com.lighthouse.alarm.service;

import com.lighthouse.alarm.dto.AlarmResponseDto;
import com.lighthouse.alarm.entity.Alarms;
import com.lighthouse.alarm.mapper.AlarmMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmService {
    private final AlarmMapper alarmMapper;
    //알림 설정 변경
    public void updateAlarmSetting(Integer memberId, Integer alarmType, Integer getAlarm){
        alarmMapper.updateAlarmSetting(memberId,alarmType,getAlarm);
    }
    //받아야 할 알림 목록 조회(아직 안 읽은 것들만 조회)
    public List<AlarmResponseDto> getAlarmList(Integer memberId){
        List<AlarmResponseDto> alarmsList=alarmMapper.getAlarmList(memberId);
        return alarmsList;
    }
    //알림 읽음 처리
    public void setAlarmRead(Integer memberId, Integer alarmType){
        alarmMapper.setAlarmRead(memberId,alarmType);
    }
}
