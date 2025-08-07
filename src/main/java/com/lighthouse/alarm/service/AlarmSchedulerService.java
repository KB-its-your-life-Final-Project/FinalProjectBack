package com.lighthouse.alarm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmSchedulerService {
    
    private final AlarmService alarmService;
    
    // 로그인 시 사용자의 알림 조건 체크 (memberController에서 호출)
    public void checkUserAlarmsOnLogin(Integer memberId, String regIp) {
        log.info("사용자 {} 로그인 시 알림 체크 시작", memberId);
        alarmService.checkUserAlarmsOnLogin(memberId, regIp);
    }
} 