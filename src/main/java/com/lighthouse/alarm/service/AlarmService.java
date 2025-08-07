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
    
    // 알림 설정 변경 (get_alarm 업데이트)
    public void updateAlarmSetting(Integer memberId, Integer alarmType, Integer getAlarm){
        alarmMapper.updateAlarmSetting(memberId, alarmType, getAlarm);
        log.info("알림 설정 변경: memberId={}, alarmType={}, getAlarm={}", memberId, alarmType, getAlarm);
    }
    
    // 받아야 할 알림 목록 조회 (get_alarm=1이고 is_checked=0인 것들만)
    public List<AlarmResponseDto> getAlarmList(Integer memberId){
        List<AlarmResponseDto> alarmsList = alarmMapper.getAlarmList(memberId);
        return alarmsList;
    }
    
    // 알림 읽음 처리 (is_checked를 1로 변경)
    public void setAlarmRead(Integer memberId, Integer alarmId){
        alarmMapper.setAlarmRead(memberId, alarmId);
        log.info("알림 읽음 처리: memberId={}, alarmId={}", memberId, alarmId);
    }
    
    // 새로운 알림 생성
    public void createAlarm(Integer memberId, Integer type, String text, String regIp) {
        try {
            log.info("알림 생성 시작: memberId={}, type={}, text={}, regIp={}", memberId, type, text, regIp);
            
            // alarm_tbl에 알림 저장
            Alarms alarm = Alarms.builder()
                    .memberId(memberId)
                    .type(type)
                    .text(text)
                    .regIp(regIp)
                    .regDate(LocalDateTime.now())
                    .isChecked(0) // 0: 확인 안함, 1: 확인함
                    .getAlarm(1) // 0: 알림 받지 않음, 1: 알림 받기
                    .build();
            
            log.info("알림 엔티티 생성 완료: {}", alarm);
            
            alarmMapper.insertAlarm(alarm);
            
            log.info("알림 생성 완료: memberId={}, type={}, text={}", memberId, type, text);
            
        } catch (Exception e) {
            log.error("알림 생성 실패: memberId={}, type={}", memberId, type, e);
        }
    }
    
    // 계약 만료 알림 생성
    public void createContractExpirationNotification(Integer memberId, String propertyAddress, int daysLeft, String regIp) {
        String alarmText;
        
        if (daysLeft == 1) {
            alarmText = "등록하신 매물 '" + propertyAddress + "'의 계약이 내일 만료됩니다!";
        } else if (daysLeft <= 7) {
            alarmText = "등록하신 매물 '" + propertyAddress + "'의 계약이 " + daysLeft + "일 후 만료됩니다. (긴급)";
        } else {
            alarmText = "등록하신 매물 '" + propertyAddress + "'의 계약이 " + daysLeft + "일 후 만료됩니다.";
        }
        
        createAlarm(memberId, 3, alarmText, regIp); // type=3: 계약 만료 알림
    }
    
    // 시세 변화 알림 생성
    public void createPriceChangeNotification(Integer memberId, String areaName, String changeInfo, String regIp) {
        String text = String.format("관심 지역 '%s'의 시세가 %s", areaName, changeInfo);
        createAlarm(memberId, 2, text, regIp); // type 2: 시세변화 알림
    }
    
    // 로그인 시 사용자의 알림 조건 체크
    public void checkUserAlarmsOnLogin(Integer memberId, String regIp) {
        log.info("사용자 {} 로그인 시 알림 조건 체크 시작", memberId);
        
        try {
            // 사용자의 집 정보 확인
            log.info("사용자 {}의 집 정보 확인 중...", memberId);
            
            // 디버깅: 사용자의 모든 집 정보 조회
            List<Map<String, Object>> allHomes = alarmMapper.getAllUserHomes(memberId);
            log.info("사용자 {}의 모든 집 정보: {}건", memberId, allHomes.size());
            for (Map<String, Object> home : allHomes) {
                log.info("집 정보: {}", home);
            }
            
            // 1. 계약 만료 알림 체크
            checkUserContractExpiration(memberId, regIp);
            
            // 2. 시세 변화 알림 체크
            checkUserPriceChanges(memberId, regIp);
            
            log.info("사용자 {} 로그인 시 알림 조건 체크 완료", memberId);
            
        } catch (Exception e) {
            log.error("사용자 {} 로그인 시 알림 조건 체크 중 오류", memberId, e);
        }
    }
    
    // 특정 사용자의 계약 만료 알림 체크
    private void checkUserContractExpiration(Integer memberId, String regIp) {
        // 30일 전부터 매일 만료 계약 체크
        for (int daysLeft = 30; daysLeft >= 1; daysLeft--) {
            checkUserExpiringContracts(memberId, daysLeft, regIp);
        }
    }
    
    // 특정 사용자의 만료 예정 계약 체크
    private void checkUserExpiringContracts(Integer memberId, int daysLeft, String regIp) {
        try {
            log.info("사용자 {}의 {}일 전 만료 계약 체크 시작", memberId, daysLeft);
            
            List<Map<String, Object>> expiringContracts = alarmMapper.getExpiringContractsByUser(memberId, daysLeft);
            log.info("사용자 {}의 {}일 전 만료 계약 조회 결과: {}건", memberId, daysLeft, expiringContracts.size());
            
            // 조회 결과 상세 로그
            if (expiringContracts.isEmpty()) {
                log.info("사용자 {}의 {}일 전 만료 계약이 없습니다.", memberId, daysLeft);
            } else {
                for (Map<String, Object> contract : expiringContracts) {
                    log.info("계약 정보: {}", contract);
                }
            }
            
            for (Map<String, Object> contract : expiringContracts) {
                String propertyAddress = (String) contract.get("property_address");
                log.info("계약 만료 예정 매물: {}", propertyAddress);
                
                if (propertyAddress != null) {
                    // 오늘 이미 해당 매물의 계약 만료 알림이 생성되었는지 체크
                    boolean alarmExists = alarmMapper.checkTodayContractAlarmExists(memberId, propertyAddress);
                    log.info("오늘 생성된 알림 존재 여부: {}", alarmExists);
                    
                    if (!alarmExists) {
                        log.info("계약 만료 알림 생성: memberId={}, propertyAddress={}, daysLeft={}", memberId, propertyAddress, daysLeft);
                        createContractExpirationNotification(memberId, propertyAddress, daysLeft, regIp);
                    } else {
                        log.info("오늘 이미 생성된 계약 만료 알림이 있습니다: memberId={}, propertyAddress={}", memberId, propertyAddress);
                    }
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
    private void checkUserPriceChanges(Integer memberId, String regIp) {
        try {
            List<Map<String, Object>> priceChanges = alarmMapper.getInterestAreaPriceChangesByUser(memberId);
            
            for (Map<String, Object> change : priceChanges) {
                String areaName = (String) change.get("area_name");
                String changeInfo = (String) change.get("change_info");
                
                if (areaName != null && changeInfo != null) {
                    createPriceChangeNotification(memberId, areaName, changeInfo, regIp);
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