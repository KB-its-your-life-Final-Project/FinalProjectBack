package com.lighthouse.alarm.service;

import com.lighthouse.alarm.dto.AlarmResponseDto;
import com.lighthouse.alarm.entity.Alarms;
import com.lighthouse.alarm.mapper.AlarmMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
    
    // 새로운 알림 생성
    public void createAlarm(Integer memberId, Integer type, String text) {
        try {
            // 1. 데이터베이스에 알림 저장
            Alarms alarm = Alarms.builder()
                    .memberId(memberId)
                    .type(type)
                    .text(text)
                    .time(LocalDateTime.now())
                    .isChecked(0)
                    .build();
            
            alarmMapper.insertAlarm(alarm);
            
            log.info("알림 생성 완료: memberId={}, type={}, text={}", memberId, type, text);
            
        } catch (Exception e) {
            log.error("알림 생성 실패: memberId={}, type={}", memberId, type, e);
        }
    }
    
    // 계약 만료 알림 생성
    public void createContractExpirationNotification(Integer memberId, String propertyAddress, int daysLeft) {
        String text = String.format("등록하신 매물 '%s'의 계약이 %d일 후 만료됩니다.", propertyAddress, daysLeft);
        createAlarm(memberId, 1, text); // type 1: 계약만료 알림
    }
    
    // 시세 변화 알림 생성
    public void createPriceChangeNotification(Integer memberId, String areaName, String changeInfo) {
        String text = String.format("관심 지역 '%s'의 시세가 %s", areaName, changeInfo);
        createAlarm(memberId, 2, text); // type 2: 시세변화 알림
    }
    
    // 로그인 시 사용자의 알림 조건 체크
    public void checkUserAlarmsOnLogin(Integer memberId) {
        log.info("사용자 {} 로그인 시 알림 조건 체크 시작", memberId);
        
        try {
            // 1. 계약 만료 알림 체크
            checkUserContractExpiration(memberId);
            
            // 2. 시세 변화 알림 체크
            checkUserPriceChanges(memberId);
            
            log.info("사용자 {} 로그인 시 알림 조건 체크 완료", memberId);
            
        } catch (Exception e) {
            log.error("사용자 {} 로그인 시 알림 조건 체크 중 오류", memberId, e);
        }
    }
    
    // 특정 사용자의 계약 만료 알림 체크
    private void checkUserContractExpiration(Integer memberId) {
        // 30일 전 만료 계약 체크
        checkUserExpiringContracts(memberId, 30);
        
        // 7일 전 만료 계약 체크
        checkUserExpiringContracts(memberId, 7);
        
        // 1일 전 만료 계약 체크
        checkUserExpiringContracts(memberId, 1);
    }
    
    // 특정 사용자의 만료 예정 계약 체크
    private void checkUserExpiringContracts(Integer memberId, int daysLeft) {
        try {
            List<Map<String, Object>> expiringContracts = alarmMapper.getExpiringContractsByUser(memberId, daysLeft);
            
            for (Map<String, Object> contract : expiringContracts) {
                String propertyAddress = (String) contract.get("property_address");
                
                if (propertyAddress != null) {
                    createContractExpirationNotification(memberId, propertyAddress, daysLeft);
                }
            }
            
            if (!expiringContracts.isEmpty()) {
                log.info("사용자 {}의 {}일 전 만료 계약 알림 생성 완료: {}건", memberId, daysLeft, expiringContracts.size());
            }
        } catch (Exception e) {
            log.error("사용자 {}의 {}일 전 만료 계약 알림 생성 중 오류", memberId, daysLeft, e);
        }
    }
    
    // 특정 사용자의 시세 변화 알림 체크
    private void checkUserPriceChanges(Integer memberId) {
        try {
            List<Map<String, Object>> priceChanges = alarmMapper.getInterestAreaPriceChangesByUser(memberId);
            
            for (Map<String, Object> change : priceChanges) {
                String areaName = (String) change.get("area_name");
                String changeInfo = (String) change.get("change_info");
                
                if (areaName != null && changeInfo != null) {
                    createPriceChangeNotification(memberId, areaName, changeInfo);
                }
            }
            
            if (!priceChanges.isEmpty()) {
                log.info("사용자 {}의 시세 변화 알림 생성 완료: {}건", memberId, priceChanges.size());
            }
        } catch (Exception e) {
            log.error("사용자 {}의 시세 변화 알림 생성 중 오류", memberId, e);
        }
    }
}
